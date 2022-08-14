package com.lf.distrifs.common;

import java.util.concurrent.TimeUnit;

public final class Constants {

    public static final int DETAIL_PORT = 8089;

    public static final long LEADER_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(15L);

    public static final long HEARTBEAT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(2L);

    public static final long RANDOM_MS = TimeUnit.SECONDS.toMillis(5L);

    public static final long DEFAULT_RPC_TIMEOUT = TimeUnit.SECONDS.toMillis(5L);

    public static final long LEADER_ELECTION_TICK = TimeUnit.MILLISECONDS.toMillis(500L);

    public static final long HEARTBEAT_TICK = TimeUnit.MILLISECONDS.toMillis(2000L);
}
