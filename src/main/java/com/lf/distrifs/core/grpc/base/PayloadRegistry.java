package com.lf.distrifs.core.grpc.base;

import com.lf.distrifs.common.packagescan.DefaultPackageScan;
import com.lf.distrifs.common.packagescan.PackagePathProvider;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class PayloadRegistry implements ApplicationListener<ContextRefreshedEvent> {


    private static final Map<String, Class<?>> REGISTRY_PAYLOAD = new HashMap<>();

    public static void scan(PackagePathProvider pathProvider) {
        Set<String> packagePaths = pathProvider.getPackagePaths();
        if (packagePaths == null || packagePaths.isEmpty()) return;
        DefaultPackageScan packageScan = new DefaultPackageScan(packagePaths);
        Set<Class<PayLoad>> payloadClasses = packageScan.getSubTypesOf(PayLoad.class);
        payloadClasses.forEach(clz -> registerPayload(clz.getSimpleName(), clz));
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        Map<String, PayLoad> beansOfType = contextRefreshedEvent.getApplicationContext().getBeansOfType(PayLoad.class);
        for (PayLoad payLoad : beansOfType.values()) {
            Class<? extends PayLoad> clazz = payLoad.getClass();
            registerPayload(clazz.getSimpleName(), clazz);
        }
    }

    public static Class getClassByType(String type) {
        return REGISTRY_PAYLOAD.get(type);
    }

    private static void registerPayload(String type, Class<? extends PayLoad> payLoadClass) {
        if (REGISTRY_PAYLOAD.containsKey(type)) {
            throw new IllegalArgumentException(String.format("Duplicated payload type = [%s], clazz = [%s]", type, payLoadClass.getName()));
        }
        REGISTRY_PAYLOAD.put(type, payLoadClass);
    }
}
