package com.waves.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.waves.actor.WaveStreamActor;
import com.waves.process.HTMLProcessor;
import com.waves.actor.WaveActor;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.pattern.AskableActorSelection;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.util.Timeout;
import javafx.util.Pair;
import scala.concurrent.Await;
import scala.concurrent.Future;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "container")
public final class Container2 {

	@XmlTransient
	private Logger log = LoggerFactory.getLogger(Container2.class);

	@XmlTransient
	private ActorSystem actorSystem = null;

	@XmlTransient
	private Domain domain = null;

	@XmlTransient
	private Materializer materializer = null;

	@XmlTransient
	private Map<String, Wave> waveList = null;

	@XmlTransient
	private Map<String, Pair<String, String>> waveTypes = null;

	public Container2() {
		this.waveList = new HashMap<>();
		this.waveTypes = new HashMap<>();
	}

	@XmlTransient
	private String rootPath = null;

	@XmlTransient
	private String contextPath = null;

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	@XmlAttribute
	private String name;

	@XmlAttribute
	private String path;

	@XmlAttribute
	private String host;

	@XmlAttribute
	private int port;

	@XmlAttribute
	private long timeout = 10000;

	@XmlAttribute
	private String type;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public Domain getDomain() {
		return domain;
	}

	public ActorSystem getActorSystem() {
		return actorSystem;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public Component loadComponent(String p) throws Exception {

		JAXBContext jc = JAXBContext.newInstance(Component.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		File f = Paths.get(rootPath, p + ".component.xml").toFile();
		log.info("Loading Component : " + f);
		Component comp = (Component) unmarshaller.unmarshal(f);
		return comp;
	}

	public void init(boolean processExist) throws Exception {
		if (actorSystem != null)
			log.info("Container already up and running");
		String s = ", akka.actor.deployment.router=round-robin-pool,akka.actor.deployment.nr-of-instances=10";
		Config config = ConfigFactory
				.parseString("akka.remote.netty.tcp.hostname=\"" + host + "\",  akka.remote.netty.tcp.port=" + port)
				.withFallback(ConfigFactory.load());

		actorSystem = ActorSystem.create(this.name, config);
		materializer = ActorMaterializer.create(actorSystem);

		if (waves != null) {
			for (Waves wvs : waves) {
				List<String> notfound = new ArrayList<>();
				JAXBContext jc2 = JAXBContext.newInstance(Waves.class);
				Unmarshaller unmarshaller2 = jc2.createUnmarshaller();
				File f1 = Paths.get(rootPath, wvs.getPath() + ".waves.xml").toFile();
				log.info("Loading Waves : " + f1);
				Waves ws = (Waves) unmarshaller2.unmarshal(f1);
				for (Wave w : ws.getWaves()) {

					w.setProcessExist(processExist);
					w.setRootPath(rootPath);

					String inputType = null;
					if (w.getInput() != null)
						inputType = w.getInput().getType();

					String outputType = null;
					if (w.getOutput() != null)
						outputType = w.getOutput().getType();

					waveTypes.put(w.getOn(), new Pair<String, String>(inputType, outputType));

					w.setPort(getPort());

					ActorRef ref = actorSystem.actorOf(WaveActor.props(w), w.getOn().replaceAll("/", "-"));
					waveList.put(w.getOn(), w);

					if (w.getInput() != null && w.getInput().getStreams() != null) {
						createStreams(w, w.getInput().getStreams());
					}
					if (w.getOutput() != null && w.getOutput().getStreams() != null) {
						createStreams(w, w.getOutput().getStreams());
					}

				}

			}
		}

	}

	private List<Stream> createStreams(Wave w, List<Stream> streams) {
		List<Container> containers = domain.getContainer();
		List<Stream> notfound = new ArrayList<>();

		for (Stream s : streams) {
			boolean found = false;
			if (s.getContainer() != null && !s.getContainer().equalsIgnoreCase(name)) {
				for (Container c : containers) {
					if (s.getContainer().equalsIgnoreCase(c.getName())) {
						// Wave w1 = new Wave();
						// w1.setOn(s.getWave());
						// w1.setContainer(s.getContainer());
						s.setPort(c.getPort());
						s.setHost(c.getHost());
						// ActorRef ref =
						// actorSystem.actorOf(RemoteStreamActor.props(s),
						// s.getWave().replaceAll("/", "-"));

						found = true;
					}
				}
			}
			if (!found)
				notfound.add(s);
		}

		return notfound;

	}

	public Pair<String, Object> wave(String path, Object data) throws Exception {

		Pair<String, Object> result = null;

		if (waveList.containsKey(path)) {
			Wave w = waveList.get(path);
			Pair<String, String> type = waveTypes.get(path);
			ActorSelection selection = actorSystem.actorSelection("/user/" + path.replaceAll("/", "-"));

			if (selection != null) {
				log.info("[" + getType().toUpperCase() + "] Starting WAVE " + path + " :  --->  "
						+ selection.pathString());
				Timeout t = new Timeout(timeout, TimeUnit.MILLISECONDS);
				AskableActorSelection asker = new AskableActorSelection(selection);
				Future<Object> fut = asker.ask(data, t);
				Object r = Await.result(fut, t.duration());
				if (r != null)
					result = new Pair<String, Object>(type.getValue(), r);
				log.info("[" + getType().toUpperCase() + "] Ending WAVE " + path + " : <--- "
						+ result.getClass().getSimpleName());

			}
			return result;

		}
		return null;

	}

	public Path getMacro(String p) throws IOException {
		Path pt = Paths.get(rootPath, p);

		if (pt.toFile().exists())
			return pt;

		throw new IOException(pt.toString());
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getRootPath() {
		return rootPath;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@XmlElement(name = "waves")
	private List<Waves> waves = null;

	public List<Waves> getWaves() {
		return waves;
	}

	public void setWaves(List<Waves> waves) {
		this.waves = waves;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
