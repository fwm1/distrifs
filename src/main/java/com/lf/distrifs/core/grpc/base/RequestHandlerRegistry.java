package com.lf.distrifs.core.grpc.base;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RequestHandlerRegistry implements ApplicationListener<ContextRefreshedEvent> {

    private Map<String, BaseRequestHandler> REGISTRY_REQ_HANDLER = new HashMap<>();


    public BaseRequestHandler getHandler(String type) {
        return REGISTRY_REQ_HANDLER.get(type);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        Map<String, BaseRequestHandler> beansOfType = contextRefreshedEvent.getApplicationContext().getBeansOfType(BaseRequestHandler.class);
        for (BaseRequestHandler handlerBean : beansOfType.values()) {
            Class<? extends BaseRequestHandler> clazz = handlerBean.getClass();
            REGISTRY_REQ_HANDLER.put(clazz.getSimpleName(), handlerBean);
        }
    }
}
