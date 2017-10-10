package com.waves.test;

import java.io.Serializable;

public class ServiceInfo implements Serializable {

	private String domainName = null;
	private String name = null;
	private Integer port = null;
	private String configPath = null;
	private String host = null;
	private int waveLength = 0;
	private int compSize = 0;
	private Long timeout = null;
	private String type = null;
	
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	
	public String getDomainName() {
		return domainName;
	}
	
	public int getCompSize() {
		return compSize;
	}
	
	public void setCompSize(int compSize) {
		this.compSize = compSize;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getWaveLength() {
		return waveLength;
	}

	public void setWaveLength(int waveLength) {
		this.waveLength = waveLength;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "ServiceInfo [name=" + name + ", port=" + port + "]";
	}

}
