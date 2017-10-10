package com.waves.model;

import static akka.pattern.PatternsCS.ask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
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
import com.waves.actor.WaveActor;
import com.waves.actor.WaveExecuteActor;
import com.waves.core.FeedBack;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.japi.function.Procedure;
import akka.stream.ActorMaterializer;
import akka.stream.Attributes;
import akka.stream.Graph;
import akka.stream.Inlet;
import akka.stream.KillSwitches;
import akka.stream.Materializer;
import akka.stream.Outlet;
import akka.stream.SinkShape;
import akka.stream.SourceShape;
import akka.stream.UniqueKillSwitch;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.AbstractOutHandler;
import akka.stream.stage.GraphStage;
import akka.stream.stage.GraphStageLogic;
import akka.util.Timeout;

import scala.concurrent.ExecutionContextExecutor;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "container")
public final class Container {

	@XmlTransient
	private Logger log = LoggerFactory.getLogger(Container.class);

	@XmlTransient
	private ActorSystem actorSystem = null;

	@XmlTransient
	private Domain domain = null;

	@XmlTransient
	private Materializer materializer = null;

	public Container() {

	}

	@XmlTransient
	private String rootPath = null;

	@XmlTransient
	private String contextPath = null;

	public Materializer getMaterializer() {
		return materializer;
	}

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
		log.info("Actor Config : " + config.toString());
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

					w.setPort(getPort());

					ActorRef ref = actorSystem.actorOf(WaveActor.props(w), w.getOn().replaceAll("/", "-"));

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
					if (s.getContainer().equalsIgnoreCase(c.name)) {
						// Wave w1 = new Wave();
						// w1.setOn(s.getWave());
						// w1.setContainer(s.getContainer());
						s.setPort(c.port);
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

	public void wave(Object request, Object resp, String path, FeedBack back) throws Exception {
		log.info("[" + getType().toUpperCase() + "]:(" + path + ")---> discovering");
		ActorSelection selection = actorSystem.actorSelection("/user/" + path.replaceAll("/", "-"));
		if (selection != null) {

			Timeout t = new Timeout(timeout, TimeUnit.MILLISECONDS);
			CompletionStage<Object> waveAsk = ask(selection, "wave", t);
			Wave w = (Wave) waveAsk.toCompletableFuture().get();
			CompletionStage<Object> refAsk = ask(selection, "ref", t);
			ActorRef ref = (ActorRef) refAsk.toCompletableFuture().get();

			Flow<Object, Object, NotUsed> flow = Flow.fromFunction(result -> {

				log.info("[" + getType().toUpperCase() + "]:(" + path + ")---> input planning");
				Object input = back.input(w.getInput(), result);
				if (input == null)
					input = result;
				return input;
			});

			Sink<Object, CompletionStage<Done>> sink = Sink.foreach(new Procedure<Object>() {

				@Override
				public void apply(Object result) throws Exception {
					log.info("[" + getType().toUpperCase() + "]:(" + path + ")---> output planning");
					back.output(w, resp, result);

				}

			});

			log.info("[" + getType().toUpperCase() + "]:(" + path + ")---> executing");
			Source<Object, NotUsed> source = Source.single(request);
			Object obj = source.via((Graph) flow).mapAsync(5, elem -> ask(ref, elem, t))
					.viaMat(KillSwitches.single(), Keep.right()).toMat(sink, Keep.right()).run(materializer);

		} else {
			log.info("[" + getType().toUpperCase() + "]:(" + path + ")---> WAVE-NOT-FOUND");
			throw new Exception("WAVE-NOT-FOUND : " + path);
		}

	}

	public void wave(String path, Object input, Flow<?, ?, NotUsed> flow, Sink<Object, CompletionStage<Done>> sink)
			throws Exception {

		log.info("[" + getType().toUpperCase() + "] discovering WAVE " + path + " :  --->  ");
		ActorSelection selection = actorSystem.actorSelection("/user/" + path.replaceAll("/", "-"));
		if (selection != null) {
			Timeout t = new Timeout(timeout, TimeUnit.MILLISECONDS);
			CompletionStage<Object> waveAsk = ask(selection, "wave", t);
			Wave wave = (Wave) waveAsk.toCompletableFuture().get();
			CompletionStage<Object> refAsk = ask(selection, "ref", t);
			ActorRef ref = (ActorRef) refAsk.toCompletableFuture().get();

			log.info("[" + getType().toUpperCase() + "] executing WAVE " + path + " :  --->  ");
			Source<Object, NotUsed> source = Source.single(input);
			Object obj = source.via((Graph) flow).mapAsync(5, elem -> ask(ref, elem, t))
					.viaMat(KillSwitches.single(), Keep.right()).toMat(sink, Keep.right()).run(materializer);

		} else {
			throw new Exception("WAVE-NOT-FOUND : " + path);
		}

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
