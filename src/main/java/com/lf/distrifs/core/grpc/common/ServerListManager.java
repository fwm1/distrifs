package com.lf.distrifs.core.grpc.common;

import com.google.common.collect.ImmutableList;
import com.lf.distrifs.common.Constants;

import java.util.List;

public class ServerListManager implements ServerListFactory {
    @Override
    public String genNextServer() {
        return "127.0.0.1:" + Constants.DETAIL_PORT;
    }

    @Override
    public String getCurrentServer() {
        return "127.0.0.1:" + Constants.DETAIL_PORT;
    }

    @Override
    public List<String> getServerList() {
        return ImmutableList.of("127.0.0.1:" + Constants.DETAIL_PORT);
    }

}
