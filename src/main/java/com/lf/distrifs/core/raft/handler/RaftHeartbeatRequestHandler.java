package com.lf.distrifs.core.raft.handler;

import com.lf.distrifs.core.grpc.remote.BaseRequestHandler;
import com.lf.distrifs.core.grpc.request.RequestMeta;
import com.lf.distrifs.core.raft.RaftNode;
import com.lf.distrifs.core.raft.RaftService;
import com.lf.distrifs.core.raft.request.RaftHeartbeatRequest;
import com.lf.distrifs.core.raft.response.RaftHeartbeatResponse;
import com.lf.distrifs.core.raft.response.RaftVoteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class RaftHeartbeatRequestHandler extends BaseRequestHandler<RaftHeartbeatRequest, RaftHeartbeatResponse> {

    @Resource
    private RaftService raftService;

    @Override
    public RaftHeartbeatResponse handle(RaftHeartbeatRequest request, RequestMeta meta) throws Exception {
        RaftNode fromNode = request.getNode();
        RaftNode selfNode = raftService.receiveHeartbeat(fromNode);
        return new RaftHeartbeatResponse(selfNode);
    }
}
