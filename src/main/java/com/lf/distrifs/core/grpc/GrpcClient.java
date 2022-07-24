package com.lf.distrifs.core.grpc;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lf.distrifs.common.packagescan.PackagePathProvider;
import com.lf.distrifs.common.IpAndPort;
import com.lf.distrifs.core.grpc.auto.BiRequestStreamGrpc;
import com.lf.distrifs.core.grpc.auto.GrpcProto;
import com.lf.distrifs.core.grpc.auto.RequestGrpc;
import com.lf.distrifs.core.grpc.base.PayloadRegistry;
import com.lf.distrifs.core.grpc.client.GrpcConnection;
import com.lf.distrifs.core.grpc.client.RpcClientStatus;
import com.lf.distrifs.core.grpc.client.ServerRequestHandler;
import com.lf.distrifs.core.grpc.common.ServerListFactory;
import com.lf.distrifs.core.grpc.request.*;
import com.lf.distrifs.core.grpc.response.*;
import com.lf.distrifs.util.CommonUtils;
import com.lf.distrifs.util.GrpcUtils;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
@Data
public class GrpcClient {

    private String name;

    private ThreadPoolExecutor grpcExecutor;

    private GrpcConnection grpcConnection;

    private long keepAliveTime = 5000L;

    private long lastActiveTimeStamp = System.currentTimeMillis();

    private List<ServerRequestHandler> serverRequestHandlers = new ArrayList<>();

    private AtomicReference<RpcClientStatus> clientStatus = new AtomicReference<>(RpcClientStatus.WAIT_INIT);

    private ServerListFactory serverListFactory;

    private ExecutorService clientEventExecutor;

    private List<ConnectEventListener> connectEventListeners = new ArrayList<>();

    private LinkedBlockingQueue<ConnectionType> eventBlockingQueue = new LinkedBlockingQueue<>();

    static {
        PayloadRegistry.scan(new PackagePathProvider() {
            @Override
            public Set<String> getPackagePaths() {
                return ImmutableSet.of(
                        "com.lf.distrifs.core.grpc.request",
                        "com.lf.distrifs.core.grpc.response",
                        "com.lf.distrifs.core.raft.request",
                        "com.lf.distrifs.core.raft.response"
                );
            }
        });
    }


    public GrpcClient(String name, ServerListFactory serverListFactory) {
        checkArgument(name != null);
        checkArgument(serverListFactory != null);

        this.name = name;
        this.serverListFactory = serverListFactory;
        if (clientStatus.compareAndSet(RpcClientStatus.WAIT_INIT, RpcClientStatus.INITIALIZED)) {
            log.info("[{}]RpcClient init in constructor, ServerListFactory = {}", name, serverListFactory.getClass().getName());
        }
    }

    public void start() {

        if (!clientStatus.compareAndSet(RpcClientStatus.INITIALIZED, RpcClientStatus.STARTING)) {
            log.warn("[{}]RpcClient has start started, no need to start again", name);
            return;
        }

        initEventProcessor();
        initServerRequestHandler();

        IpAndPort serverAddress = resolveAddress(serverListFactory.genNextServer());
        log.info("[{}]Try to connect server {}", name, serverAddress);
        GrpcConnection grpcConnection = connectTo(serverAddress);
        if (grpcConnection == null) {
            int maxTry = 3;
            while (maxTry-- > 0 && grpcConnection == null) {
                grpcConnection = connectTo(serverAddress);
                log.warn("[{}]Try to connect server failed, retry after 5 seconds", name);
                CommonUtils.threadSleep(5000L);
            }
        }

        if (grpcConnection != null) {
            log.info("[{}]Success to connect to server [{}], connectionId = {}", name, grpcConnection.getIpAndPort(), grpcConnection.getConnectionId());
            this.grpcConnection = grpcConnection;
            this.clientStatus.set(RpcClientStatus.RUNNING);
        } else {
            //todo async switch server
        }
    }

    public void loop() {
        //just for test
        while (true) {
            healthCheck();
            try {
                Thread.sleep(2000L);
            } catch (Exception e) {
            }
        }
    }

    public Response request(Request request, long timeoutMills) throws Exception {
        try {
            Response response = this.grpcConnection.request(request, timeoutMills);
            if (response == null) {
                throw new RuntimeException("Unknown Exception");
            }
            if (response instanceof ErrorResponse) {
                throw new RuntimeException(response.getMsg());
            }
            lastActiveTimeStamp = System.currentTimeMillis();
            return response;
        } catch (Exception e) {
            log.warn("[{}]Send request fail, request = {}, errorMsg = {}", name, request, e.getMessage());
        }
        if (clientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
            // todo async switch server
        }
        throw new RuntimeException();
    }

    public void asyncRequest(Request request, RequestCallback<Response> callback) {
        try {
            this.grpcConnection.asyncRequest(request, callback);
            return;
        } catch (Exception e) {
            log.warn("[{}]Send async request fail, request = {}, errorMsg = {}", name, request, e.getMessage());
        }
        if (clientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
            // todo async switch server
        }
        throw new RuntimeException();
    }

