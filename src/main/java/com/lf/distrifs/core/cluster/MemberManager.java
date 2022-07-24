package com.lf.distrifs.core.cluster;

import com.google.common.base.Strings;
import com.lf.distrifs.common.Constants;
import com.lf.distrifs.util.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentSkipListMap;

@Service
@Slf4j
public class MemberManager {


    private ConcurrentSkipListMap<String, Member> membersExcludeSelf = new ConcurrentSkipListMap<>();
    private Member self;


    @PostConstruct
    public void init() {
        log.info("Member manager init start");
        int port = NetUtils.LOCAL_PORT;
        String ip = NetUtils.LOCAL_IP;
        self = new Member(ip, port);
        self.setNodeStatus(NodeStatus.UP);
        log.info("Member manager init finished");

        mock();
    }

    public void mock() {
        String serverIp = System.getProperty("distrifs.target.server.ip");
        String serverPort = System.getProperty("distrifs.target.server.port");
        if (!Strings.isNullOrEmpty(serverPort) && !Strings.isNullOrEmpty(serverIp)) {
            membersExcludeSelf.put(serverIp + ":" + serverPort, new Member(serverIp, Integer.parseInt(serverPort)));
        }
    }


    public Collection<Member> allMembers() {
        HashSet<Member> set = new HashSet<>(membersExcludeSelf.values());
        set.add(self);
        return set;
    }

    public Collection<Member> allMembersExcludeSelf() {
        return new HashSet<>(membersExcludeSelf.values());
    }
}
