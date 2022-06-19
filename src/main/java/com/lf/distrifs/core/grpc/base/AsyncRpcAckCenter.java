package com.lf.distrifs.core.grpc.base;

import com.lf.distrifs.core.grpc.request.DefaultRequestFuture;
import com.lf.distrifs.core.grpc.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * 异步ack处理中心，根据requestId处理requestFuture
 * grpc的BiStream， request和response都是流式，需要自己来处理异步结果
 */
public class AsyncRpcAckCenter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncRpcAckCenter.class);

    private static final Map<String, Map<String, DefaultRequestFuture>> CALLBACK_MAP = new ConcurrentHashMap<>(128);


    public static void saveCallback(String connectionId, String requestId, DefaultRequestFuture future) {
        Map<String, DefaultRequestFuture> futureMap = getRequestFutureMap(connectionId);
        if (!futureMap.containsKey(requestId)) {
            DefaultRequestFuture pre = futureMap.putIfAbsent(requestId, future);
            if (pre == null) {
                return;
            }
        }
        throw new RuntimeException("Request Id [" + requestId + "] conflict");
    }

    public static void ackNotify(String connectionId, Response response) {
        Map<String, DefaultRequestFuture> requestFutureMap = CALLBACK_MAP.get(connectionId);
        if (requestFutureMap == null) {
            LOGGER.warn("Received ack for a outdated connection, connectionId={}, requestId={}", connectionId, response.getRequestId());
            return;
        }
        DefaultRequestFuture requestFuture = requestFutureMap.get(response.getRequestId());
        if (requestFuture == null) {
            LOGGER.warn("Received ack for a outdated request, connectionId={}, requestId={}", connectionId, response.getRequestId());
            return;
        }
        if (response.isSuccess()) {
            requestFuture.setResponse(response);
        } else {
            requestFuture.setFailResult(new RuntimeException(response.getMsg()));
        }
    }

    public static void clearRequestFutureByConnectionId(String connectionId) {
        CALLBACK_MAP.remove(connectionId);
    }

    public static void clearRequestFuture(String connectionId, String requestId) {
        Map<String, DefaultRequestFuture> requestFutureMap = CALLBACK_MAP.get(connectionId);
        if (requestFutureMap == null || !requestFutureMap.containsKey(requestId)) {
            return;
        }
        requestFutureMap.remove(requestId);
    }

    private static Map<String, DefaultRequestFuture> getRequestFutureMap(String connectionId) {
        if (!CALLBACK_MAP.containsKey(connectionId)) {
            Map<String, DefaultRequestFuture> futureMap = new ConcurrentHashMap<>(128);
            Map<String, DefaultRequestFuture> pre = CALLBACK_MAP.putIfAbsent(connectionId, futureMap);
            return pre == null ? futureMap : pre;
        } else {
            return CALLBACK_MAP.get(connectionId);
        }
    }
}
