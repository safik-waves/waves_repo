package com.waves.core;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import com.waves.ex.WavesInitException;
import com.waves.ex.WavesRuntimeException;
import com.waves.model.Component;

public abstract class Builder {
	private RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();

	private Component component = null;

	public Builder(Component component) {
		this.component = component;
	}

	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	public abstract void build() throws WavesInitException;

	public abstract String stream(String cmd, Object input) throws WavesRuntimeException;

	protected String getCode(String type, String name, Object obj) {
		Map<String, Object> map = new HashMap<String, Object>();
		return getCode(type, map);
	}

	protected String getCode(String type, Map<String, Object> map) {
		ClassLoader classLoader = getClass().getClassLoader();
		URL url = classLoader.getResource("templates/" + type + ".vm");

		StringWriter sw = new StringWriter();
		SimpleNode node = null;
		try {
			// Path p = Paths.get(url.toURI());
			// map.put("file", p.getParent());
			InputStream input = classLoader.getResourceAsStream("templates/" + type + ".vm");
			if (input == null)
				return "";
			node = runtimeServices.parse(new InputStreamReader(input), type);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Template template = new Template();
		template.setRuntimeServices(runtimeServices);
		template.setData(node);
		template.initDocument();

		VelocityContext context = new VelocityContext();
		context.put("list", new HashSet());
		for (String key : map.keySet()) {
			context.put(key, map.get(key));
		}

		template.merge(context, sw);
		return sw.toString();
	}

}
