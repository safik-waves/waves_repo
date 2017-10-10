package com.waves.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.waves.process.HTMLProcessor;

public final class CodeUtil {

	private final static RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
	private static Logger log = LoggerFactory.getLogger(CodeUtil.class);

	public static String buildCode(String type, String subtype, Map<String, Object> map) {

		if (subtype == null)
			subtype = "vm";

		ClassLoader classLoader = CodeUtil.class.getClassLoader();
		URL url = classLoader.getResource("templates/" + type + "." + subtype);
		if (url == null)
			return "alert('CODE NOT AVAILABLE for " + type + "." + subtype+"');";
		log.info("building CODE  : " + url.toString());
		StringWriter sw = new StringWriter();
		SimpleNode node = null;
		try {
			// Path p = Paths.get(url.toURI());
			// map.put("file", p.getParent());
			InputStream input = classLoader.getResourceAsStream("templates/" + type + "." + subtype);
			if (input == null)
				return "alert('CODE NOT AVAILABLE for " + type + "." + subtype+"');";
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
		if (map != null) {
			for (String key : map.keySet()) {
				context.put(key, map.get(key));
			}
		}

		template.merge(context, sw);
		return sw.toString();
	}

}
