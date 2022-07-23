package com.lf.distrifs.core.cluster;

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
        int port = Constants.DETAIL_PORT;
        String ip = NetUtils.LOCAL_IP;
        self = new Member(ip, port);
        self.setNodeStatus(NodeStatus.UP);
        log.info("Member manager init finished");
    }


    public Collection<Member> allMembers() {
        HashSet<Member> set = new HashSet<>(membersExcludeSelf.values());
        set.add(self);
        return set;
    }
}
