package com.lf.distrifs.core.grpc.common;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class ServerListManager implements ServerListFactory {
    @Override
    public String genNextServer() {
        return "127.0.0.1:8089";
    }

    @Override
    public String getCurrentServer() {
        return "127.0.0.1:8089";
    }

    @Override
    public List<String> getServerList() {
        return ImmutableList.of("127.0.0.1:8089");
    }

}
