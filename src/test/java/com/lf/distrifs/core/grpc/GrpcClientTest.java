package com.lf.distrifs.core.grpc;

import com.lf.distrifs.core.grpc.common.ServerListManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class GrpcClientTest {

    @Resource
    private GrpcServer grpcServer;


    @Test
    public void testClient() {
        GrpcClient client = new GrpcClient("mock-client", new ServerListManager());
        client.start();
        client.loop();

    }

}