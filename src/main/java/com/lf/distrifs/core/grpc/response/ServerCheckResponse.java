package com.lf.distrifs.core.grpc.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ServerCheckResponse extends Response {

    String connectionId;
}
