package com.waves.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "interface")
public class Interface {

	@XmlAttribute
	private String type = null;

	@XmlTransient
	private String path = null;

	@XmlAttribute
	private String on = null;

	@XmlAttribute
	private String to = null;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	@XmlElement(name = "macro")
	private List<Macro> macros = null;

	public List<Macro> getMacros() {
		return macros;
	}

	public void setMacros(List<Macro> macros) {
		this.macros = macros;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOn() {
		return on;
	}

	public void setOn(String on) {
		this.on = on;
	}

}
