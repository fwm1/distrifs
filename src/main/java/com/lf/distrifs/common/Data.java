package com.lf.distrifs.common;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@lombok.Data
public class Data<T extends Record> implements Serializable {

    private static final long serialVersionUID = -889330773506458062L;

    String key;

    T value;

    AtomicLong timeStamp = new AtomicLong(0L);

    public static <T extends Record> Data<T> create(String key, T value) {
        Data<T> data = new Data<>();
        data.key = key;
        data.value = value;
        data.timeStamp.set(System.currentTimeMillis());
        return data;
    }
}
