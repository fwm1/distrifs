package com.lf.distrifs.core.raft;

import com.lf.distrifs.core.grpc.GrpcClient;
import com.lf.distrifs.core.grpc.common.ServerListManager;
import com.lf.distrifs.core.grpc.response.Response;
import com.lf.distrifs.core.raft.request.RaftVoteRequest;
import com.lf.distrifs.core.raft.response.RaftVoteResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RaftRpcClient {

    private final GrpcClient client;

    public RaftRpcClient() {
        client = new GrpcClient("Raft-client", new ServerListManager());
        start();
    }

    public void start() {
        client.start();
    }


    public RaftNode sendVoteRequest(RaftNode candidate) {
        RaftNode node = null;
        try {
            Response response = client.request(new RaftVoteRequest(candidate), 5000L);
            RaftVoteResponse voteRes = (RaftVoteResponse) response;
            node = voteRes.getRemoteNode();
        } catch (Exception e) {
            log.error("[Raft] Send vote request failed", e);
        }
        return node;
    }
}
