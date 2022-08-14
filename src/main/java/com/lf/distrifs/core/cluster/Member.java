package com.lf.distrifs.core.cluster;

import com.lf.distrifs.util.CommonUtils;
import lombok.Data;

import java.util.Comparator;
import java.util.Objects;
import java.util.StringJoiner;

import static com.google.common.base.Preconditions.checkArgument;

@Data
public class Member implements Comparator<Member> {

    String ip;
    int port;
    String entryPoint;

    volatile NodeStatus nodeStatus;

    public Member() {
    }


    public Member(String entryPoint) {
        checkArgument(entryPoint != null && entryPoint.contains(":"), "Illegal address, expected 'ip:port'");
        int idx = entryPoint.indexOf(":");
        this.ip = entryPoint.substring(0, idx);
        this.port = Integer.parseInt(entryPoint.substring(idx + 1));
        this.entryPoint = CommonUtils.toEntryPoint(ip, port);
    }

    public Member(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.entryPoint = CommonUtils.toEntryPoint(ip, port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(entryPoint, member.entryPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entryPoint);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Member.class.getSimpleName() + "[", "]")
                .add("ip='" + ip + "'")
                .add("port=" + port)
                .add("address='" + entryPoint + "'")
                .add("nodeStatus=" + nodeStatus)
                .toString();
    }

    @Override
    public int compare(Member o1, Member o2) {
        return o1.entryPoint.compareTo(o2.entryPoint);
    }
}
