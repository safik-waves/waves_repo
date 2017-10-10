package com.waves.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "wave")
public class Wave implements Serializable{

	@XmlTransient
	private String rootPath = null;

	@XmlTransient
	private boolean processExist = true;

	@XmlTransient
	private int port;

	@XmlTransient
	private String host;

	@XmlAttribute
	private String on = null;

	@XmlAttribute
	private String container = null;
	
	@XmlAttribute
	private String type = null;

	@XmlElement(name = "input")
	private Input input = null;

	@XmlElement(name = "output")
	private Output output = null;

	public void setProcessExist(boolean processExist) {
		this.processExist = processExist;
	}

	public boolean isProcessExist() {
		return processExist;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public String getOn() {
		return on;
	}

	public void setOn(String on) {
		this.on = on;
	}

	public Input getInput() {
		return input;
	}

	public void setInput(Input input) {
		this.input = input;
	}

	public Output getOutput() {
		return output;
	}

	public void setOutput(Output output) {
		this.output = output;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "wave[" + on + "]";
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;

	}

	public String getRootPath() {
		return rootPath;
	}

}
