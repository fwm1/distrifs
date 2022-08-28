package com.lf.distrifs.core.raft;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.lf.distrifs.core.cluster.Member;
import com.lf.distrifs.core.cluster.MemberManager;
import com.lf.distrifs.core.grpc.GrpcClient;
import com.lf.distrifs.core.grpc.common.ServerListFactory;
import com.lf.distrifs.core.grpc.request.RequestCallBackBase;
import com.lf.distrifs.core.grpc.response.Response;
import com.lf.distrifs.core.raft.request.RaftHeartbeatRequest;
import com.lf.distrifs.core.raft.request.RaftVoteRequest;
import com.lf.distrifs.core.raft.response.RaftHeartbeatResponse;
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
                GrpcClient client = new GrpcClient("RaftRpcClient-" + peer.getEntryPoint(), new ServerListFactory() {
                    @Override
                    public String genNextServer() {
                        return peer.getEntryPoint();
                    }

                    @Override
                    public String getCurrentServer() {
                        return peer.getEntryPoint();
                    }

                    @Override
                    public List<String> getServerList() {
                        return ImmutableList.of(peer.getEntryPoint());
                    }
                });
                new Thread(client::start, "RaftRpcClient-starter-" + client.getName()).start();
                return client;
            });
        }
        inited.set(true);
    }

    public boolean isInited() {
        return inited.get();
    }

    public void sendVoteRequest(RaftNode candidate, RaftNode target, FutureCallback<RaftNode> callback) {
        if (!isInited()) {
            log.warn("[Raft] Raft client service not init, send vote request failed");
            return;
        }
        GrpcClient grpcClient = rpcClientMap.get(target);
        if (grpcClient == null) {
            log.warn("[Raft] Send vote request failed because rpc client miss");
            return;
        }
        try {
            grpcClient.asyncRequest(new RaftVoteRequest(candidate), new RaftVoteRequestCallback(callback));
        } catch (Throwable e) {
            log.error("[Raft] Send vote request failed because of exception", e);
        }
    }

    public <T> void sendHeartbeatRequest(RaftNode from, RaftNode target, T data, FutureCallback<Object> callback) {
        if (!isInited()) {
            log.warn("[Raft] Raft client service not init, send heartbeat request failed");
            return;
        }

        GrpcClient grpcClient = rpcClientMap.get(target);
        if (grpcClient == null) {
            log.warn("[Raft] Send heartbeat request failed because rpc client miss");
            return;
        }
        try {
            grpcClient.asyncRequest(new RaftHeartbeatRequest<>(from, data), new RaftHeartbeatRequestCallback(callback));
        } catch (Throwable e) {
            log.error("[Raft] Send heartbeat request failed because of exception", e);
        }
    }

    @AllArgsConstructor
    private static class RaftHeartbeatRequestCallback extends RequestCallBackBase<RaftHeartbeatResponse> {

        FutureCallback<Object> callback;

        @Override
        public void onResponse(Response response) {

            try {
                RaftHeartbeatResponse heartbeatResponse = (RaftHeartbeatResponse) response;
                RaftNode remoteNode = heartbeatResponse.getNode();
                callback.onSuccess(remoteNode);
                super.onResponse(response);
            } catch (Exception e) {
                log.warn("[Raft] Raft request callback onResponse failed", e);
                throw e;
            }
        }

        @Override
        public void onException(Throwable e) {
            callback.onFailure(e);
        }
    }

    @AllArgsConstructor
    private static class RaftVoteRequestCallback extends RequestCallBackBase<RaftVoteResponse> {

        FutureCallback<RaftNode> callback;

        @Override
        public void onResponse(Response response) {
            try {
                RaftVoteResponse voteResponse = (RaftVoteResponse) response;
                RaftNode remoteNode = voteResponse.getRemoteNode();
                callback.onSuccess(remoteNode);
                super.onResponse(response);
            } catch (Exception e) {
                log.warn("[Raft] Raft request callback onResponse failed", e);
                throw e;
            }
        }

        @Override
        public void onException(Throwable e) {
            callback.onFailure(e);
        }
    }

}
