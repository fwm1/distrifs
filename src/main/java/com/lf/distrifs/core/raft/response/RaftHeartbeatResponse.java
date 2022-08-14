package com.lf.distrifs.core.raft.response;

import com.lf.distrifs.core.grpc.response.Response;
import com.lf.distrifs.core.raft.RaftNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaftHeartbeatResponse  extends Response {
    RaftNode node;
}
