package com.lf.distrifs.core.grpc.base;

import com.lf.distrifs.core.grpc.auto.BiRequestStreamGrpc;
import com.lf.distrifs.core.grpc.auto.GrpcProto;
import com.lf.distrifs.core.grpc.connect.ConnectionManager;
import com.lf.distrifs.core.grpc.connect.ConnectionMeta;
import com.lf.distrifs.core.grpc.connect.GrpcConnection;
import com.lf.distrifs.core.grpc.request.ConnectionResetRequest;
import com.lf.distrifs.core.grpc.request.ConnectionSetupRequest;
import com.lf.distrifs.core.grpc.response.Response;
import com.lf.distrifs.util.GrpcUtils;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.lf.distrifs.core.grpc.GrpcServer.*;

@Component
public class GrpcBiStreamRequestAcceptor extends BiRequestStreamGrpc.BiRequestStreamImplBase {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Resource
    private RequestHandlerRegistry registry;

    @Resource
    private ConnectionManager connectionManager;


    @Override
    public StreamObserver<GrpcProto.Payload> requestBiStream(StreamObserver<GrpcProto.Payload> responseObserver) {

        StreamObserver<GrpcProto.Payload> streamObserver = new StreamObserver<GrpcProto.Payload>() {

            final String connectionId = CONTEXT_KEY_CONN_ID.get();
            final String remoteIp = CONTEXT_KEY_CONN_REMOTE_IP.get();
            final int remotePort = CONTEXT_KEY_CONN_REMOTE_PORT.get();
            final int localPort = CONTEXT_KEY_CONN_LOCAL_PORT.get();
            String clientIp = "";

            @Override
            public void onNext(GrpcProto.Payload grpcPayload) {
                clientIp = grpcPayload.getMetadata().getClientIp();
                Object parseObj;
                try {
                    parseObj = GrpcUtils.parse(grpcPayload);
                } catch (Throwable throwable) {
                    LOGGER.warn("[{}]Grpc request bi stream,payload parse error", connectionId, throwable);
                    return;
                }

                if (parseObj == null) {
                    LOGGER.warn("[{}]Grpc request bi stream,payload parse null ,body={},meta={}", connectionId,
                            grpcPayload.getBody().getValue().toStringUtf8(), grpcPayload.getMetadata());
                    return;
                }

                if (parseObj instanceof ConnectionSetupRequest) {

                    ConnectionMeta connectionMeta = new ConnectionMeta(grpcPayload.getMetadata().getClientIp(), remoteIp,
                            remotePort, localPort, connectionId);
                    GrpcConnection connection = new GrpcConnection(connectionMeta, responseObserver, CONTEXT_KEY_CHANNEL.get());
                    boolean registerSuccess = connectionManager.register(connectionId, connection);
                    if (!registerSuccess) {
                        try {
                            connection.request(new ConnectionResetRequest(), 3000L);
                            connection.close();
                        } catch (Exception ignore) {
                            //ignore
                        }
                    }
                } else if (parseObj instanceof Response) {
                    Response response = (Response) parseObj;
                    // ack notify
                    AsyncRpcAckCenter.ackNotify(connectionId, response);
                    connectionManager.refreshActiveTime(connectionId);
                } else {
                    LOGGER.warn("[{}]Grpc request bi stream,unknown payload receive ,parseObj={}", connectionId, parseObj);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (responseObserver instanceof ServerCallStreamObserver) {
                    ServerCallStreamObserver serverCallStreamObserver = ((ServerCallStreamObserver) responseObserver);
                    if (!serverCallStreamObserver.isCancelled()) {
                        //client close the stream.
                    } else {
                        try {
                            serverCallStreamObserver.onCompleted();
                        } catch (Throwable t) {
                            //ignore
                        }
                    }
                }
            }

            @Override
            public void onCompleted() {
                if (responseObserver instanceof ServerCallStreamObserver) {
                    ServerCallStreamObserver serverCallStreamObserver = ((ServerCallStreamObserver) responseObserver);
                    if (serverCallStreamObserver.isCancelled()) {
                        //client close the stream.
                    } else {
                        try {
                            serverCallStreamObserver.onCompleted();
                        } catch (Throwable throwable) {
                            //ignore
                        }

                    }
                }
            }
        };
        return streamObserver;
    }
}
