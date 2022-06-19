package com.lf.distrifs.core.grpc.request;

import com.lf.distrifs.core.grpc.response.Response;

import java.util.concurrent.Executor;

public interface RequestCallback<T extends Response> {

    Executor getExecutor();

    long getTimeout();

    void onResponse(T response);

    void onException(Throwable e);

}
