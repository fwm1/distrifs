package com.lf.distrifs.core.fs;

import com.google.common.collect.Maps;
import com.lf.distrifs.common.Data;

import java.util.Map;

public class FileStore {

    Map<String, Data<FileRecord>> fileMap = Maps.newConcurrentMap();
    //todo 文件存储，暂时支持一层目录结构

}
