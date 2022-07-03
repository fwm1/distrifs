package com.lf.distrifs.common.packagescan;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class DefaultPackageScan implements PackageScan {

    private final Set<String> packagePaths;

    public DefaultPackageScan(Set<String> packagePaths) {
        this.packagePaths = packagePaths;
    }

    @Override
    public <T> Set<Class<T>> getSubTypesOf(Class<T> pClazz) {
        Set<Class<T>> result = new HashSet<>();
        for (String packagePath : packagePaths) {
            Classes.load(packagePath, DefaultPackageScan.class.getClassLoader(), new Classes.Callback() {
                @Override
                public void onLoaded(Class<?> clazz) {
                    result.add((Class<T>) clazz);
                    log.info("Load class successful, class = {}", clazz.getName());
                }

                @Override
                public void onFiltered(String className, Class<?> clazz) {

                }

                @Override
                public void onLoadFailed(String className, ClassNotFoundException cause) {
                    log.warn("Failed to Load class {}", className);
                }
            }, clazz -> pClazz.isAssignableFrom(clazz));
        }
        return result;
    }

    @Override
    public <T> Set<Class<T>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        return null;
    }
}
