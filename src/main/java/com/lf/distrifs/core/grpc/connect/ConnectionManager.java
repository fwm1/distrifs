package com.lf.distrifs.core.grpc.connect;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ConnectionManager {

    Map<String, GrpcConnection> connections = new ConcurrentHashMap<>();


    public GrpcConnection getConnection(String connectionId) {
        return connections.get(connectionId);
    }

    public void refreshActiveTime(String connectionId) {
        GrpcConnection connection = getConnection(connectionId);
        if (connection != null) {
            connection.freshActiveTime();
        }
    }

    public synchronized boolean register(String connectionId, GrpcConnection connection) {
        if (connection.isConnected()) {
            if (connections.containsKey(connectionId)) {
                return true;
            }
            connections.put(connectionId, connection);
            log.info("new connection registered successfully, connectionId = {},connection={} ", connectionId, connection);
            return true;
        }
        return false;
    }

    public synchronized void unregister(String connectionId) {
        GrpcConnection remove = this.connections.remove(connectionId);
        if (remove != null) {
            remove.close();
            log.info("[{}]Connection unregistered successfully", connectionId);
        }
    }

    public boolean isValid(String connectionId) {
        return connections.containsKey(connectionId);
    }
}