    public RequestFuture requestFuture(Request request) {
        try {
            return this.grpcConnection.requestFuture(request);
        } catch (Exception e) {
            log.warn("[{}]Send async request fail, request = {}, errorMsg = {}", name, request, e.getMessage());
        }
        if (clientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
            // todo async switch server
        }
        throw new RuntimeException();
    }

    private IpAndPort resolveAddress(String serverAddress) {
        String[] split = serverAddress.split(":");
        return new IpAndPort(split[0], Integer.parseInt(split[1]));
    }

    private void initEventProcessor() {
        if (clientEventExecutor == null) {
            clientEventExecutor = new ThreadPoolExecutor(2, 2, 0, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(100), r -> {
                Thread t = new Thread(r);
                t.setName("com.lf.distrifs.client.remote.worker");
                t.setDaemon(true);
                return t;
            });
        }

        // 开启连接事件的处理线程
        clientEventExecutor.submit(() -> {
            while (!clientEventExecutor.isShutdown() && !clientEventExecutor.isTerminated()) {
                ConnectionType connectionType;
                try {
                    connectionType = eventBlockingQueue.take();
                    for (ConnectEventListener listener : connectEventListeners) {
                        try {
                            if (ConnectionType.CONNECT == connectionType) {
                                listener.onConnect();
                            } else if (ConnectionType.DIS_CONNECT == connectionType) {
                                listener.onDisconnect();
                            }
                        } catch (Exception e) {

                        }
                    }

                } catch (Exception e) {

                }
            }
        });
    }


    private void initServerRequestHandler() {
        // 注册connection reset处理器，连接重置时切换server重连
        registerServerRequestHandler(new ServerRequestHandler() {
            @Override
            public Response requestReply(Request request) {
                if (request instanceof ConnectionResetRequest) {
                    try {
                        synchronized (GrpcClient.this) {
                            // todo async switch server
                        }
                        return new ConnectionResetResponse();
                    } catch (Exception e) {
                        log.error("[{}]Switch server error, {}", name, e);
                    }
                }
                return null;
            }
        });

        // server会向client发送alive detect请求
        registerServerRequestHandler(new ServerRequestHandler() {
            @Override
            public Response requestReply(Request request) {
                if (request instanceof AliveDetectRequest) {
                    return new AliveDetectResponse();
                }
                return null;
            }
        });
    }


    public GrpcConnection connectTo(IpAndPort ipAndPort) {
        try {
            if (grpcExecutor == null) {
                grpcExecutor = createGrpcExecutor(ipAndPort);
            }

            ManagedChannel channel = createNewChannel(ipAndPort);
            RequestGrpc.RequestFutureStub requestFutureStub = RequestGrpc.newFutureStub(channel);

            Response serverCheck = serverCheck(ipAndPort, requestFutureStub);
            if (!(serverCheck instanceof ServerCheckResponse)) {
                shutdownChannel(channel);
                return null;
            }

            BiRequestStreamGrpc.BiRequestStreamStub biRequestStreamStub = BiRequestStreamGrpc.newStub(requestFutureStub.getChannel());
            GrpcConnection grpcConnection = new GrpcConnection(ipAndPort, this.grpcExecutor);
            grpcConnection.setConnectionId(((ServerCheckResponse) serverCheck).getConnectionId());

            //create stream request and bind connection event to this connection
            StreamObserver<GrpcProto.Payload> streamObserver = bindRequestStream(biRequestStreamStub, grpcConnection);

            grpcConnection.setPayloadStreamObserver(streamObserver);
            grpcConnection.setGrpcFutureServiceStub(requestFutureStub);
            grpcConnection.setChannel(channel);
            grpcConnection.setIpAndPort(ipAndPort);


            //send a setup request
            ConnectionSetupRequest conSetupRequest = new ConnectionSetupRequest();
            grpcConnection.sendRequest(conSetupRequest);
            //wait to register connection setup
            Thread.sleep(200L);
            return grpcConnection;
        } catch (Exception e) {
            log.error("Failed to connect to {}", ipAndPort);
        }
        return null;
    }


    public void registerServerRequestHandler(ServerRequestHandler serverRequestHandler) {
        this.serverRequestHandlers.add(serverRequestHandler);
        log.info("[{}] Register server request handler:{}", name, serverRequestHandler.getClass().getName());
    }

    public void registerConnectEventListener(ConnectEventListener listener) {
        this.connectEventListeners.add(listener);
        log.info("[{}] Register connect event listener:{}", name, listener.getClass().getName());
    }






    /*===================================== private methods ===========================================*/


