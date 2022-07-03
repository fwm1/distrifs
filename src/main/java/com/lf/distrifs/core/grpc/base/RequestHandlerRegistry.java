package com.lf.distrifs.core.grpc.base;

import com.google.common.base.Strings;
import com.lf.distrifs.core.grpc.remote.BaseRequestHandler;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class RequestHandlerRegistry implements ApplicationListener<ContextRefreshedEvent> {

    private static final String HANDLER_SUFFIX = "Handler";

    private Map<String, BaseRequestHandler> REGISTRY_REQ_HANDLER = new HashMap<>();


    public BaseRequestHandler getHandler(String type) {
        return REGISTRY_REQ_HANDLER.get(type);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        Map<String, BaseRequestHandler> beansOfType = contextRefreshedEvent.getApplicationContext().getBeansOfType(BaseRequestHandler.class);
        for (BaseRequestHandler handlerBean : beansOfType.values()) {
            REGISTRY_REQ_HANDLER.put(handlerFor(handlerBean), handlerBean);
        }
    }

    private String handlerFor(BaseRequestHandler handler) {
        checkNotNull(handler, "Request handler cannot be null");
        if (Strings.isNullOrEmpty(handler.handleFor())) {
            String clazzName = handler.getClass().getSimpleName();
            if (clazzName.contains(HANDLER_SUFFIX)) {
                return clazzName.substring(0, clazzName.lastIndexOf(HANDLER_SUFFIX));
            } else {
                throw new RuntimeException("Invalid handler name [{}], must be 'xxxHandler'");
            }
        } else {
            return handler.handleFor();
        }
    }
}
