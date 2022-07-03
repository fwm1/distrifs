package com.lf.distrifs.common.packagescan;


import com.google.common.base.Strings;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Classes {

    private static final String PACKAGE_SEPARATOR = ".";
    private static final String PATH_SEPARATOR = "/";
    private static final String URL_PROTOCOL_FILE = "file";
    private static final String URL_PROTOCOL_JAR = "jar";
    private static final String CLASS_FILE_EXTENSION = ".class";
    private static final String INNER_CLASS_FILE_SYMBOL = "$";

    public static void load(String basePackage, ClassLoader classLoader, Callback callback, Predicate<Class> filter) {
        if (Strings.isNullOrEmpty(basePackage)) {
            return;
        }
        String basePath = basePackage.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
        try {
            Enumeration<URL> urls = classLoader.getResources(basePath);
            while (urls.hasMoreElements()) {
                load(basePackage, urls.nextElement(), classLoader, callback, filter);
            }
        } catch (IOException e) {
            throw new RuntimeException("Errored getting resources from " + basePath, e);
        }
    }

    private static void load(String basePackage, URL url, ClassLoader classLoader, Callback callback, Predicate<Class> filter) {
        String protocol = url.getProtocol();
        String path = url.getPath();
        if (URL_PROTOCOL_FILE.equals(protocol)) {
            loadFromFile(basePackage, new File(path), classLoader, callback, filter);
        } else if (URL_PROTOCOL_JAR.equals(protocol)) {
            try (JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile()) {
                loadFromJar(basePackage, jarFile, classLoader, callback, filter);
            } catch (IOException e) {
                throw new RuntimeException("Errored loading jar " + url, e);
            }
        }
    }

    private static void loadFromFile(String packageName, File file, ClassLoader classLoader, Callback callback, Predicate<Class> filter) {
        File[] subFiles = file.listFiles(CLASS_OR_DIRECTORY_FILE_FILTER);
        if (subFiles != null) {
            for (File subFile : subFiles) {
                String fileName = subFile.getName();
                if (subFile.isFile()) {
                    String className = getClassName(packageName, fileName);
                    loadClass(className, classLoader, callback, filter);
                } else {
                    String subPackageName = Strings.isNullOrEmpty(packageName) ?
                            fileName : packageName + PACKAGE_SEPARATOR + fileName;
                    loadFromFile(subPackageName, subFile, classLoader, callback, filter);
                }
            }
        }
    }

    private static void loadFromJar(String packageName, JarFile jarFile, ClassLoader classLoader, Callback callback, Predicate<Class> filter) {
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String jarEntryName = jarEntry.getName();
            if (!jarEntry.isDirectory() && isNormalClassFile(jarEntryName)) {
                String className = getClassName(jarEntryName);
                if (className.startsWith(packageName)) {
                    loadClass(className, classLoader, callback, filter);
                }
            }
        }
    }

    private static String getClassName(String packageName, String classFileName) {
        String className = classFileName.substring(0, classFileName.lastIndexOf(CLASS_FILE_EXTENSION));
        return Strings.isNullOrEmpty(packageName) ? className : packageName + PACKAGE_SEPARATOR + className;
    }

    private static String getClassName(String classFilePath) {
        String className = classFilePath.substring(0, classFilePath.lastIndexOf(CLASS_FILE_EXTENSION));
        return className.replace(PATH_SEPARATOR, PACKAGE_SEPARATOR);
    }

    private static void loadClass(String className, ClassLoader classLoader, Callback callback, Predicate<Class> classPredicate) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            if (classPredicate.test(clazz)) {
                callback.onLoaded(clazz);
            } else {
                callback.onFiltered(className, clazz);
            }
        } catch (ClassNotFoundException e) {
            callback.onLoadFailed(className, e);
        }
    }

    private static final FileFilter CLASS_OR_DIRECTORY_FILE_FILTER = file -> {
        String fileName = file.getName();
        return file.isFile() ? isNormalClassFile(fileName) : file.isDirectory();
    };

    private static boolean isNormalClassFile(String fileName) {
        return fileName.endsWith(CLASS_FILE_EXTENSION) && !fileName.contains(INNER_CLASS_FILE_SYMBOL);
    }

    public interface Callback {
        //通过filter并被加载成功
        void onLoaded(Class<?> clazz);

        //被filter拦截
        void onFiltered(String className, Class<?> clazz);

        //类加载失败
        void onLoadFailed(String className, ClassNotFoundException cause);
    }

}
