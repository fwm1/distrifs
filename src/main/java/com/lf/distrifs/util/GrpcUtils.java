/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lf.distrifs.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.lf.distrifs.core.grpc.auto.GrpcProto;
import com.lf.distrifs.core.grpc.base.PayloadRegistry;
import com.lf.distrifs.core.grpc.request.Request;
import com.lf.distrifs.core.grpc.request.RequestMeta;
import com.lf.distrifs.core.grpc.response.Response;

import java.nio.charset.StandardCharsets;


public class GrpcUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }


    private static String toJson(Object obj) {

        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }


    public static <T> T toObj(String json, Class<T> cls) {
        try {
            return mapper.readValue(json, cls);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }


    public static GrpcProto.Payload convert(Request request, RequestMeta meta) {
        // request meta
        GrpcProto.Payload.Builder payloadBuilder = GrpcProto.Payload.newBuilder();
        GrpcProto.Metadata.Builder metaBuilder = GrpcProto.Metadata.newBuilder();
        if (meta != null) {
            metaBuilder.putAllHeaders(request.getHeaders()).setType(request.getClass().getSimpleName());
        }
        metaBuilder.setClientIp(NetUtils.localIP());
        payloadBuilder.setMetadata(metaBuilder.build());

        // request body
        request.clearHeaders();
        String jsonString = toJson(request);
        return payloadBuilder
                .setBody(Any.newBuilder().setValue(ByteString.copyFrom(jsonString, StandardCharsets.UTF_8)))
                .build();

    }

    /**
     * convert request to payload.
     *
     * @param request request.
     * @return payload.
     */
    public static GrpcProto.Payload convert(Request request) {

        GrpcProto.Metadata newMeta = GrpcProto.Metadata.newBuilder().setType(request.getClass().getSimpleName())
                .setClientIp(NetUtils.localIP()).putAllHeaders(request.getHeaders()).build();
        request.clearHeaders();
        String jsonString = toJson(request);

        GrpcProto.Payload.Builder builder = GrpcProto.Payload.newBuilder();

        return builder
                .setBody(Any.newBuilder().setValue(ByteString.copyFrom(jsonString, StandardCharsets.UTF_8)))
                .setMetadata(newMeta).build();

    }


    public static GrpcProto.Payload convert(Response response) {
        String jsonString = toJson(response);

        GrpcProto.Metadata.Builder metaBuilder = GrpcProto.Metadata.newBuilder().setType(response.getClass().getSimpleName());
        return GrpcProto.Payload.newBuilder()
                .setBody(Any.newBuilder().setValue(ByteString.copyFrom(jsonString, StandardCharsets.UTF_8)))
                .setMetadata(metaBuilder.build()).build();
    }


    public static Object parse(GrpcProto.Payload payload) {
        Class classType = PayloadRegistry.getClassByType(payload.getMetadata().getType());
        if (classType != null) {
            Object obj = toObj(payload.getBody().getValue().toString(StandardCharsets.UTF_8), classType);
            if (obj instanceof Request) {
                ((Request) obj).putAllHeader(payload.getMetadata().getHeadersMap());
            }
            return obj;
        } else {
            throw new RuntimeException("Unknown payload type:" + payload.getMetadata().getType());
        }

    }

}
