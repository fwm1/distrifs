package com.lf.distrifs.core.grpc.auto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.42.0)",
    comments = "Source: requestGrpc.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class RequestGrpc {

  private RequestGrpc() {}

  public static final String SERVICE_NAME = "distrifs.Request";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.lf.distrifs.core.grpc.auto.GrpcProto.Payload,
      com.lf.distrifs.core.grpc.auto.GrpcProto.Payload> getRequestMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "request",
      requestType = com.lf.distrifs.core.grpc.auto.GrpcProto.Payload.class,
      responseType = com.lf.distrifs.core.grpc.auto.GrpcProto.Payload.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.lf.distrifs.core.grpc.auto.GrpcProto.Payload,
      com.lf.distrifs.core.grpc.auto.GrpcProto.Payload> getRequestMethod() {
    io.grpc.MethodDescriptor<com.lf.distrifs.core.grpc.auto.GrpcProto.Payload, com.lf.distrifs.core.grpc.auto.GrpcProto.Payload> getRequestMethod;
    if ((getRequestMethod = RequestGrpc.getRequestMethod) == null) {
      synchronized (RequestGrpc.class) {
        if ((getRequestMethod = RequestGrpc.getRequestMethod) == null) {
          RequestGrpc.getRequestMethod = getRequestMethod =
              io.grpc.MethodDescriptor.<com.lf.distrifs.core.grpc.auto.GrpcProto.Payload, com.lf.distrifs.core.grpc.auto.GrpcProto.Payload>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "request"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.lf.distrifs.core.grpc.auto.GrpcProto.Payload.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.lf.distrifs.core.grpc.auto.GrpcProto.Payload.getDefaultInstance()))
              .setSchemaDescriptor(new RequestMethodDescriptorSupplier("request"))
              .build();
        }
      }
    }
    return getRequestMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RequestStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequestStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequestStub>() {
        @java.lang.Override
        public RequestStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequestStub(channel, callOptions);
        }
      };
    return RequestStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RequestBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequestBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequestBlockingStub>() {
        @java.lang.Override
        public RequestBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequestBlockingStub(channel, callOptions);
        }
      };
    return RequestBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RequestFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequestFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequestFutureStub>() {
        @java.lang.Override
        public RequestFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequestFutureStub(channel, callOptions);
        }
      };
    return RequestFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class RequestImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Sends a commonRequest
     * </pre>
     */
    public void request(com.lf.distrifs.core.grpc.auto.GrpcProto.Payload request,
        io.grpc.stub.StreamObserver<com.lf.distrifs.core.grpc.auto.GrpcProto.Payload> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRequestMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRequestMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                com.lf.distrifs.core.grpc.auto.GrpcProto.Payload,
                com.lf.distrifs.core.grpc.auto.GrpcProto.Payload>(
                  this, METHODID_REQUEST)))
          .build();
    }
  }

  /**
   */
  public static final class RequestStub extends io.grpc.stub.AbstractAsyncStub<RequestStub> {
    private RequestStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RequestStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequestStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sends a commonRequest
     * </pre>
     */
    public void request(com.lf.distrifs.core.grpc.auto.GrpcProto.Payload request,
        io.grpc.stub.StreamObserver<com.lf.distrifs.core.grpc.auto.GrpcProto.Payload> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRequestMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class RequestBlockingStub extends io.grpc.stub.AbstractBlockingStub<RequestBlockingStub> {
    private RequestBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RequestBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequestBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sends a commonRequest
     * </pre>
     */
    public com.lf.distrifs.core.grpc.auto.GrpcProto.Payload request(com.lf.distrifs.core.grpc.auto.GrpcProto.Payload request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRequestMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class RequestFutureStub extends io.grpc.stub.AbstractFutureStub<RequestFutureStub> {
    private RequestFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RequestFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequestFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sends a commonRequest
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.lf.distrifs.core.grpc.auto.GrpcProto.Payload> request(
        com.lf.distrifs.core.grpc.auto.GrpcProto.Payload request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRequestMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REQUEST = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final RequestImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(RequestImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REQUEST:
          serviceImpl.request((com.lf.distrifs.core.grpc.auto.GrpcProto.Payload) request,
              (io.grpc.stub.StreamObserver<com.lf.distrifs.core.grpc.auto.GrpcProto.Payload>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class RequestBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    RequestBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.lf.distrifs.core.grpc.auto.GrpcProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Request");
    }
  }

  private static final class RequestFileDescriptorSupplier
      extends RequestBaseDescriptorSupplier {
    RequestFileDescriptorSupplier() {}
  }

  private static final class RequestMethodDescriptorSupplier
      extends RequestBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    RequestMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (RequestGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new RequestFileDescriptorSupplier())
              .addMethod(getRequestMethod())
              .build();
        }
      }
    }
    return result;
  }
}
