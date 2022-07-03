package com.lf.distrifs.core.grpc.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionResetRequest extends ServerRequest {
    String serverIp;

    String serverPort;

}
