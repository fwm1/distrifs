package com.lf.distrifs.core.raft.handler;

import com.lf.distrifs.core.grpc.remote.BaseRequestHandler;
import com.lf.distrifs.core.grpc.request.RequestMeta;
import com.lf.distrifs.core.raft.RaftNode;
import com.lf.distrifs.core.raft.RaftService;
import com.lf.distrifs.core.raft.request.RaftVoteRequest;
import com.lf.distrifs.core.raft.response.RaftVoteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class RaftVoteRequestHandler extends BaseRequestHandler<RaftVoteRequest, RaftVoteResponse> {

    @Resource
    private RaftService raftService;

    @Override
    public RaftVoteResponse handle(RaftVoteRequest request, RequestMeta meta) throws Exception {
        RaftNode candidate = request.getCandidate();
        RaftNode selfNode = raftService.receiveVote(candidate);
        return new RaftVoteResponse(selfNode);
    }
}
