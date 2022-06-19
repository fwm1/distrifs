package com.lf.distrifs.core.grpc.request;

import com.lf.distrifs.core.grpc.common.RpcExecutors;
import com.lf.distrifs.core.grpc.response.Response;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultRequestFuture implements RequestFuture {

    private volatile long timestamp;
    private volatile boolean isDone = false;
    private volatile boolean isSuccess = false;
    private RequestCallback requestCallback;
    private Exception exception;
    private Response response;
    private String connectionId;
    private ScheduledFuture timeoutFuture;
    private String requestId;
    private CallbackOnTimeout callbackOnTimeout;

    public DefaultRequestFuture(String connectionId, String requestId, RequestCallback requestCallback, CallbackOnTimeout callbackOnTimeout) {
        this.requestCallback = requestCallback;
        this.connectionId = connectionId;
        this.requestId = requestId;
        this.timestamp = System.currentTimeMillis();
        this.callbackOnTimeout = callbackOnTimeout;
        if (requestCallback != null) {
            this.timeoutFuture = RpcExecutors.TIMEOUT_SCHEDULER.schedule(new Runnable() {
                @Override
                public void run() {
                    setFailResult(new TimeoutException("Async request timeout"));
                    if (callbackOnTimeout != null) {
                        callbackOnTimeout.onTimeout();
                    }
                }
            }, requestCallback.getTimeout(), TimeUnit.MILLISECONDS);
        }

    }

    public void setResponse(final Response response) {
        isDone = true;
        this.response = response;
        this.isSuccess = response.isSuccess();
        if (this.timeoutFuture != null) {
            this.timeoutFuture.cancel(true);
        }
        synchronized (this) {
            notifyAll();
        }
        onCallback();
    }

    public void setFailResult(Exception e) {
        isDone = true;
        isSuccess = false;
        exception = e;
        synchronized (this) {
            notifyAll();
        }
        onCallback();
    }

    private void onCallback() {
        if (requestCallback != null) {
            Runnable callback = new Runnable() {
                @Override
                public void run() {
                    if (exception != null) {
                        requestCallback.onException(exception);
                    } else {
                        requestCallback.onResponse(response);
                    }
                }
            };
            if (requestCallback.getExecutor() != null) {
                requestCallback.getExecutor().execute(callback);
            } else {
                callback.run();
            }
        }
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public Response get() throws Exception {
        synchronized (this) {
            while (!isDone) {
                wait();
            }
        }
        return response;
    }

    @Override
    public Response get(long timeout) throws Exception {
        if (timeout < 0) {
            synchronized (this) {
                while (!isDone) {
                    wait();
                }
            }
        } else if (timeout > 0) {
            long end = System.currentTimeMillis() + timeout;
            long waitTime = timeout;
            synchronized (this) {
                while (!isDone && waitTime > 0) {
                    wait(waitTime);
                    waitTime = end - System.currentTimeMillis();
                }
            }
        }

        if (isDone) {
            return response;
        } else {
            if (callbackOnTimeout != null) {
                callbackOnTimeout.onTimeout();
            }
            throw new TimeoutException("Async request timeout");
        }
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getRequestId() {
        return requestId;
    }


    public interface CallbackOnTimeout {
        void onTimeout();
    }
}
