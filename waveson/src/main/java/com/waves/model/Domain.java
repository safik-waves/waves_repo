package com.waves.model;

import java.nio.file.Path;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "domain")
public class Domain {

	@XmlTransient
	private String status = "STARTED";

	@XmlAttribute
	private String path;

	@XmlAttribute
	private String base;

	@XmlAttribute
	private String title;

	@XmlAttribute
	private String namespace;

	@XmlAttribute
	private boolean secure = false;

	@XmlAttribute
	private String macro = null;

	public String getMacro() {
		return macro;
	}

	public String getBase() {
		return base;
	}

	public String getPath() {
		return path;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isSecure() {
		return secure;
	}

	public String getNamespace() {
		return namespace;
	}

	@XmlElement(name = "container")
	private List<Container> container = null;

	@XmlTransient
	private Path file;

	public Path getFile() {
		return file;
	}

	public void setFile(Path file) {
		this.file = file;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Container> getContainer() {
		return container;
	}

	public void setContainer(List<Container> container) {
		this.container = container;
	}

	@Override
	public String toString() {
		return "Domain [ title=" + title + ", container=" + container + "]";
	}

	private String contextPath = null;

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getContextPath() {
		return contextPath;
	}

	@XmlTransient
	private Path configPath = null;

	public void setConfigPath(Path configPath) {
		this.configPath = configPath;
	}

	public Path getConfigPath() {
		return configPath;
	}

	@XmlTransient
	private String name = null;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@XmlTransient
	private String url = null;

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	private String hostname = null;

	public void setHostname(String hostName) {
		this.hostname = hostname;
	}

	public String getHostname() {
		return hostname;
	}
}
