package com.lf.distrifs.core.grpc.client;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.lf.distrifs.common.IpAndPort;
import com.lf.distrifs.core.grpc.auto.GrpcProto;
import com.lf.distrifs.core.grpc.auto.RequestGrpc;
import com.lf.distrifs.core.grpc.connect.Requester;
import com.lf.distrifs.core.grpc.request.Request;
import com.lf.distrifs.core.grpc.request.RequestCallback;
import com.lf.distrifs.core.grpc.request.RequestFuture;
import com.lf.distrifs.core.grpc.response.ErrorResponse;
import com.lf.distrifs.core.grpc.response.Response;
import com.lf.distrifs.util.GrpcUtils;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Data
public class GrpcConnection implements Requester {

    String connectionId;

    IpAndPort ipAndPort;

    ManagedChannel channel;

    Executor executor;

    RequestGrpc.RequestFutureStub grpcFutureServiceStub;

    StreamObserver<GrpcProto.Payload> payloadStreamObserver;

    public GrpcConnection(IpAndPort ipAndPort, Executor executor) {
        this.ipAndPort = ipAndPort;
        this.executor = executor;
    }

    public void sendResponse(Response response) {
        GrpcProto.Payload convert = GrpcUtils.convert(response);
        payloadStreamObserver.onNext(convert);
    }

    public void sendRequest(Request request) {
        GrpcProto.Payload convert = GrpcUtils.convert(request);
        payloadStreamObserver.onNext(convert);
    }

    @Override
    public Response request(Request request, long timeoutMills) throws Exception {
        GrpcProto.Payload payload = GrpcUtils.convert(request);
        ListenableFuture<GrpcProto.Payload> requestFuture = grpcFutureServiceStub.request(payload);
        GrpcProto.Payload grpcResponse = requestFuture.get(timeoutMills, TimeUnit.MILLISECONDS);
        return (Response) GrpcUtils.parse(grpcResponse);
    }

    @Override
    public RequestFuture requestFuture(Request request) throws Exception {
        GrpcProto.Payload payload = GrpcUtils.convert(request);
        ListenableFuture<GrpcProto.Payload> guavaFuture = grpcFutureServiceStub.request(payload);
        return new RequestFuture() {
            @Override
            public boolean isDone() {
                return guavaFuture.isDone();
            }

            @Override
            public Response get() throws Exception {
                GrpcProto.Payload responsePayload = guavaFuture.get();
                Response response = (Response) GrpcUtils.parse(responsePayload);
                if (response instanceof ErrorResponse) {
                    throw new RuntimeException(response.getMsg());
                }
                return response;
            }

            @Override
            public Response get(long timeout) throws Exception {
                GrpcProto.Payload responsePayload = guavaFuture.get(timeout, TimeUnit.MILLISECONDS);
                Response response = (Response) GrpcUtils.parse(responsePayload);
                if (response instanceof ErrorResponse) {
                    throw new RuntimeException(response.getMsg());
                }
                return response;
            }
        };
    }

    @Override
    public void asyncRequest(Request request, RequestCallback<Response> callback) throws Exception {
        GrpcProto.Payload grpcRequest = GrpcUtils.convert(request);
        ListenableFuture<GrpcProto.Payload> guavaFuture = grpcFutureServiceStub.request(grpcRequest);

        Futures.addCallback(guavaFuture, new FutureCallback<GrpcProto.Payload>() {
            @Override
            public void onSuccess(@NullableDecl GrpcProto.Payload payload) {
                Response response = (Response) GrpcUtils.parse(payload);
                if (response != null) {
                    if (response instanceof ErrorResponse) {
                        callback.onException(new RuntimeException(response.getMsg()));
                    } else {
                        callback.onResponse(response);
                    }
                } else {
                    callback.onException(new RuntimeException("response is null"));
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                if (throwable instanceof CancellationException) {
                    callback.onException(
                            new TimeoutException("Timeout after " + callback.getTimeout() + " milliseconds."));
                } else {
                    callback.onException(throwable);
                }
            }
        }, callback.getExecutor() != null ? callback.getExecutor() : this.executor);
    }

    @Override
    public void close() {
        if (this.payloadStreamObserver != null) {
            try {
                payloadStreamObserver.onCompleted();
            } catch (Throwable throwable) {
                //ignore.
            }
        }

        if (this.channel != null && !channel.isShutdown()) {
            try {
                this.channel.shutdownNow();
            } catch (Throwable throwable) {
                //ignore.
            }
        }
    }
}
