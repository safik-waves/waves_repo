package com.waves.process;

import akka.NotUsed;
import akka.actor.*;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Optional;

public class WebSocketServer {

	private static final class Router extends HttpApp {

		private final ActorSystem system;

		public Router(ActorSystem system) {
			this.system = system;
		}

		private Flow<Message, Message, NotUsed> createWebSocketFlow() {
			ActorRef actor = system.actorOf(Props.create(AnActor.class));

			Source<Message, NotUsed> source = Source.<Outgoing>actorRef(5, OverflowStrategy.fail())
					.map((outgoing) -> (Message) TextMessage.create(outgoing.message))
					.<NotUsed>mapMaterializedValue(destinationRef -> {
						actor.tell(new OutgoingDestination(destinationRef), ActorRef.noSender());
						return NotUsed.getInstance();
					});

			Sink<Message, NotUsed> sink = Flow.<Message>create()
					.map((msg) -> new Incoming(msg.asTextMessage().getStrictText()))
					.to(Sink.actorRef(actor, PoisonPill.getInstance()));

			return Flow.fromSinkAndSource(sink, source);
		}

		@Override
		protected Route routes() {
			return null;
		//	return route(path("test").route(get(handleWebSocketMessages(createWebSocketFlow()))));
		}

	}

	public static void main(String[] args) {
		ActorSystem actorSystem = ActorSystem.create();

		Router router = new Router(actorSystem);
	//	router.bindRoute("127.0.0.1", 8082, actorSystem);
	}

	static class Incoming {
		public final String message;

		public Incoming(String message) {
			this.message = message;
		}
	}

	static class Outgoing {
		public final String message;

		public Outgoing(String message) {
			this.message = message;
		}
	}

	static class OutgoingDestination {
		public final ActorRef destination;

		OutgoingDestination(ActorRef destination) {
			this.destination = destination;
		}
	}

	static class AnActor extends AbstractActor {

		private Optional<ActorRef> outgoing = Optional.empty();

		public AnActor() {

		}

		@Override
		public Receive createReceive() {
			// TODO Auto-generated method stub
			return ReceiveBuilder.create()
					.match(OutgoingDestination.class, (msg) -> outgoing = Optional.of(msg.destination))
					.match(Incoming.class,
							(in) -> outgoing.ifPresent((out) -> out.tell(new Outgoing("got it"), self())))
					.build();
		}
	}
}