package com.lf.distrifs.core.grpc;

import com.google.common.base.Strings;
import com.lf.distrifs.common.Constants;
import com.lf.distrifs.core.grpc.auto.BiRequestStreamGrpc;
import com.lf.distrifs.core.grpc.auto.GrpcProto;
import com.lf.distrifs.core.grpc.auto.RequestGrpc;
import com.lf.distrifs.core.grpc.base.GrpcBiStreamRequestAcceptor;
import com.lf.distrifs.core.grpc.base.GrpcRequestAcceptor;
import com.lf.distrifs.core.grpc.connect.ConnectionManager;
import com.lf.distrifs.util.NetUtils;
import io.grpc.*;
import io.grpc.netty.shaded.io.netty.channel.Channel;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import io.grpc.util.MutableHandlerRegistry;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;

@Service
public class GrpcServer {

    private Server server;

    @Resource
    private GrpcRequestAcceptor grpcRequestAcceptor;

    @Resource
    private GrpcBiStreamRequestAcceptor grpcBiStreamRequestAcceptor;

    @Resource
    private ConnectionManager connectionManager;


    @PostConstruct
    public void start() throws IOException {
        final MutableHandlerRegistry handlerRegistry = new MutableHandlerRegistry();

        ServerInterceptor serverInterceptor = new ServerInterceptor() {
            @SneakyThrows
            @Override
            public <Req, Resp> ServerCall.Listener<Req> interceptCall(ServerCall<Req, Resp> call, Metadata headers, ServerCallHandler<Req, Resp> next) {
                Context ctx = Context.current()
                        .withValue(CONTEXT_KEY_CONN_ID, call.getAttributes().get(TRANS_KEY_CONN_ID))
                        .withValue(CONTEXT_KEY_CONN_REMOTE_IP, call.getAttributes().get(TRANS_KEY_REMOTE_IP))
                        .withValue(CONTEXT_KEY_CONN_REMOTE_PORT, call.getAttributes().get(TRANS_KEY_REMOTE_PORT))
                        .withValue(CONTEXT_KEY_CONN_LOCAL_PORT, call.getAttributes().get(TRANS_KEY_LOCAL_PORT));

//                if (BiRequestStreamGrpc.SERVICE_NAME.equals(call.getMethodDescriptor().getServiceName())) {
//                    Channel internalChannel = getInternalChannel(call);
//                    ctx = ctx.withValue(CONTEXT_KEY_CHANNEL, internalChannel);
//                }
                return Contexts.interceptCall(ctx, call, headers, next);
            }
        };

        addServices(handlerRegistry, serverInterceptor);


        server = ServerBuilder.forPort(NetUtils.LOCAL_PORT)
                .fallbackHandlerRegistry(handlerRegistry)
                .compressorRegistry(CompressorRegistry.getDefaultInstance())
                .decompressorRegistry(DecompressorRegistry.emptyInstance())
                .addTransportFilter(new ServerTransportFilter() {
                    @Override
                    public Attributes transportReady(Attributes transportAttrs) {
                        // append attrs
                        InetSocketAddress remoteAddress = ((InetSocketAddress) transportAttrs.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR));
                        InetSocketAddress localAddress = ((InetSocketAddress) transportAttrs.get(Grpc.TRANSPORT_ATTR_LOCAL_ADDR));
                        int remotePort = remoteAddress.getPort();
                        int localPort = localAddress.getPort();
                        String remoteIp = remoteAddress.getAddress().getHostAddress();
                        return transportAttrs.toBuilder()
                                .set(TRANS_KEY_CONN_ID, System.currentTimeMillis() + "_" + remoteIp + "_" + remotePort)
                                .set(TRANS_KEY_REMOTE_IP, remoteIp)
                                .set(TRANS_KEY_REMOTE_PORT, remotePort)
                                .set(TRANS_KEY_LOCAL_PORT, localPort)
                                .build();
                    }

                    @Override
                    public void transportTerminated(Attributes transportAttrs) {
                        String connId = null;
                        try {
                            connId = transportAttrs.get(TRANS_KEY_CONN_ID);
                        } catch (Exception e) {

                        }
                        if (!Strings.isNullOrEmpty(connId)) {
                            connectionManager.unregister(connId);
                        }

                    }
                })
                .build();

