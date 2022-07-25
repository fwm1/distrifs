package com.lf.distrifs.core.raft;

import com.google.common.base.Strings;
import com.lf.distrifs.core.cluster.Member;
import com.lf.distrifs.core.cluster.MemberManager;
import com.lf.distrifs.util.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class RaftNodeManager {

    @Resource
    private MemberManager memberManager;

    private Map<String, RaftNode> peers = new ConcurrentHashMap<>();

    AtomicLong localTerm = new AtomicLong(0L);

    RaftNode leader;


    @PostConstruct
    public void init() {
        RaftNode self = new RaftNode();
        self.ip = NetUtils.LOCAL_IP;
        self.port = NetUtils.LOCAL_PORT;
        peers.put(NetUtils.localAddress(), self);
        changePeers(memberManager.allMembers());
    }

    public RaftNode getNodeByAddress(String address) {
        return peers.get(address);
    }

    public RaftNode getSelf() {
        return getNodeByAddress(NetUtils.localAddress());
    }

    public boolean containsNode(String address) {
        return peers.containsKey(address);
    }

    public void changePeers(Collection<Member> members) {
        Map<String, RaftNode> tmp = new ConcurrentHashMap<>(peers.size());
        for (Member member : members) {

            final String address = member.getAddress();

            if (peers.containsKey(address)) {
                tmp.put(address, peers.get(address));
                continue;
            }

            RaftNode node = new RaftNode();
            node.ip = address;

            if (StringUtils.equals(NetUtils.localAddress(), address)) {
                node.term.set(localTerm.get());
            }

            tmp.put(address, node);
        }

        peers = tmp;

        log.info("Raft Node manager changePeers -> {}", peers);
    }

    public RaftNode calculateLeader(RaftNode candidate) {
        return null;
    }
}
