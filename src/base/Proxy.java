package base;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.InetAddressValidator;

import burp.BurpExtender;

public class Proxy {

	String host = "127.0.0.1";
	int port;

	public Proxy(String proxyStr) {
		if (StringUtils.isEmpty(proxyStr)) {
			return;
		}
		try {
			String[] parts = proxyStr.split(":");
			if (parts.length != 2) {
				return;
			}
			String tmp_host = parts[0];
			if (!isValidIPAddress(tmp_host) && !isValidDomainName(tmp_host)) {
				return;
			}
			String portStr = parts[1];
			int tmp_port = Integer.parseInt(portStr);
			if (!(tmp_port >= 0 && tmp_port <= 65535)) {
				return;
			}
			host = tmp_host;
			port = tmp_port;
		} catch (Exception e) {
			e.printStackTrace();
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
