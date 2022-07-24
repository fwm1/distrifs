package com.lf.distrifs.core.grpc.common;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.lf.distrifs.common.Constants;
import com.lf.distrifs.util.NetUtils;

import java.util.List;

public class ServerListManager implements ServerListFactory {
    @Override
    public String genNextServer() {
        String serverIp = System.getProperty("distrifs.target.server.ip", "127.0.0.1");
        String serverPort = System.getProperty("distrifs.target.server.port");
        if (Strings.isNullOrEmpty(serverPort)) {
            return serverIp + ":" + Constants.DETAIL_PORT;
        }
        return serverIp + ":" + Integer.parseInt(serverPort);
    }

    @Override
    public String getCurrentServer() {
        return "127.0.0.1:" + NetUtils.LOCAL_PORT;
    }

    @Override
    public List<String> getServerList() {
        return ImmutableList.of("127.0.0.1:" + NetUtils.LOCAL_PORT);
    }

}
