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

    public static <T extends Record> Data create(String key, T value) {
        Data data = new Data();
        data.key = key;
        data.value = value;
        return data;
    }
}
