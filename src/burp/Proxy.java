package burp;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.InetAddressValidator;

import javax.swing.*;

public class Proxy {

    String host;
    int port;

    public Proxy(String proxyStr) throws IllegalArgumentException {
        if (StringUtils.isEmpty(proxyStr)) {
            throw new IllegalArgumentException("input is empty");
        }
        try {
            String[] parts = proxyStr.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("not valid host:port format");
            }
            host = parts[0];
            if (!isValidIPAddress(host) && !isValidDomainName(host)) {
                throw new IllegalArgumentException("host is not valid IP address and domain name");
            }
            String portStr = parts[1];
            port = Integer.parseInt(portStr);
            if (port >= 0 && port <= 65535) {
                throw new IllegalArgumentException("invalid port range");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    // 检查是否是合法的IP地址
    public static boolean isValidIPAddress(String ipAddress) {
        InetAddressValidator validator = InetAddressValidator.getInstance();
        return validator.isValidInet4Address(ipAddress);
    }

    // 检查是否是合法的域名
    public static boolean isValidDomainName(String domainName) {
        DomainValidator validator = DomainValidator.getInstance();
        return validator.isValid(domainName);
    }

    public static Proxy inputProxy() {
        int retry = 3;
        while (retry > 0) {
            String proxy = JOptionPane.showInputDialog("Confirm Proxy Of Burp", "127.0.0.1:8080");
            try {
                return new Proxy(proxy);
            } catch (IllegalArgumentException e) {
                BurpExtender.getStderr().println(e);
                retry = retry - 1;
            }
        }
        return null;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProxyStr() {
        return host + ":" + port;
    }
}
