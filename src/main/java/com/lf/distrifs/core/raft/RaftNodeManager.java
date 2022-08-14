package com.lf.distrifs.core.raft;

import com.google.common.base.Strings;
import com.lf.distrifs.core.cluster.Member;
import com.lf.distrifs.core.cluster.MemberManager;
import com.lf.distrifs.util.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.bag.TreeBag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.lf.distrifs.util.CommonUtils.toEntryPoint;
import static com.lf.distrifs.util.NetUtils.localAddress;

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
        peers.put(localAddress(), self);
        changePeers(memberManager.allMembers());
    }

    public List<RaftNode> getPeersExcludeSelf() {
        return peers.values()
                .stream()
                .filter(peer -> !Objects.equals(toEntryPoint(peer.ip, peer.port), localAddress()))
                .collect(Collectors.toList());
    }

    public RaftNode getNodeByAddress(String address) {
        return peers.get(address);
    }

    public RaftNode getSelf() {
        return getNodeByAddress(localAddress());
    }

    public RaftNode getLeader() {
        return leader;
    }

    public void reset() {
        log.info("[Raft] Node manager reset, leader and all voteFor will be null");
        leader = null;
        peers.values().forEach(peer -> peer.voteFor = null);
    }

    public boolean containsNode(String address) {
        return peers.containsKey(address);
    }

    public void changePeers(Collection<Member> members) {
        Map<String, RaftNode> tmp = new ConcurrentHashMap<>(peers.size());
        for (Member member : members) {

            final String address = member.getEntryPoint();

            if (peers.containsKey(address)) {
                tmp.put(address, peers.get(address));
                continue;
            }

            RaftNode node = new RaftNode();
            node.ip = member.getIp();
            node.port = member.getPort();

            if (StringUtils.equals(localAddress(), address)) {
                node.term.set(localTerm.get());
            }

            tmp.put(address, node);
        }

        peers = tmp;

        log.info("Raft Node manager changePeers -> {}", peers);
    }

    public synchronized RaftNode calculateLeader(RaftNode candidate) {
        peers.put(toEntryPoint(candidate.ip, candidate.port), candidate);

        TreeBag<String> treeBag = new TreeBag<>();
        int maxApprovedCount = 0;
        String maxApprovedAddress = null;
        for (RaftNode peer : peers.values()) {
            if (Strings.isNullOrEmpty(peer.voteFor)) continue;
            treeBag.add(peer.voteFor);
            if (treeBag.getCount(peer.voteFor) > maxApprovedCount) {
                maxApprovedCount = treeBag.getCount(peer.voteFor);
                maxApprovedAddress = peer.voteFor;
            }
        }

        if (maxApprovedCount > peers.size() / 2) {
            RaftNode peer = peers.get(maxApprovedAddress);
            peer.status = RaftNode.NodeStatus.LEADER;

            if (!Objects.equals(peer, leader)) {
                leader = peer;
                log.info("[Raft] {} has become LEADER", leader);
            }
        } else {
            log.info("[Raft] Leader election failed because of insufficient votes");
        }
        return leader;
    }
}