    private StreamObserver<GrpcProto.Payload> bindRequestStream(BiRequestStreamGrpc.BiRequestStreamStub streamStub, GrpcConnection grpcConnection) {
        return streamStub.requestBiStream(new StreamObserver<GrpcProto.Payload>() {
            @Override
            public void onNext(GrpcProto.Payload payload) {
                try {
                    final Request request = (Request) GrpcUtils.parse(payload);
                    if (request != null) {
                        try {
                            Response response = handleServerRequest(request);
                            if (response != null) {
                                response.setRequestId(request.getRequestId());
                                try {
                                    grpcConnection.sendResponse(response);
                                } catch (Exception e) {
                                    log.error("[{}]Error to send ack response, ackId->{}", grpcConnection.getConnectionId(),
                                            response.getRequestId());
                                }
                            } else {
                                log.warn("[{}]Failed to process server request, ackId->{}", grpcConnection.getConnectionId(), request.getRequestId());
                            }

                        } catch (Exception e) {
                            log.error("[{}]Handle server request exception: {}", grpcConnection.getConnectionId(), e.getMessage());
                            Response errResponse = ErrorResponse
                                    .build("Handle server request error");
                            errResponse.setRequestId(request.getRequestId());
                            try {
                                grpcConnection.sendResponse(errResponse);
                            } catch (Exception ex) {
                                log.error("[{}]Error to send ack response, ackId->{}", grpcConnection.getConnectionId(),
                                        errResponse.getRequestId());
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error("[{}]Error to process server push response: {}", grpcConnection.getConnectionId(), payload.getBody().getValue().toStringUtf8());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                //todo async switch server
            }

            @Override
            public void onCompleted() {

            }
        });
    }

    private Response handleServerRequest(final Request request) {

        log.info("[{}] Receive server request, request = {}, requestId = {}", name, request.getClass().getSimpleName(), request.getRequestId());

        lastActiveTimeStamp = System.currentTimeMillis();
        for (ServerRequestHandler serverRequestHandler : serverRequestHandlers) {
            try {
                Response response = serverRequestHandler.requestReply(request);
                if (response != null) {
                    log.info("[{}] Ack server request, request = {}, requestId = {}", name, request.getClass().getSimpleName(), request.getRequestId());
                    return response;
                }
            } catch (Exception e) {
                log.error("[{}] HandleServerRequest:{}, errorMessage = {}", name, request.getClass().getSimpleName(), e.getMessage());
            }

        }
        return null;
    }

    private Response serverCheck(IpAndPort ipAndPort, RequestGrpc.RequestFutureStub requestFutureStub) {

        try {
            if (requestFutureStub == null) {
                return null;
            }
            ServerCheckRequest serverCheckRequest = new ServerCheckRequest();
            GrpcProto.Payload payload = GrpcUtils.convert(serverCheckRequest);
            ListenableFuture<GrpcProto.Payload> request = requestFutureStub.request(payload);
            GrpcProto.Payload response = request.get(3000L, TimeUnit.MILLISECONDS);
            return (Response) GrpcUtils.parse(response);
        } catch (Exception e) {
            log.error("Failed to check server {}", ipAndPort, e);
        }
        return null;
    }

    private void asyncSwitchServer() {
        IpAndPort newServer = resolveAddress(serverListFactory.genNextServer());
        if (Objects.equals(newServer, grpcConnection.getIpAndPort()) && healthCheck()) {
            log.info("[{}]Switch to the same server = {}", name, newServer);
            return;
        }
        log.info("[{}}]Try to switch to a new server = {}", name, newServer);
        GrpcConnection newConnection = connectTo(newServer);
        if (newConnection != null) {
            log.info("[{}]Connect to new server = {}, connectionId = {}", name, newServer, newConnection.getConnectionId());
            if (grpcConnection != null) {
                log.info("[{}]Close old connection, server = {}, connectionId = {}", name, grpcConnection.getIpAndPort(), grpcConnection.getConnectionId());
                grpcConnection.close();
            }
            grpcConnection = newConnection;
            clientStatus.set(RpcClientStatus.RUNNING);
            eventBlockingQueue.add(ConnectionType.CONNECT);
        }
    }

    private boolean healthCheck() {
        AliveDetectRequest healthCheckRequest = new AliveDetectRequest();
        if (this.grpcConnection == null) {
            return false;
        }
        try {
            Response response = this.grpcConnection.request(healthCheckRequest, 3000L);
            if (response != null && response.isSuccess()) {
                log.info("[{}]Receive alive detect response from [{}], connectionId=  {}", name, grpcConnection.getIpAndPort(), grpcConnection.getConnectionId());
                return true;
            } else {
                log.warn("[{}]Receive error detect response from [{}], connectionId = {}", name, grpcConnection.getIpAndPort(), grpcConnection.getConnectionId());
            }
        } catch (Exception e) {

        }
        return false;
    }

    private ManagedChannel createNewChannel(IpAndPort ipAndPort) {
        return ManagedChannelBuilder.forAddress(ipAndPort.getIp(), ipAndPort.getPort())
                .executor(grpcExecutor)
                .compressorRegistry(CompressorRegistry.getDefaultInstance())
                .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
                .usePlaintext()
                .build();
    }

    private ThreadPoolExecutor createGrpcExecutor(IpAndPort ipAndPort) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 4,
                10L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000),
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("grpc-client-executor-" + ipAndPort.getIp() + ":" + ipAndPort.getPort() + "-%d")
                        .build());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }


    private void shutdownChannel(ManagedChannel channel) {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
        }
    }

    public enum ConnectionType {
        CONNECT,
        DIS_CONNECT;
    }

    public interface ConnectEventListener {

        void onConnect();

        void onDisconnect();
    }
}
