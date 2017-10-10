package com.waves.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionStage;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleMetaData;
import org.jboss.metadata.parser.servlet.WebMetaDataParser;
import org.jboss.metadata.parser.util.MetaDataElementParser;
import org.jboss.metadata.property.PropertyReplacers;
import org.jboss.metadata.web.spec.AuthConstraintMetaData;
import org.jboss.metadata.web.spec.FilterMappingMetaData;
import org.jboss.metadata.web.spec.FilterMetaData;
import org.jboss.metadata.web.spec.FiltersMetaData;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.metadata.web.spec.LoginConfigMetaData;
import org.jboss.metadata.web.spec.SecurityConstraintMetaData;
import org.jboss.metadata.web.spec.ServletMappingMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.metadata.web.spec.ServletsMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.metadata.web.spec.WebResourceCollectionMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.waves.core.AbstractWavesContainer;
import com.waves.core.Builder;
import com.waves.model.Component;
import com.waves.model.Container;
import com.waves.process.HTMLProcessor;

import akka.actor.ActorSystem;
import akka.http.javadsl.ServerBinding;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.attribute.ExchangeAttributes;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.LoginConfig;
import io.undertow.servlet.api.SecurityConstraint;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.api.WebResourceCollection;

public class Web extends AbstractWavesContainer {

	Logger log = LoggerFactory.getLogger(Web.class);

	CompletionStage<ServerBinding> serverBindingFuture = null;

	public Web(Container container) {
		super(container, false);

	}

	@Override
	public void build(Component component) throws IOException {

	}

