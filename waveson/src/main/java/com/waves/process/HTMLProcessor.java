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

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.RemoteEndpoint.Async;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waves.core.FeedBack;
import com.waves.model.Component;
import com.waves.model.Container;
import com.waves.model.Input;
import com.waves.model.Interface;
import com.waves.model.Macro;
import com.waves.model.Wave;
import com.waves.model.Waves;
import com.waves.util.CodeUtil;

import javafx.util.Pair;
import sun.net.www.content.audio.wav;

public class HTMLProcessor extends HttpServlet implements FeedBack {

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
	private Map<String, Interface> paths = new HashMap<>();
	private Container container = null;

	private Logger log = LoggerFactory.getLogger(HTMLProcessor.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		log.info("Servlet INIT");
		script.append("<script>");
		script.append("var App = { log:function(data){ if (console && console.log) {console.log(data);} }, ");
		try {
			container = (Container) getServletContext().getAttribute("container");
			for (Waves w : container.getWaves()) {
				for (Component com : w.getComponent()) {
					Component comp = (Component) container.loadComponent(com.getPath());

					if (comp != null && comp.getInterface() != null) {

						Path parentpath = Paths.get(container.getRootPath(), comp.getPath());

						for (Interface i : comp.getInterface()) {

							paths.put(container.getContextPath() + "/" + i.getOn(), i);
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

									// paths.put(container.getContextPath() + p,
									// i.getOn());
									// parent.put(p, parentpath);
								}
							}

							StringBuilder sb = new StringBuilder();

							sb.append(i.getOn().toLowerCase() + " : function(data){  var process = this; ");
							sb.append("App.log('waves -->" + i.getOn() + "()');");
							for (Macro m : i.getMacros()) {
								Map<String, Object> map = new HashMap<>();
								map.put("interface", i);
								map.put("macro", m);
								map.put("contextpath", container.getContextPath());
								sb.append(CodeUtil.buildCode(i.getType(), m.getType(), map));

							}

							if (i.getTo() != null) {
								String[] split = i.getTo().split(",");
								for (int j = 0; j < split.length; j++) {
									sb.append("\n$.when(process).done(App." + split[j] + "(data)); \n");
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

		log.info("[" + container.getType().toUpperCase() + "]:(" + request.getPathInfo() + ")---> WEB-BROWSER-REQUEST");
		String path = request.getPathInfo();
		String wavePath = path;
		if (wavePath.contains("/")) {
			wavePath = path.substring(path.indexOf("/") + 1, path.length());
		}

		InputStream input = null;

		Pair<String, Object> result = null;

		try {
			if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {

				if (html.containsKey(path)) {

					input = new FileInputStream(html.get(path).toFile());
					resp.setContentType("text/html");
					writeResponse(path, input, resp);
				} else if (js.containsKey(path)) {

					input = new FileInputStream(js.get(path).toFile());
					resp.setContentType("text/javascript");
					writeResponse(path, input, resp);
				} else if (json.containsKey(path)) {

					input = new FileInputStream(json.get(path).toFile());
					resp.setContentType("application/json");
					writeResponse(path, input, resp);
				} else if (velocity.containsKey(path)) {

					input = new FileInputStream(velocity.get(path).toFile());
					resp.setContentType("text/plain");
					writeResponse(path, input, resp);
				} else {

					AsyncContext asyncContext = request.startAsync();
					asyncContext.setTimeout(0);
					container.wave(request, asyncContext, wavePath, this);

				}

			} else {

				AsyncContext asyncContext = request.startAsync();
				asyncContext.setTimeout(0);
				container.wave(request, asyncContext, wavePath, this);

				if (parent.containsKey(path)) {
					InputStream html = new FileInputStream(parent.get(path).toFile());
					resp.setContentType("text/html");
					InputStream scr = IOUtils.toInputStream(script.toString());
					input = new SequenceInputStream(html, scr);
					writeResponse(path, input, resp);
				}
			}

		} catch (

		Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		super.destroy();

	}

	private void writeResponse(String path, InputStream input, HttpServletResponse resp) throws Exception {

		if (input != null) {
			OutputStream out = resp.getOutputStream();
			IOUtils.copy(input, out);
			input.close();
			out.close();
			log.info("[" + container.getType().toUpperCase() + "]:(" + path + ")---> DATA-STREAM-SUCCESS");
		} else {
			log.info("[" + container.getType().toUpperCase() + "]:(" + path + ")---> DATA-STREAM-FAILED");
		}
	}

	@Override
	public void output(Wave wave, Object output, Object result) throws Exception {

		AsyncContext context = (AsyncContext) output;

		InputStream input = null;
		String type = "appication/json";
		if (wave.getType() != null)
			type = wave.getType();

		HttpServletResponse resp = (HttpServletResponse) context.getResponse();
		if (result instanceof InputStream) {
			input = (InputStream) result;
			resp.setContentType(type);
		} else if (result instanceof File) {
			input = new FileInputStream((File) result);
			resp.setContentType(type);
		} else if (result instanceof StringBuilder) {
			input = IOUtils.toInputStream(result.toString());

			// ObjectMapper objectMapper = new ObjectMapper();
			// objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
			// JsonNode node = objectMapper.readTree(result.toString());

			resp.setContentType(type);
		}
		context.complete();
		writeResponse(wave.getOn(), input, resp);

	}

	@Override
	public Object input(Input model, Object request) throws Exception {

		HttpServletRequest input = (HttpServletRequest) request;
		if ("javax.servlet.http.HttpSession".equalsIgnoreCase(model.getOn())) {
			return input.getSession();
		} else if ("java.security.Principal".equalsIgnoreCase(model.getOn())) {
			return input.getUserPrincipal();
		} else if ("java.io.InputStream".equalsIgnoreCase(model.getOn())) {
			return input.getInputStream();
		} else if ("javax.servlet.ServletContext".equalsIgnoreCase(model.getOn())) {
			return input.getServletContext();
		} else if ("java.util.Map".equalsIgnoreCase(model.getOn())) {
			return input.getParameterMap();
		}
		return input;
	}

}
