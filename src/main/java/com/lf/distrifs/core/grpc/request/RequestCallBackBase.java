package com.lf.distrifs.core.grpc.request;

import com.lf.distrifs.common.Constants;
import com.lf.distrifs.core.grpc.response.Response;

import java.util.concurrent.Executor;

public abstract class RequestCallBackBase<T extends Response> implements RequestCallback {
    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public long getTimeout() {
        return Constants.DEFAULT_RPC_TIMEOUT;
    }

    @Override
    public void onResponse(Response response) {

    }

    @Override
    public void onException(Throwable e) {

    }
}
