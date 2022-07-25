package com.lf.distrifs.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    private final AtomicInteger id = new AtomicInteger(0);

    private String name;

    public NamedThreadFactory(String name) {
        if (!name.endsWith("-")) {
            name += "-";
        }
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, name + id.getAndIncrement());
        t.setDaemon(true);
        return t;
    }
}
