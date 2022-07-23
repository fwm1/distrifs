package com.lf.distrifs.util;

import com.lf.distrifs.common.Constants;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class NetUtils {

    private static final String LEGAL_LOCAL_IP_PROPERTY = "java.net.preferIPv6Addresses";

    public static final String LOCAL_IP = getIp();

    public static final int LOCAL_PORT = Constants.DETAIL_PORT;

    public static String localAddress() {
        return LOCAL_IP + ":" + LOCAL_PORT;
    }

    private static String getIp() {
        InetAddress inetAddress = findFirstNonLoopbackAddress();
        if (inetAddress == null) {
            return "127.0.0.1";
        }

        return inetAddress.getHostAddress();
    }

    private static InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;

        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics.hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    } else {
                        continue;
                    }

                    for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs.hasMoreElements(); ) {
                        InetAddress address = addrs.nextElement();
                        boolean isLegalIpVersion =
                                Boolean.parseBoolean(System.getProperty(LEGAL_LOCAL_IP_PROPERTY))
                                        ? address instanceof Inet6Address : address instanceof Inet4Address;
                        if (isLegalIpVersion && !address.isLoopbackAddress()) {
                            result = address;
                        }
                    }

                }
            }
        } catch (IOException ex) {
            //ignore
        }

        if (result != null) {
            return result;
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            //ignore
        }

        return null;

    }
}
