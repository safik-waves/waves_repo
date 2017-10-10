package com.waves.model;

import java.io.Serializable;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.io.IOUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "macro")
public class Macro {

	@XmlAttribute
	private String type = null;

	@XmlAttribute
	private String to = null;

	@XmlTransient
	private Serializable data = null;

	public Serializable getData() {
		return data;
	}

	public void setData(Serializable data) {
		this.data = data;
	}

	@XmlAttribute
	private String path = null;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getContent() throws Exception {
		return IOUtils.toString(new URL(path));
	}

	@Override
	public String toString() {
		return "Macro [type=" + type + ", on=" + to + ", path=" + path + "]";
	}

}
