package com.lf.distrifs.core.grpc.connect;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class ConnectionMeta {

    String clientIp;

    String remoteIp;

    int remotePort;

    int localPort;

    String connectionId;

    Date createTime;

    long lastActiveTime;

    Map<String, String> labels;

    public ConnectionMeta(String clientIp, String remoteIp, int remotePort, int localPort, String connectionId) {
        this.clientIp = clientIp;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.localPort = localPort;
        this.connectionId = connectionId;
    }

    public String getLabel(String key) {
        return labels.get(key);
    }
}
