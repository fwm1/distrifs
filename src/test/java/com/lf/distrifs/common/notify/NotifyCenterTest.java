package com.lf.distrifs.common.notify;

import com.google.common.collect.ImmutableList;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.ref.Reference;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class NotifyCenterTest {

    static EventPublisher mockPublisher;

    @BeforeAll
    public static void before() {
        mockPublisher = NotifyCenter.registerPublisher(MockEvent.class);
    }

    @Test
    public void testNotifyCenter() throws Exception{
        AtomicBoolean flag = new AtomicBoolean(false);
        NotifyCenter.registerSubscriber(new EventSubscriber() {
            @Override
            public void onEvent(Event event) {
                assertTrue(event instanceof MockEvent);
                flag.set(true);
            }

            @Override
            public List<Class<? extends Event>> subscribeTypes() {
                return ImmutableList.of(MockEvent.class);
            }
        });
        mockPublisher.publish(new MockEvent());
        Thread.sleep(1000);
        assertTrue(flag.get());
    }


    private class MockEvent extends Event {

    }

}