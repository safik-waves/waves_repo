package com.waves.core;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.waves.container.Database;
import com.waves.container.FileSystem;
import com.waves.container.HTTP;
import com.waves.container.Java;
import com.waves.container.REST;
import com.waves.container.Web;
import com.waves.model.Container;
import com.waves.model.Domain;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class WavesStartUp {
	static Logger log = LoggerFactory.getLogger(WavesStartUp.class);

	public static void main(String[] args) throws Exception {
		startUp();
	}

	public static void test() throws Exception {
		ActorSystem actorSystem = ActorSystem.create();
		ActorMaterializer materializer = ActorMaterializer.create(actorSystem);
		// final Source<Integer, NotUsed> source = Source.from(Arrays.asList(1,
		// 2, 3, 4, 5, 6, 7, 8, 9, 10));
		// // note that the Future is scala.concurrent.Future
		// final Sink<Integer, CompletionStage<Integer>> sink = Sink.<Integer,
		// Integer>fold(0,
		// (aggr, next) -> aggr + next);
		//
		// // connect the Source to the Sink, obtaining a RunnableFlow
		// final RunnableGraph<CompletionStage<Integer>> runnable =
		// source.toMat(sink, Keep.right());
		//
		// // materialize the flow
		// final CompletionStage<Integer> sum = runnable.run(materializer);

		Source.single(Arrays.asList(1, 2, 3)).map(i -> {
			System.out.println("A: " + i);
			return i;
		}).async().map(i -> {
			System.out.println("B: " + i);
			return i;
		}).async().map(i -> {
			System.out.println("C: " + i);
			return i;
		}).async().runWith(Sink.ignore(), materializer);
	}

	public static void startUp() throws Exception {
		JAXBContext jc = JAXBContext.newInstance(Domain.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		Domain domain = (Domain) unmarshaller.unmarshal(new File(
				"C:/Users/smdsa/git/waves/wavesdemo/WavesDemo/WebContent/WEB-INF/app/domains/dashboard.domain.xml"));
		List<Container> cont = domain.getContainer();

		for (Container c : cont) {
			c.setDomain(domain);
			c.setRootPath("C:/Users/smdsa/git/waves/wavesdemo/WavesDemo/WebContent/WEB-INF");
			WavesContainer won = factory(c);
			won.start();

		}
	}

	public static WavesContainer factory(Container container) {
		log.info("**************** <<< Starting " + container.getType().toUpperCase() + " : " + container.getHost()
				+ ":" + container.getPort() + "/" + container.getName() + ">>> ************************");
		if ("web".equalsIgnoreCase(container.getType())) {
			return new Web(container);
		} else if ("java".equalsIgnoreCase(container.getType())) {
			return new Java(container);
		} else if ("database".equalsIgnoreCase(container.getType())) {
			return new Database(container);
		} else if ("filesystem".equalsIgnoreCase(container.getType())) {
			return new FileSystem(container);
		} else if ("http".equalsIgnoreCase(container.getType())) {
			return new HTTP(container);
		}

		return null;
	}

}
