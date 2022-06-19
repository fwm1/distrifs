package com.lf.distrifs.core.grpc.base;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.HashMap;
import java.util.Map;

public class PayloadRegistry implements ApplicationListener<ContextRefreshedEvent> {


    private static final Map<String, Class<?>> REGISTRY_PAYLOAD = new HashMap<>();


    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        Map<String, PayLoad> beansOfType = contextRefreshedEvent.getApplicationContext().getBeansOfType(PayLoad.class);
        for (PayLoad payLoad : beansOfType.values()) {
            Class<? extends PayLoad> clazz = payLoad.getClass();
            REGISTRY_PAYLOAD.put(clazz.getSimpleName(), clazz);
        }
    }

    public static Class getClassByType(String type) {
        return REGISTRY_PAYLOAD.get(type);
    }
}
