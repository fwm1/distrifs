package com.lf.distrifs.core.raft;

import com.lf.distrifs.common.NamedThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class RaftExecutors {

    public static final ScheduledExecutorService RAFT_TIMER_EXECUTOR = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2,
            new NamedThreadFactory("Raft-timer"));
}
