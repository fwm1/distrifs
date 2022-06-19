package com.lf.distrifs.common.notify;

import java.util.List;
import java.util.concurrent.Executor;

public interface EventSubscriber<T extends Event> {

    void onEvent(T event);

    List<Class<? extends Event>> subscribeTypes();

    //自定义处理线程池
    default Executor executor() {
        return null;
    }

    //优先级
    default int prior() {
        return -1;
    }
}
