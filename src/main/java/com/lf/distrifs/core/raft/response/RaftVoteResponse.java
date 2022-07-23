package com.lf.distrifs.core.raft.response;

import com.lf.distrifs.core.grpc.response.Response;
import com.lf.distrifs.core.raft.RaftNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaftVoteResponse extends Response {
    //接收到vote请求，将自己的节点信息返回给发起vote的节点，节点信息中包含了投票信息
    RaftNode remoteNode;
}
