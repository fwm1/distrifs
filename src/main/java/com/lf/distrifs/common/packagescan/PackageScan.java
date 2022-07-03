package com.lf.distrifs.common.packagescan;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface PackageScan {

    <T> Set<Class<T>> getSubTypesOf(Class<T> pClazz);

    <T> Set<Class<T>> getTypesAnnotatedWith(Class<? extends Annotation> annotation);
}
