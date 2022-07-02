package com.lf.distrifs.core.grpc.client;

public enum RpcClientStatus {

    WAIT_INIT(0, "Wait to init server list..."),

    INITIALIZED(1, "Server list is ready, wait to starting..."),

    STARTING(2, "Client already staring, wait to connect with server..."),

    UNHEALTHY(3, "Client unhealthy, may closed by server or timeout, may reconnecting..."),

    RUNNING(4, "Client is running"),

    SHUTDOWN(5, "Client is shutdown")

    ;

    int status;
    String desc;

    RpcClientStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