        server.start();
    }

    private void addServices(MutableHandlerRegistry handlerRegistry, ServerInterceptor... serverInterceptor) {

        // unary common call register.
        final MethodDescriptor<GrpcProto.Payload, GrpcProto.Payload> unaryPayloadMethod = MethodDescriptor.<GrpcProto.Payload, GrpcProto.Payload>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName(RequestGrpc.SERVICE_NAME, "request"))
                .setRequestMarshaller(ProtoUtils.marshaller(GrpcProto.Payload.getDefaultInstance()))
                .setResponseMarshaller(ProtoUtils.marshaller(GrpcProto.Payload.getDefaultInstance())).build();

        final ServerCallHandler<GrpcProto.Payload, GrpcProto.Payload> payloadHandler = ServerCalls
                .asyncUnaryCall((request, responseObserver) -> grpcRequestAcceptor.request(request, responseObserver));

        final ServerServiceDefinition serviceDefOfUnaryPayload = ServerServiceDefinition.builder(RequestGrpc.SERVICE_NAME)
                .addMethod(unaryPayloadMethod, payloadHandler).build();
        handlerRegistry.addService(ServerInterceptors.intercept(serviceDefOfUnaryPayload, serverInterceptor));

        // bi stream register.
        final ServerCallHandler<GrpcProto.Payload, GrpcProto.Payload> biStreamHandler = ServerCalls.asyncBidiStreamingCall(
                (responseObserver) -> grpcBiStreamRequestAcceptor.requestBiStream(responseObserver));

        final MethodDescriptor<GrpcProto.Payload, GrpcProto.Payload> biStreamMethod = MethodDescriptor.<GrpcProto.Payload, GrpcProto.Payload>newBuilder()
                .setType(MethodDescriptor.MethodType.BIDI_STREAMING).setFullMethodName(MethodDescriptor
                        .generateFullMethodName(BiRequestStreamGrpc.SERVICE_NAME, "requestBiStream"))
                .setRequestMarshaller(ProtoUtils.marshaller(GrpcProto.Payload.newBuilder().build()))
                .setResponseMarshaller(ProtoUtils.marshaller(GrpcProto.Payload.getDefaultInstance())).build();

        final ServerServiceDefinition serviceDefOfBiStream = ServerServiceDefinition
                .builder(BiRequestStreamGrpc.SERVICE_NAME).addMethod(biStreamMethod, biStreamHandler).build();
        handlerRegistry.addService(ServerInterceptors.intercept(serviceDefOfBiStream, serverInterceptor));

    }

    private Channel getInternalChannel(ServerCall serverCall) throws IllegalAccessException {
        try {
            Field field = serverCall.getClass().getDeclaredField("stream");
            field.setAccessible(true);
            return (Channel) field.get(serverCall);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static final Attributes.Key<String> TRANS_KEY_CONN_ID = Attributes.Key.create("conn_id");

    static final Attributes.Key<String> TRANS_KEY_REMOTE_IP = Attributes.Key.create("remote_ip");

    static final Attributes.Key<Integer> TRANS_KEY_REMOTE_PORT = Attributes.Key.create("remote_port");

    static final Attributes.Key<Integer> TRANS_KEY_LOCAL_PORT = Attributes.Key.create("local_port");

    public static final Context.Key<String> CONTEXT_KEY_CONN_ID = Context.key("conn_id");

    public static final Context.Key<String> CONTEXT_KEY_CONN_REMOTE_IP = Context.key("remote_ip");

    public static final Context.Key<Integer> CONTEXT_KEY_CONN_REMOTE_PORT = Context.key("remote_port");

    public static final Context.Key<Integer> CONTEXT_KEY_CONN_LOCAL_PORT = Context.key("local_port");

    public static final Context.Key<Channel> CONTEXT_KEY_CHANNEL = Context.key("ctx_channel");
}
