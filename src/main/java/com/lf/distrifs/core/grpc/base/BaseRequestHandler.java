package com.lf.distrifs.core.grpc.base;

import com.lf.distrifs.core.grpc.request.Request;
import com.lf.distrifs.core.grpc.request.RequestMeta;
import com.lf.distrifs.core.grpc.response.Response;

public abstract class BaseRequestHandler<T extends Request, R extends Response> {


    public abstract R handle(T request, RequestMeta meta) throws Exception;
}
