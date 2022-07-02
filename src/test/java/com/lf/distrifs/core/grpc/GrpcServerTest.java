package com.lf.distrifs.core.grpc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class GrpcServerTest {

    @Resource
    private GrpcServer grpcServer;

    @Test
    public void testGrpcServer() throws Exception {

    }
}