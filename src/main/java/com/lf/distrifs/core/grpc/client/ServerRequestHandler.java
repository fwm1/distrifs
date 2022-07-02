package com.lf.distrifs.core.grpc.client;

import com.lf.distrifs.core.grpc.request.Request;
import com.lf.distrifs.core.grpc.response.Response;

public interface ServerRequestHandler {
    Response requestReply(Request request);
}
