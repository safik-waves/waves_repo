package com.waves.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.StringBufferInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.waves.model.Component;
import com.waves.model.Container;
import com.waves.model.Interface;
import com.waves.model.Macro;
import com.waves.model.Waves;
import com.waves.util.CodeUtil;

import javafx.util.Pair;

public class HTTPProcessor extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Path> html = new HashMap<>();
	private Map<String, Path> parent = new HashMap<>();
	private Map<String, Path> jquery = new HashMap<>();
	private Map<String, Path> json = new HashMap<>();
	private Map<String, Path> velocity = new HashMap<>();
	private Map<String, Path> js = new HashMap<>();
	private StringBuilder script = new StringBuilder();
	private Map<String, String> paths = new HashMap<>();
	private Container container = null;

	private Logger log = LoggerFactory.getLogger(HTTPProcessor.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		log.info("Servlet INIT");
		script.append("<script>");
		script.append("var App = { ");
		try {
			container = (Container) getServletContext().getAttribute("container");
			for (Waves w : container.getWaves()) {
				for (Component com : w.getComponent()) {
					Component comp = (Component) container.loadComponent(com.getPath());

					if (comp != null && comp.getInterface() != null) {

						Path parentpath = Paths.get(container.getRootPath(), comp.getPath());

						for (Interface i : comp.getInterface()) {

							paths.put(container.getContextPath() + "/" + i.getOn(), i.getOn());
							parent.put("/" + i.getOn(), parentpath);

							if (i.getMacros() != null) {
								for (Macro m : i.getMacros()) {
									String p = "/" + i.getOn() + "/" + m.getTo();
									if ("html".equalsIgnoreCase(m.getType())) {
										html.put(p, Paths.get(container.getRootPath(), m.getPath()));
									} else if ("json".equalsIgnoreCase(m.getType())) {
										json.put(p, Paths.get(container.getRootPath(), m.getPath()));
									} else if ("velocity".equalsIgnoreCase(m.getType())) {
										velocity.put(p, Paths.get(container.getRootPath(), m.getPath()));
									} else if ("js".equalsIgnoreCase(m.getType())) {
										js.put(p, Paths.get(container.getRootPath(), m.getPath()));
									}

									paths.put(container.getContextPath() + p, i.getOn());
									// parent.put(p, parentpath);
								}
							}

							StringBuilder sb = new StringBuilder();

							sb.append(i.getOn().toLowerCase() + " : function(data){");

							for (Macro m : i.getMacros()) {
								Map<String, Object> map = new HashMap<>();
								map.put("interface", i);
								map.put("macro", m);
								map.put("contextpath", container.getContextPath());
								sb.append(CodeUtil.buildCode(i.getType(), m.getType(), map));

							}

							if (i.getTo() != null) {
								String[] split = i.getTo().split(",");
								for (int j = 0; 0 < split.length; j++) {
									sb.append(split[j] + "(data);");
								}
							}

							sb.append("},");

							script.append(sb.toString());
							script.append("\n");

						}
					}

					log.info("html : " + html);
					log.info("json : " + json);
					log.info("velocity : " + velocity);
					log.info("js : " + js);
				}
			}

			script.append(CodeUtil.buildCode("jquery", "error", null));
			script.append("};");
			script.append("\n");
			script.append("$(document).ready(function(){");
			Map<String, Object> map = new HashMap<>();
			map.put("paths", paths);
			script.append(CodeUtil.buildCode("jquery", "ready", map));
			script.append("});");
			script.append("</script>");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		log.info("Servlet POST Path Info : " + request.getPathInfo() + " Request URI :" + request.getRequestURI());
		String path = request.getPathInfo();
		InputStream input = null;

		Pair<String, Object> result = null;
		try {
			//result = container.wave(path.substring(1, path.length()), request);
		} catch (Exception e1) {
			log.info(" Result Error : " + e1);
			e1.printStackTrace();
		}

		try {
			if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {

				if (html.containsKey(path)) {

					input = new FileInputStream(html.get(path).toFile());
					resp.setContentType("text/html");

				} else if (js.containsKey(path)) {

					input = new FileInputStream(js.get(path).toFile());
					resp.setContentType("text/javascript");

				} else if (json.containsKey(path)) {

					input = new FileInputStream(json.get(path).toFile());
					resp.setContentType("application/json");

				} else if (velocity.containsKey(path)) {

					input = new FileInputStream(velocity.get(path).toFile());
					resp.setContentType("text/plain");
				} else if (result != null) {
					log.info(" Result Type :" + result.getKey() + " Data : " + result.getValue());
					if (result.getValue() instanceof InputStream) {
						input = (InputStream) result.getValue();
						resp.setContentType(result.getKey());
					} else if (result.getValue() instanceof File) {
						input = new FileInputStream((File) result.getValue());
						resp.setContentType(result.getKey());
					} else if (result.getValue() instanceof StringBuilder) {

						input = new StringBufferInputStream(result.getValue().toString());
						resp.setContentType(result.getKey());
					}

				}

			} else {

				if (parent.containsKey(path)) {
					InputStream html = new FileInputStream(parent.get(path).toFile());
					resp.setContentType("text/html");

					InputStream scr = IOUtils.toInputStream(script.toString());

					input = new SequenceInputStream(html, scr);
				}
			}

			if (input != null) {
				OutputStream out = resp.getOutputStream();
				IOUtils.copy(input, out);
				input.close();
				out.close();
			} else {
				log.error("Error : PATH NOT FOUND : " + path);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		super.destroy();

	}

}
