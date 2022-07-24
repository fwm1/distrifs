package com.lf.distrifs.core.raft;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class RaftServiceTest {

    @Resource
    RaftService raftService;

    @Test
    public void testVote() {
        raftService.testVote();
    }

}