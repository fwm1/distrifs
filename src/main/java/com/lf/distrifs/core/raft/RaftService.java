package com.lf.distrifs.core.raft;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;
import com.lf.distrifs.common.Constants;
import com.lf.distrifs.common.Data;
import com.lf.distrifs.core.fs.FileRecord;
import com.lf.distrifs.core.fs.FileStore;
import com.lf.distrifs.util.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lf.distrifs.util.CommonUtils.toEntryPoint;

@Slf4j
@Service
public class RaftService {

    @Resource
    private RaftNodeManager raftNodeManager;

    @Resource
    private RaftClientService raftClientService;

    @Resource
    private FileStore fileStore;

    private boolean initialized;

    @PostConstruct
    public void init() {
        log.info("[Raft] ======== Start initializing raft service ========");
        initTerm();

        registerElectionTimer();
        registerHeartbeatTimer();
    }

    private void registerElectionTimer() {
        RaftExecutors.RAFT_TIMER_EXECUTOR.scheduleAtFixedRate(new LeaderElectionTask(), 0, Constants.LEADER_ELECTION_TICK, TimeUnit.MILLISECONDS);
    }

    private void registerHeartbeatTimer() {
        RaftExecutors.RAFT_TIMER_EXECUTOR.scheduleAtFixedRate(new HeartbeatTask(), 0, Constants.HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void initTerm() {
        raftNodeManager.localTerm.set(0L);
        log.info("[Raft] Init local term -> 0");
        initialized = true;

    }

    public synchronized RaftNode receiveVote(RaftNode candidate) {
        String remoteAddress = toEntryPoint(candidate.ip, candidate.port);
        if (!raftNodeManager.containsNode(remoteAddress)) {
            throw new IllegalStateException("Cannot find node:" + candidate);
        }
        RaftNode self = raftNodeManager.getSelf();
        if (candidate.term.get() <= self.term.get()) {
            log.info("[Raft] Received illegal vote request, local term={}, remote term={}", self.term.get(), candidate.term.get());
            if (Strings.isNullOrEmpty(self.voteFor)) {
                self.voteFor = toEntryPoint(self.ip, self.port);
            }

            return self;
        }

        self.resetLeaderTimeout();
        self.ip = NetUtils.LOCAL_IP;
        self.port = NetUtils.LOCAL_PORT;
        self.status = RaftNode.NodeStatus.FOLLOWER;
        self.voteFor = remoteAddress;
        self.term.set(candidate.term.get());

        log.info("[Raft] {} vote {} as leader, term={}", toEntryPoint(self.ip, self.port), remoteAddress, candidate.term.get());
        return self;
    }

    public synchronized RaftNode receiveHeartbeat(RaftNode node) {
        String remoteAddress = toEntryPoint(node.ip, node.port);
        if (!raftNodeManager.containsNode(remoteAddress)) {
            throw new IllegalStateException("Cannot find node:" + node);
        }

        log.info("[Raft] Receive heartbeat from {}", node);
        RaftNode self = raftNodeManager.getSelf();
        self.resetLeaderTimeout();
        self.resetHeartbeatTimeout();

        return self;
    }

    private class LeaderElectionTask implements Runnable {
        @Override
        public void run() {
            try {
                RaftNode self = raftNodeManager.getSelf();
                if ((self.leaderTimeout -= Constants.LEADER_ELECTION_TICK) > 0) {
                    return;
                }

                self.resetLeaderTimeout();
                self.resetHeartbeatTimeout();

                //开始投票
                startVote();
            } catch (Exception e) {
                log.warn("[Raft] Leader election task has exception", e);
            }
        }

        public void startVote() {
            RaftNode self = raftNodeManager.getSelf();
            log.info("[Raft] Leader timeout, start vote, leader={}, term={}", raftNodeManager.getLeader(), self.term);

            raftNodeManager.reset();

            self.term.incrementAndGet();
            self.voteFor = toEntryPoint(self.ip, self.port);
            self.status = RaftNode.NodeStatus.CANDIDATE;

            List<RaftNode> peersExcludeSelf = raftNodeManager.getPeersExcludeSelf();
            peersExcludeSelf.forEach(peer -> raftClientService.sendVoteRequest(self, peer, new FutureCallback<RaftNode>() {
                @Override
                public void onSuccess(RaftNode result) {
                    log.info("[Raft] Received raft vote response in callback, result={}", result);
                    raftNodeManager.calculateLeader(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    log.error("[Raft] Raft vote request failed", t);
                }
            }));

        }
    }

    private class HeartbeatTask implements Runnable {
        @Override
        public void run() {

            try {
                RaftNode self = raftNodeManager.getSelf();
                if (self.status != RaftNode.NodeStatus.LEADER || (self.heartbeatTimeout -= Constants.HEARTBEAT_TICK) > 0) {
                    return;
                }
                self.resetHeartbeatTimeout();
                //发送心跳
                sendHeartbeat();
            } catch (Exception e) {
                log.warn("[Raft] Heartbeat task has exception", e);
            }

        }

        public void sendHeartbeat() {
            RaftNode self = raftNodeManager.getSelf();
            self.resetLeaderTimeout();

            List<RaftNode> peersExcludeSelf = raftNodeManager.getPeersExcludeSelf();
            int peerSize = peersExcludeSelf.size();
            final AtomicInteger successTimes = new AtomicInteger(0);

            LinkedHashMap<String, Data<FileRecord>> appendingLog = fileStore.getAppendingLog();
            peersExcludeSelf.forEach(peer -> {
                raftClientService.sendHeartbeatRequest(self, peer, appendingLog,
                        new FutureCallback<Object>() {
                            @Override
                            public void onSuccess(@NullableDecl Object result) {
                                log.info("[Raft] Received raft heartbeat response in callback, result={}", result);
                                //todo raft heartbeat with data
                                if (successTimes.incrementAndGet() > peerSize / 2) {
                                    log.info("[Raft] Raft heartbeat received over half of peers");
                                    fileStore.onSync(appendingLog);
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                log.error("[Raft] Raft heartbeat request failed", t);
                            }
                        });
            });
        }
    }
}
