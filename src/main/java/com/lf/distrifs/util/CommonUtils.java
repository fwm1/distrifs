package com.lf.distrifs.util;

import com.google.common.base.Strings;

import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;

public class CommonUtils {

    private static final Random RANDOM = new Random();

    public static long nextRandomLong(long start, long end) {
        checkArgument(start >= 0 && end >= start, "Illegal params");
        long period = end - start;
        if (period == 0) {
            return start;
        }
        return (long) (start + (period * RANDOM.nextDouble()));
    }

    public static String getAddress(String ip, int port) {
        checkArgument(!Strings.isNullOrEmpty(ip) && port > 0, String.format("Illegal ip or port, ip=%s, port=%s", ip, port));
        return ip + ":" + port;
    }
}
