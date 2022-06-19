package com.lf.distrifs.core.grpc.request;

import lombok.Data;

@Data
public class ConnectionResetRequest extends ServerRequest {
    String serverIp;

    String serverPort;

}
