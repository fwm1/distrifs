package com.lf.distrifs.core.raft.request;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.lf.distrifs.core.grpc.request.Request;
import com.lf.distrifs.core.raft.RaftNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaftVoteRequest extends Request {
    //发起vote的节点
    RaftNode candidate;
}
