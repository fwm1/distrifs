package com.lf.distrifs.core.grpc.request;

import com.lf.distrifs.core.grpc.response.Response;

public interface RequestFuture {

    boolean isDone();

    Response get() throws Exception;

    Response get(long timeout) throws Exception;

}