	@Override
	public boolean create(Container container) throws IOException {
		try {

			ServletContainer scontainer = ServletContainer.Factory.newInstance();
			DeploymentInfo deploymentInfo = new DeploymentInfo();
			deploymentInfo.setDeploymentName(container.getName());
			deploymentInfo.setClassLoader(Web.class.getClassLoader());
			parseWeb(deploymentInfo, container.getMacro(container.getPath()));
			deploymentInfo.setContextPath("/" + container.getName());
			container.setContextPath(deploymentInfo.getContextPath());
			ResourceManager resources = getResourceManager(container.getRootPath(), deploymentInfo);
			deploymentInfo.setResourceManager(resources);

			deploymentInfo.addServletContextAttribute("container", container);

			ServletInfo hydridesServlet = Servlets.servlet("builder", HTMLProcessor.class).setLoadOnStartup(1)
					.addMapping("/*").setAsyncSupported(true);
			deploymentInfo.addServlets(hydridesServlet);

			DeploymentManager manager = scontainer.addDeployment(deploymentInfo);

			manager.deploy();
			// HttpHandler path = manager.start();
			PathHandler path = Handlers.path(Handlers.redirect(deploymentInfo.getContextPath()))
					.addPrefixPath(deploymentInfo.getContextPath(), manager.start());

			final ResourceHandler resourceHandler = new ResourceHandler(resources);
			PredicateHandler predicateHandler = new PredicateHandler(
					Predicates.or(Predicates.contains(ExchangeAttributes.relativePath(), "."),
							Predicates.path(deploymentInfo.getContextPath()), Predicates.path("/")),
					resourceHandler, path);

			Undertow server = Undertow.builder().addHttpListener(8080, "localhost")
					.setHandler(predicateHandler).build();

			server.start();
			log.info("WEB Server Started : localhost:" + container.getPort() + deploymentInfo.getContextPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private ResourceManager getResourceManager(String rootPath, DeploymentInfo deploymentInfo) {

		return new PathResourceManager(Paths.get(rootPath, "/"), 100) {

			@Override
			public Resource getResource(String f) {
				log.debug("Web Resource Lookup :" + f);

				if (f.equalsIgnoreCase(deploymentInfo.getContextPath()))
					return super.getResource(deploymentInfo.getWelcomePages().get(0));
				else if (f.equals("/"))
					return super.getResource(deploymentInfo.getWelcomePages().get(0));
				else if (f.contains(".")) {

					if (Paths.get(rootPath, f).toFile().exists())
						return super.getResource(f);
					else {
						Path p = Paths.get("", f);
						int c = p.getNameCount();
						for (int i = 0; i < c; i++) {
							Path p1 = p.subpath(i, c);
							if (Paths.get(rootPath, p1.toString()).toFile().exists())
								return super.getResource(p1.toString());
						}
					}

				}
				return null;

			}
		};
	}

	public void parseWeb(DeploymentInfo info, Path path) throws Exception {
		File webxml = path.toFile();
		WebMetaDataParser parser = new WebMetaDataParser();
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader reader = inputFactory.createXMLStreamReader(new FileInputStream(webxml));

		WebMetaData web = WebMetaDataParser.parse(reader, new MetaDataElementParser.DTDInfo(),
				PropertyReplacers.noop());

		info.setDisplayName(web.getDescriptionGroup().getDisplayName());

		List<String> wf = web.getWelcomeFileList().getWelcomeFiles();
		if (wf != null) {
			for (String page : wf) {
				info.addWelcomePage(page);
			}
		}

		if (web.getContextParams() != null) {
			for (ParamValueMetaData p : web.getContextParams()) {
				info.addServletContextAttribute(p.getParamName(), p.getParamValue());

			}
		}
		if (web.getListeners() != null)
			for (ListenerMetaData l : web.getListeners()) {

				ListenerInfo listener = new ListenerInfo(
						(Class<? extends EventListener>) info.getClassLoader().loadClass(l.getListenerClass()));
				info.addListener(listener);
			}

		FiltersMetaData fs = web.getFilters();
		if (fs != null) {
			Iterator<FilterMetaData> it = fs.iterator();
			while (it.hasNext()) {
				FilterMetaData f = it.next();
				FilterInfo filter = Servlets.filter(f.getFilterName(),
						(Class<? extends Filter>) info.getClassLoader().loadClass(f.getFilterClass()),
						new InstanceFactory<Filter>() {

							@Override
							public InstanceHandle<Filter> createInstance() throws InstantiationException {
								System.out.println("&&&&&&&&&&&&&&&         FILTER *************");
								return null;
							}
						});
				log.info(filter.getName());
				if (f.getInitParam() != null)
					for (ParamValueMetaData p : f.getInitParam())
						filter.addInitParam(p.getParamName(), p.getParamValue());
				filter.setAsyncSupported(true);
				info.addFilter(filter);
			}
		}

		List<FilterMappingMetaData> fsl = web.getFilterMappings();
		if (fsl != null)
			for (FilterMappingMetaData d : fsl) {
				for (String u : d.getUrlPatterns()) {
					log.info("Filter Mapping : " + d.getFilterName() + " ---> " + u);
					info.addFilterUrlMapping(d.getFilterName(), u, DispatcherType.REQUEST);
				}
			}

		ServletsMetaData ss = web.getServlets();
		if (ss != null) {
			Iterator<ServletMetaData> its = ss.iterator();
			while (its.hasNext()) {
				ServletMetaData s = its.next();
				ServletInfo servlet = new ServletInfo(s.getName(),
						(Class<? extends Servlet>) info.getClassLoader().loadClass(s.getServletClass()));
				if (s.getInitParam() != null)
					for (ParamValueMetaData p : s.getInitParam())
						servlet.addInitParam(p.getParamName(), p.getParamValue());
				servlet.setLoadOnStartup(s.getLoadOnStartupInt());
				info.addServlet(servlet);
				for (ServletMappingMetaData sm : web.getServletMappings()) {
					if (s.getServletName().equalsIgnoreCase(sm.getServletName())) {
						servlet.addMappings(sm.getUrlPatterns());
					}

				}
			}
		}

		if (web.getSecurityConstraints() != null) {
			for (SecurityConstraintMetaData sc : web.getSecurityConstraints()) {
				SecurityConstraint s = new SecurityConstraint();
				for (WebResourceCollectionMetaData wm : sc.getResourceCollections()) {
					WebResourceCollection w = new WebResourceCollection();
					w.addUrlPatterns(wm.getUrlPatterns());
					w.addHttpMethods(wm.getHttpMethods());
					w.addHttpMethodOmissions(wm.getHttpMethodOmissions());
					s.addWebResourceCollection(w);
				}

				AuthConstraintMetaData wm = sc.getAuthConstraint();
				if (wm != null) {
					s.addRolesAllowed(wm.getRoleNames());
					info.addSecurityConstraint(s);
				}
			}
		}

		LoginConfigMetaData lm = web.getLoginConfig();
		if (lm != null) {
			LoginConfig l = new LoginConfig(lm.getAuthMethod(), lm.getRealmName(),
					lm.getFormLoginConfig().getLoginPage(), lm.getFormLoginConfig().getErrorPage());

			info.setLoginConfig(l);
		}

		if (web.getSecurityRoles() != null)
			for (SecurityRoleMetaData sr : web.getSecurityRoles())
				info.addSecurityRole(sr.getRoleName());

		// info.setSecurityContextFactory(new SecurityContextFactory() {
		//
		// @Override
		// public SecurityContext createSecurityContext(HttpServerExchange
		// exchange, AuthenticationMode mode,
		// IdentityManager identityManager, String programmaticMechName) {
		//
		// SecurityContextImpl impl=new SecurityContextImpl(exchange,mode,
		// identityManager);
		// log.info("8888888888888 "+identityManager);
		// return impl;
		// }
		// });

	}

}
