package com.lf.distrifs.core.grpc.request;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RequestMeta {

    private String connectionId;

    private String clientIp;

    private Map<String, String> tags = new HashMap<>();

}
