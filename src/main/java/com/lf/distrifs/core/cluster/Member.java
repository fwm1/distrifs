package com.lf.distrifs.core.cluster;

import lombok.Data;

import java.util.Comparator;
import java.util.Objects;
import java.util.StringJoiner;

@Data
public class Member implements Comparator<Member> {

    String ip;
    int port;
    String address;

    volatile NodeStatus nodeStatus;

    public Member() {
    }

    public Member(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.address = ip + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(address, member.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Member.class.getSimpleName() + "[", "]")
                .add("ip='" + ip + "'")
                .add("port=" + port)
                .add("address='" + address + "'")
                .add("nodeStatus=" + nodeStatus)
                .toString();
    }

    @Override
    public int compare(Member o1, Member o2) {
        return o1.address.compareTo(o2.address);
    }
}
