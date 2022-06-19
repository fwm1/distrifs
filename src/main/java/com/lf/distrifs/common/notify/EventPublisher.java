package com.lf.distrifs.common.notify;

public interface EventPublisher {
    void init(Class<? extends Event> eventType);

    void addSubscriber(EventSubscriber subscriber);

    void remoteSubscriber(EventSubscriber subscriber);

    void publish(Event event);

    void notifySubscriber(EventSubscriber subscriber, Event event);
}
