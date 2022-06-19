package com.lf.distrifs.core.grpc.request;

import com.lf.distrifs.core.grpc.base.PayLoad;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Request implements PayLoad {

    private final Map<String, String> headers = new ConcurrentHashMap<>();


    public void putHeader(String name, String val) {
        headers.put(name, val);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public void putAllHeader(Map<String, String> headerMap) {
        headers.putAll(headerMap);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void clearHeaders() {
        headers.clear();
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", Request.class.getSimpleName() + "[", "]")
                .add("headers=" + headers)
                .toString();
    }
}
