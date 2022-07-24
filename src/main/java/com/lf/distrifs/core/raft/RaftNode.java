package com.lf.distrifs.core.raft;

import com.lf.distrifs.common.Constants;
import com.lf.distrifs.util.CommonUtils;
import lombok.Data;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class RaftNode {
    String ip;
    int port;
    String voteFor;
    AtomicLong term = new AtomicLong(0L);

    volatile long leaderTimeout = CommonUtils.nextRandomLong(0, Constants.LEADER_TIMEOUT_MS);
    volatile long heartbeatTimeout = CommonUtils.nextRandomLong(0, Constants.HEARTBEAT_INTERVAL_MS);

    NodeStatus status = NodeStatus.FOLLOWER;

    public RaftNode() {
    }

    public RaftNode(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void resetLeaderTimeout() {
        leaderTimeout = Constants.LEADER_TIMEOUT_MS + CommonUtils.nextRandomLong(0, Constants.RANDOM_MS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RaftNode raftNode = (RaftNode) o;
        return port == raftNode.port && Objects.equals(ip, raftNode.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RaftNode.class.getSimpleName() + "[", "]")
                .add("ip='" + ip + "'")
                .add("port=" + port)
                .add("voteFor='" + voteFor + "'")
                .add("term=" + term)
                .add("leaderTimeout=" + leaderTimeout)
                .add("heartbeatTimeout=" + heartbeatTimeout)
                .add("status=" + status)
                .toString();
    }

    public enum NodeStatus {
        FOLLOWER,
        CANDIDATE,
        LEADER
    }
}
