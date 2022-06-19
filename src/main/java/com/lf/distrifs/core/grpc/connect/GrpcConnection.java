package com.lf.distrifs.core.grpc.connect;

import com.lf.distrifs.core.grpc.auto.GrpcProto;
import com.lf.distrifs.core.grpc.base.AsyncRpcAckCenter;
import com.lf.distrifs.core.grpc.request.DefaultRequestFuture;
import com.lf.distrifs.core.grpc.request.Request;
import com.lf.distrifs.core.grpc.request.RequestCallback;
import com.lf.distrifs.core.grpc.request.RequestFuture;
import com.lf.distrifs.core.grpc.response.Response;
import com.lf.distrifs.util.GrpcUtils;
import io.grpc.netty.shaded.io.netty.channel.Channel;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GrpcConnection implements Requester {

    private final ConnectionMeta meta;

    private StreamObserver streamObserver;

    private Channel channel;

    private static final AtomicLong ID = new AtomicLong(0);

    public GrpcConnection(ConnectionMeta meta, StreamObserver streamObserver, Channel channel) {
        this.meta = meta;
        this.streamObserver = streamObserver;
        this.channel = channel;
    }

    public boolean isConnected() {
        return channel != null && channel.isOpen() && channel.isActive();
    }

    public Map<String, String> getLabels() {
        return meta.getLabels();
    }

    public void freshActiveTime() {
        meta.setLastActiveTime(System.currentTimeMillis());
    }

    public ConnectionMeta getMeta() {
        return meta;
    }

    @Override
    public Response request(Request request, long timeoutMills) throws Exception {
        DefaultRequestFuture requestFuture = sendReqInner(request, null);
        try {
            return requestFuture.get(timeoutMills);
        } finally {
            AsyncRpcAckCenter.clearRequestFuture(requestFuture.getConnectionId(), requestFuture.getRequestId());
        }
    }

    @Override
    public RequestFuture requestFuture(Request request) throws Exception {
        return sendReqInner(request, null);
    }

    @Override
    public void asyncRequest(Request request, RequestCallback<Response> callback) throws Exception {
        sendReqInner(request, callback);
    }

    @Override
    public void close() {
        if (streamObserver instanceof ServerCallStreamObserver) {
            ServerCallStreamObserver serverCallStreamObserver = (ServerCallStreamObserver) this.streamObserver;
            if (!serverCallStreamObserver.isCancelled()) {
                serverCallStreamObserver.onCompleted();
            }
        }
        channel.close();
    }

    private DefaultRequestFuture sendReqInner(Request request, RequestCallback callback) throws Exception {
        String requestId = String.valueOf(getNextId());
        DefaultRequestFuture requestFuture = new DefaultRequestFuture(meta.connectionId, requestId, callback,
                new DefaultRequestFuture.CallbackOnTimeout() {
                    @Override
                    public void onTimeout() {
                        AsyncRpcAckCenter.clearRequestFuture(meta.connectionId, requestId);
                    }
                });
        AsyncRpcAckCenter.saveCallback(meta.connectionId, requestId, requestFuture);
        sendRequestNoAck(request);
        return requestFuture;
    }


    private void sendRequestNoAck(Request request) throws Exception {
        try {
            //StreamObserver#onNext() is not thread-safe,synchronized is required to avoid direct memory leak.
            synchronized (streamObserver) {
                GrpcProto.Payload payload = GrpcUtils.convert(request);
                streamObserver.onNext(payload);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private Long getNextId() {
        if (ID.longValue() > Long.MAX_VALUE) {
            ID.set(0L);
        }
        return ID.incrementAndGet();
    }
}
