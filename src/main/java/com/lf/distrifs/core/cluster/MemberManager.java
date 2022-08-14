package com.lf.distrifs.core.cluster;

import com.google.common.base.Splitter;
import com.lf.distrifs.util.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.google.common.base.Preconditions.checkArgument;

@Service
@Slf4j
public class MemberManager {

    @Value("${distfs.servers:}")
    private String serverStr;

    private ConcurrentSkipListMap<String, Member> members = new ConcurrentSkipListMap<>();
    private Member self;


    @PostConstruct
    public void init() {
        log.info("Member manager init start");
        int port = NetUtils.LOCAL_PORT;
        String ip = NetUtils.LOCAL_IP;
        self = new Member(ip, port);
        self.setNodeStatus(NodeStatus.UP);

        List<String> peerEntryPoints = Splitter.on(",").splitToList(serverStr);
        peerEntryPoints.forEach(entryPoint -> {
            Member peer = new Member(entryPoint);
            peer.setNodeStatus(NodeStatus.UP);
            members.putIfAbsent(entryPoint, peer);
        });

        checkArgument(members.containsKey(self.entryPoint), "Static entryPoints not contains self, self={}, peers={}", self.entryPoint, peerEntryPoints);

        log.info("Member manager init finished, size={}, self={}, peers={}", members.size(), self, members);
    }


    public Collection<Member> allMembers() {
        return new HashSet<>(members.values());
    }

    public Collection<Member> allMembersExcludeSelf() {
        HashMap<String, Member> tmp = new HashMap<>(this.members);
        tmp.remove(self.entryPoint);
        return tmp.values();
    }
}
