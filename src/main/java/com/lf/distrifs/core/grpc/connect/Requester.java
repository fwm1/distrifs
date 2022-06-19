package com.lf.distrifs.core.grpc.connect;

import com.lf.distrifs.core.grpc.request.Request;
import com.lf.distrifs.core.grpc.request.RequestCallback;
import com.lf.distrifs.core.grpc.request.RequestFuture;
import com.lf.distrifs.core.grpc.response.Response;

public interface Requester {

    Response request(Request request, long timeoutMills) throws Exception;


    RequestFuture requestFuture(Request request) throws Exception;


    void asyncRequest(Request request, RequestCallback<Response> callback) throws Exception;


    void close();
}
