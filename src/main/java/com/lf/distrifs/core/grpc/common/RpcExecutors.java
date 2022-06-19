package com.lf.distrifs.core.grpc.common;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class RpcExecutors {

    public static final ScheduledThreadPoolExecutor TIMEOUT_SCHEDULER = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "rpc_timeout_scheduler");
        }
    });

    public static final ScheduledThreadPoolExecutor COMMON_SCHEDULER = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "rpc_common_scheduler");
        }
    });
}
