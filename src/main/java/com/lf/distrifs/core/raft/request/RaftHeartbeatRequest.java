package com.lf.distrifs.core.raft.request;

import com.lf.distrifs.core.grpc.request.Request;
import com.lf.distrifs.core.raft.RaftNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaftHeartbeatRequest<T> extends Request {

    //发起心跳的节点，必定是leader
    RaftNode node;

    T data;
}
