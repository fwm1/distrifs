package com.lf.distrifs.core.grpc.base;

import com.lf.distrifs.core.grpc.auto.GrpcProto;
import com.lf.distrifs.core.grpc.auto.RequestGrpc;
import com.lf.distrifs.core.grpc.connect.ConnectionManager;
import com.lf.distrifs.core.grpc.connect.GrpcConnection;
import com.lf.distrifs.core.grpc.request.Request;
import com.lf.distrifs.core.grpc.request.RequestMeta;
import com.lf.distrifs.core.grpc.response.ErrorResponse;
import com.lf.distrifs.core.grpc.response.Response;
import com.lf.distrifs.util.GrpcUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.lf.distrifs.core.grpc.GrpcServer.CONTEXT_KEY_CONN_ID;

@Component
public class GrpcRequestAcceptor extends RequestGrpc.RequestImplBase {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Resource
    private RequestHandlerRegistry registry;

    @Resource
    private ConnectionManager connectionManager;

    @Override
    public void request(GrpcProto.Payload request, StreamObserver<GrpcProto.Payload> responseObserver) {
        String type = request.getMetadata().getType();
        BaseRequestHandler requestHandler = registry.getHandler(type);

        if (requestHandler == null) {
            GrpcProto.Payload payloadResponse = GrpcUtils.convert(ErrorResponse.build(String.format("No matched handler for type [%s]", type)));
            responseObserver.onNext(payloadResponse);
            responseObserver.onCompleted();
            return;
        }

        String connectionId = CONTEXT_KEY_CONN_ID.get();
        boolean valid = connectionManager.isValid(connectionId);
        if (!valid) {
            LOGGER.warn("Invalid connection Id [{}]", connectionId);
            GrpcProto.Payload payloadResponse = GrpcUtils
                    .convert(ErrorResponse.build("Connection is unregistered."));
            responseObserver.onNext(payloadResponse);
            responseObserver.onCompleted();
            return;
        }


        Object parseObj;
        try {
            parseObj = GrpcUtils.parse(request);
        } catch (Exception e) {
            GrpcProto.Payload payloadResponse = GrpcUtils.convert(ErrorResponse.build(e.getMessage()));
            responseObserver.onNext(payloadResponse);
            responseObserver.onCompleted();
            return;
        }

        if (!(parseObj instanceof Request)) {
            LOGGER.warn("Parsed payload is not a request,connectionId={}, parseObj={}", connectionId, parseObj);
            GrpcProto.Payload payloadResponse = GrpcUtils.convert(ErrorResponse.build("Invalid request"));
            responseObserver.onNext(payloadResponse);
            responseObserver.onCompleted();
            return;
        }

        Request realRequest = (Request) parseObj;
        try {
            GrpcConnection connection = connectionManager.getConnection(connectionId);
            connectionManager.refreshActiveTime(connectionId);
            RequestMeta requestMeta = new RequestMeta();
            requestMeta.setConnectionId(connectionId);
            requestMeta.setTags(connection.getLabels());
            requestMeta.setClientIp(connection.getMeta().getClientIp());
            Response response = requestHandler.handle(realRequest, requestMeta);
            GrpcProto.Payload payloadResponse = GrpcUtils.convert(response);
            responseObserver.onNext(payloadResponse);
            responseObserver.onCompleted();
        } catch (Throwable e) {
            LOGGER.error("Fail to handle request of connection [{}] ", connectionId, e);
            GrpcProto.Payload payloadResponse = GrpcUtils.convert(ErrorResponse.build(e.getMessage()));
            responseObserver.onNext(payloadResponse);
            responseObserver.onCompleted();
        }
    }
}
