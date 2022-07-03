package com.lf.distrifs.core.grpc.remote;

import com.lf.distrifs.core.grpc.request.AliveDetectRequest;
import com.lf.distrifs.core.grpc.request.RequestMeta;
import com.lf.distrifs.core.grpc.response.AliveDetectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AliveDetectRequestHandler extends BaseRequestHandler<AliveDetectRequest, AliveDetectResponse> {
    @Override
    public AliveDetectResponse handle(AliveDetectRequest request, RequestMeta meta) throws Exception {
        log.info("Receive alive detect request from [{}], connectionId = {}", meta.getClientIp(), meta.getConnectionId());
        return new AliveDetectResponse();
    }
}
