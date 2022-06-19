package com.lf.distrifs.core.grpc.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConnectionManager {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

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
            LOGGER.info("new connection registered successfully, connectionId = {},connection={} ", connectionId, connection);
            return true;
        }
        return false;
    }

    public boolean isValid(String connectionId) {
        return connections.containsKey(connectionId);
    }
}
