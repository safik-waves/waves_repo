package com.waves.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "component")

public class Component {

	@XmlTransient
	private Container container;

	public void setContainer(Container container) {
		this.container = container;
	}

	public Container getContainer() {
		return container;
	}

	@XmlTransient
	private Domain domain;

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public Domain getDomain() {
		return domain;
	}

	@XmlTransient
	private Waves wavesModel;

	public Waves getWavesModel() {
		return wavesModel;
	}

	public void setWavesModel(Waves wavesModel) {
		this.wavesModel = wavesModel;
	}

	@XmlAttribute
	private String title = null;

	@XmlAttribute
	private String type = null;

	@XmlAttribute
	private String on = null;

	@XmlElement(name = "interface")
	private List<Interface> interfaces = null;

	@XmlElement(name = "waves")
	private List<Waves> waves = null;

	@XmlAttribute(name = "path")
	private String path = null;

	@XmlAttribute(name = "alias")
	private String alias = null;

	@XmlAttribute
	private String macro = null;

	public String getMacro() {
		return macro;
	}

	public String getPath() {
		return path;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAlias() {
		return alias;
	}

	public String getOn() {
		return on;
	}

	public void setOn(String on) {
		this.on = on;
	}

	public List<Waves> getWaves() {
		return waves;
	}

	public void setWaves(List<Waves> waves) {
		this.waves = waves;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public List<Interface> getInterface() {
		return interfaces;
	}

	public void setInterface(List<Interface> interfaces) {
		this.interfaces = interfaces;
	}

}
