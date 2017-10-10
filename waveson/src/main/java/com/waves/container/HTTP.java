package com.waves.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.waves.core.AbstractWavesContainer;
import com.waves.model.Component;
import com.waves.model.Container;
import com.waves.model.Interface;

import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.IncomingConnection;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.ResponseEntity;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.japi.Pair;
import akka.japi.function.Function;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class HTTP extends AbstractWavesContainer {

	Logger log = LoggerFactory.getLogger(HTTP.class);

	private String name = null;

	public HTTP(Container container) {
		super(container, false);
		this.name = "/" + container.getName();
	}

	List<String> gets = new ArrayList<>();
	List<String> posts = new ArrayList<>();
	List<String> deletes = new ArrayList<>();
	List<String> puts = new ArrayList<>();

	@Override
	public void build(Component component) throws IOException {

		for (Interface i : component.getInterface()) {
			if ("get".equalsIgnoreCase(i.getType()))
				gets.add(name + "/" + i.getOn());
			else if ("post".equalsIgnoreCase(i.getType()))
				posts.add(name + "/" + i.getOn());
			else if ("put".equalsIgnoreCase(i.getType()))
				puts.add(name + "/" + i.getOn());
			else if ("delete".equalsIgnoreCase(i.getType()))
				deletes.add(name + "/" + i.getOn());
		}

	}

	class HttpServer extends AllDirectives {

		private Route createRoute() {
			return route(path("hello", () -> get(() -> complete("<h1>Say hello to akka-http</h1>"))));
		}
	}

	@Override
	public boolean create(Container container) throws IOException {
		try {

			Http http = Http.get(container.getActorSystem());
			ConnectHttp connection = ConnectHttp.toHost("localhost", 9090);
			Source<IncomingConnection, CompletionStage<ServerBinding>> serverSource = http.bind(connection,
					container.getMaterializer());

			Flow flow = http.outgoingConnection(connection);

			serverSource.map(new Function<IncomingConnection, HttpResponse>() {

				@Override
				public HttpResponse apply(IncomingConnection in) throws Exception {

					getContainer().wave(name, in, flow,Sink.ignore());

					return null;
				}

			});

		} catch (

		Exception e) {
			e.printStackTrace();
		}

		return true;
	}

}
