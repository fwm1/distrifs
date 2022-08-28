package com.lf.distrifs.core.fs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.lf.distrifs.common.Data;
import com.sun.org.apache.bcel.internal.generic.IXOR;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class FileStore {

    @PostConstruct
    public void init() {
        new Thread(() -> {
            int idx = 1;
            while (true) {
                String path = "file_" + idx++;
                append(path, Data.create(path, new FileRecord(new File(path))));
                try {
                    Thread.sleep(1000L);
                } catch (Throwable ignored) {

                }
            }
        }).start();
    }


    Map<String, Data<FileRecord>> fileMap = Maps.newConcurrentMap();

    LinkedBlockingQueue<Map.Entry<String, Data<FileRecord>>> appendQueue = new LinkedBlockingQueue<>();

    public void append(String path, Data<FileRecord> data) {
        appendQueue.offer(Maps.immutableEntry(path, data));
        log.info("[FileStore] Append file entry to queue, path={}, queue size={}", path, appendQueue.size());
    }

    public LinkedHashMap<String, Data<FileRecord>> getAppendingLog() {
        if (appendQueue.isEmpty()) {
            log.info("[FileStore] Append queue is empty");
            return new LinkedHashMap<>();
        }
        int size = appendQueue.size();
        log.info("[FileStore] Append queue size={}", size);
        LinkedHashMap<String, Data<FileRecord>> map = new LinkedHashMap<>(size);
        appendQueue.iterator().forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue()));
        return map;
    }

    public void onSync(LinkedHashMap<String, Data<FileRecord>> synced) {
        List<String> removed = new ArrayList<>();
        Iterator<Map.Entry<String, Data<FileRecord>>> iterator = appendQueue.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Data<FileRecord>> entry = iterator.next();
            String path = entry.getKey();
            Data<FileRecord> dataInQueue = entry.getValue();
            if (synced.containsKey(path)) {
                Data<FileRecord> syncedData = synced.get(path);
                if (syncedData != null) {
                    if (syncedData.getTimeStamp().get() >= dataInQueue.getTimeStamp().get()) {
                        removed.add(path);
                        try {
                            doWrite(dataInQueue.getValue());
                            iterator.remove();
                        } catch (Throwable e) {
                            log.warn("[FileStore] Failed to write file, path={}", path, e);
                        }
                    }
                }
            }
        }
        log.info("[FileStore] Remove entry from appending queue when sync success, removed size={}, removed paths={}", removed.size(), removed);
    }


    private void doWrite(FileRecord fileRecord) {
        // todo write file
    }


    public Map<String, Data<FileRecord>> getSnapshot() {
        return ImmutableMap.copyOf(fileMap);
    }

}
