package com.lf.distrifs.core.raft;

import com.lf.distrifs.core.grpc.GrpcServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class RaftServiceTest {

    @Resource
    RaftService raftService;

    @Resource
    GrpcServer grpcServer;

    @Test
    public void testVote() {
        raftService.testVote();
    }

}