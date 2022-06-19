package com.lf.distrifs.core.grpc.response;

public class ErrorResponse extends Response {

    public ErrorResponse() {

    }

    public static ErrorResponse build(String errMsg) {
        ErrorResponse response = new ErrorResponse();
        response.resultCode = 500;
        response.msg = errMsg;
        return response;
    }
}
