package com.lf.distrifs.core.grpc.common;

import java.util.List;

public interface ServerListFactory {

    String genNextServer();

    String getCurrentServer();

    List<String> getServerList();
}
