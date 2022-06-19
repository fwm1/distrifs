package com.lf.distrifs.core.grpc.response;

import com.lf.distrifs.core.grpc.base.PayLoad;

import java.util.StringJoiner;

public abstract class Response implements PayLoad {

    int resultCode = 200;

    String msg;

    String requestId;

    public boolean isSuccess() {
        return this.resultCode == 200;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getMsg() {
        return msg;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", Response.class.getSimpleName() + "[", "]")
                .add("resultCode=" + resultCode)
                .add("msg='" + msg + "'")
                .toString();
    }
}
