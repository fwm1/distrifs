package com.lf.distrifs.core.raft;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;
import com.lf.distrifs.core.grpc.GrpcClient;
import com.lf.distrifs.core.raft.response.RaftVoteResponse;
import com.lf.distrifs.util.CommonUtils;
import com.lf.distrifs.util.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.util.concurrent.atomic.AtomicLong;

import static com.lf.distrifs.util.CommonUtils.getAddress;

@Slf4j
@Service
public class RaftService {

    @Resource
    private RaftNodeManager raftNodeManager;

    @Resource
    private RaftClientService raftClientService;

    private RaftRpcClient rpcClient = new RaftRpcClient();

    @PostConstruct
    public void init() {
        testVote();
    }

    public void testVote() {
        RaftNode candidate = new RaftNode();
        candidate.status = RaftNode.NodeStatus.CANDIDATE;
        candidate.ip = NetUtils.LOCAL_IP;
        candidate.port = NetUtils.LOCAL_PORT;
        candidate.term = new AtomicLong(1L);
        raftClientService.sendVoteRequest(candidate, new FutureCallback<RaftNode>() {
            @Override
            public void onSuccess(RaftNode result) {
                log.info("[Raft] Received raft vote response in callback, voteFor={}", result);
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("[Raft] Raft vote request failed", t);
            }
        });
    }

    public synchronized RaftNode receiveVote(RaftNode candidate) {
        String remoteAddress = getAddress(candidate.ip, candidate.port);
        if (!raftNodeManager.containsNode(remoteAddress)) {
            throw new IllegalStateException("Cannot find node:" + candidate);
        }
        RaftNode self = raftNodeManager.getSelf();
        if (candidate.term.get() <= self.term.get()) {
            log.info("[Raft] Received illegal vote request, local term={}, remote term={}", self.term.get(), candidate.term.get());
            if (Strings.isNullOrEmpty(self.voteFor)) {
                self.voteFor = getAddress(self.ip, self.port);
            }

            return self;
        }

        self.resetLeaderTimeout();
        self.ip = NetUtils.LOCAL_IP;
        self.port = NetUtils.LOCAL_PORT;
        self.status = RaftNode.NodeStatus.FOLLOWER;
        self.voteFor = remoteAddress;
        self.term.set(candidate.term.get());

        log.info("[Raft] {} vote {} as leader, term={}", getAddress(self.ip, self.port), remoteAddress, candidate.term.get());
        return self;
    }
}
