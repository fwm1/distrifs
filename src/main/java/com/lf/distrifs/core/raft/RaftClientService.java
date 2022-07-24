package com.lf.distrifs.core.raft;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.lf.distrifs.common.Constants;
import com.lf.distrifs.core.cluster.Member;
import com.lf.distrifs.core.cluster.MemberManager;
import com.lf.distrifs.core.grpc.GrpcClient;
import com.lf.distrifs.core.grpc.common.ServerListFactory;
import com.lf.distrifs.core.grpc.request.RequestCallBackBase;
import com.lf.distrifs.core.grpc.response.Response;
import com.lf.distrifs.core.raft.request.RaftVoteRequest;
import com.lf.distrifs.core.raft.response.RaftVoteResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class RaftClientService {

    @Resource
    private MemberManager memberManager;

    Map<RaftNode, GrpcClient> rpcClientMap = new HashMap<>();

    AtomicBoolean inited = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        prepareRpcClient();
    }

    private void prepareRpcClient() {
        Collection<Member> peers = memberManager.allMembersExcludeSelf();
        for (Member peer : peers) {
            rpcClientMap.computeIfAbsent(new RaftNode(peer.getIp(), peer.getPort()), innerNode -> {
                GrpcClient client = new GrpcClient("RaftRpcClient-" + peer.getAddress(), new ServerListFactory() {
                    @Override
                    public String genNextServer() {
                        return peer.getAddress();
                    }

                    @Override
                    public String getCurrentServer() {
                        return peer.getAddress();
                    }

                    @Override
                    public List<String> getServerList() {
                        return ImmutableList.of(peer.getAddress());
                    }
                });
                client.start();
                return client;
            });
        }
        inited.set(true);
    }

    public boolean isInited() {
        return inited.get();
    }

    public void sendVoteRequest(RaftNode candidate, FutureCallback<RaftNode> callback) {
        if (!isInited()) {
            log.warn("[Raft] Raft client service not init, send vote request failed");
            return;
        }
        for (GrpcClient client : rpcClientMap.values()) {
            client.asyncRequest(new RaftVoteRequest(candidate), new RaftVoteRequestCallback(callback));
        }
    }

    @AllArgsConstructor
    private static class RaftVoteRequestCallback extends RequestCallBackBase<RaftVoteResponse> {

        FutureCallback<RaftNode> callback;

        @Override
        public long getTimeout() {
            return Constants.DEFAULT_RPC_TIMEOUT;
        }

        @Override
        public void onResponse(Response response) {
            try {
                RaftVoteResponse voteResponse = (RaftVoteResponse) response;
                RaftNode remoteNode = voteResponse.getRemoteNode();
                callback.onSuccess(remoteNode);
            } catch (Exception e) {
                log.warn("[Raft] Raft request callback onResponse failed", e);
            }
        }

        @Override
        public void onException(Throwable e) {
            callback.onFailure(e);
        }
    }

}
