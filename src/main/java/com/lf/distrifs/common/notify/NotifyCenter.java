package com.lf.distrifs.common.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

public class NotifyCenter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyCenter.class);

    private static final NotifyCenter INSTANCE = new NotifyCenter();

    private final Map<String, EventPublisher> publisherMap = new ConcurrentHashMap<>(16);


    public static void registerSubscriber(final EventSubscriber subscriber) {
        for (Object type : subscriber.subscribeTypes()) {
            Class<? extends Event> subscribeType = (Class<? extends Event>) type;
            addSubscriberInternal(subscriber, subscribeType);
        }

    }

    public static EventPublisher registerPublisher(final Class<? extends Event> eventType) {
        EventPublisher eventPublisher = initDefaultPublisher(eventType);
        INSTANCE.publisherMap.putIfAbsent(eventType.getCanonicalName(), eventPublisher);
        return eventPublisher;
    }

    public static void deregisterPublisher(final Class<? extends Event> eventType) {
        String topic = eventType.getCanonicalName();
        INSTANCE.publisherMap.remove(topic);
    }

    /*---------------------------------private methods-----------------------------------------*/

    private static void addSubscriberInternal(final EventSubscriber subscriber, Class<? extends Event> subscribeType) {

        final String topic = subscribeType.getCanonicalName();
        EventPublisher publisher = INSTANCE.publisherMap.get(topic);
        if (publisher == null) {
            synchronized (NotifyCenter.class) {
                INSTANCE.publisherMap.putIfAbsent(topic, initDefaultPublisher(subscribeType));
                publisher = INSTANCE.publisherMap.get(topic);
            }
        }
        publisher.addSubscriber(subscriber);
    }

    private static EventPublisher initDefaultPublisher(final Class<? extends Event> eventType) {
        EventPublisher eventPublisher = new DefaultEventPublisher();
        eventPublisher.init(eventType);
        return eventPublisher;
    }


    static class DefaultEventPublisher extends Thread implements EventPublisher {

        Class<? extends Event> eventType;

        final ConcurrentSkipListSet<EventSubscriber> subscribers = new ConcurrentSkipListSet<>((o1, o2) -> o2.prior() - o1.prior());

        final BlockingQueue<Event> eventQueue = new ArrayBlockingQueue<>(1024);

        private volatile boolean initialized = false;

        @Override
        public void init(Class<? extends Event> eventType) {
            this.eventType = eventType;
            start();
        }

        @Override
        public void addSubscriber(EventSubscriber subscriber) {
            subscribers.add(subscriber);
        }

        @Override
        public void remoteSubscriber(EventSubscriber subscriber) {
            subscribers.remove(subscriber);
        }

        @Override
        public void publish(Event event) {
            boolean success = eventQueue.offer(event);
            if (!success) {
                receiveEvent(event);
            }
        }

        @Override
        public void notifySubscriber(EventSubscriber subscriber, Event event) {
            final Runnable runnable = () -> subscriber.onEvent(event);
            Executor executor = subscriber.executor();
            if (executor != null) {
                executor.execute(runnable);
            } else {
                runnable.run();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Event event = eventQueue.take();
                    receiveEvent(event);
                }
            } catch (Throwable e) {
                LOGGER.error("Event listener error", e);
            }
        }

        @Override
        public synchronized void start() {
            if (!initialized) {
                super.start();
                initialized = true;
            }
        }

        private void receiveEvent(final Event event) {
            for (EventSubscriber subscriber : subscribers) {
                notifySubscriber(subscriber, event);
            }
        }
    }


}
