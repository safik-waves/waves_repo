package com.waves.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "output")
public class Output implements Serializable{

	@XmlAttribute(required = true)
	private String on = null;

	@XmlTransient
	private String rootPath = null;

	@XmlAttribute
	private String to = null;

	@XmlAttribute
	private String type = null;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	@XmlElement(name = "stream")
	private List<Stream> streams = null;

	public String getTo() {
		return to;
	}

	public void setTo(String type) {
		this.to = type;
	}

	public List<Stream> getStreams() {
		return streams;
	}

	public void setStreams(List<Stream> streams) {
		this.streams = streams;
	}

	public String getOn() {
		return on;
	}

	public void setOn(String on) {
		this.on = on;
	}

	@Override
	public String toString() {
		return "output[" +type  + ":(" + on + "->" + to + ")]";
	}

}
