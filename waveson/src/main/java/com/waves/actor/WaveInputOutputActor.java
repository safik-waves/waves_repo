package com.waves.actor;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import com.waves.core.MapperFn;
import com.waves.model.Input;
import com.waves.model.Output;
import com.waves.model.Stream;

import akka.Done;
import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.function.Function;
import akka.japi.function.Procedure;
import akka.japi.pf.DeciderBuilder;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import akka.stream.ActorMaterializer;
import akka.stream.Graph;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.scaladsl.GraphDSL;

public class WaveInputOutputActor extends AbstractActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private Class onClass = null;
	private Class toClass = null;
	private Object model = null;

	private Router routers;

	private boolean processCompleted = false;

	@Override
	public void preStart() {
		l("preStart()", this);
		processCompleted = false;
	}

	private SupervisorStrategy strategy = new OneForOneStrategy(false,
			DeciderBuilder
					// .match(Exception.class, e -> {
					// log.warning("Evaluation of {} failed, restarting.");
					// return SupervisorStrategy.restart();
					// })
					.matchAny(e -> SupervisorStrategy.escalate()).build());

	@Override
	public SupervisorStrategy supervisorStrategy() {
		return strategy;
	}

	public WaveInputOutputActor(Input input) throws Exception {
		this.model = input;
		l("Input Unit : Initialize " + input.getClass().getName(), "--->");
		if (input.getStreams() != null && input.getStreams().size() > 0) {
			routing(input.getStreams());
		}

		if (input.getOn() != null)
			this.onClass = Class.forName(input.getOn());
		else
			this.onClass = String.class;

		if (input.getTo() != null)
			this.toClass = Class.forName(input.getTo());
		else
			this.onClass = String.class;

		flow = Flow.fromFunction(new MapperFn(input.getType(), input.getRootPath(), toClass, onClass));

	}

	private void routing(List<Stream> input) {
		List<Routee> routees = new ArrayList<>();
		for (Stream stream : input) {
			ActorRef ref = getContext().actorOf(WaveStreamActor.props(stream), stream.getWave().replaceAll("/", "-"));
			routees.add(new ActorRefRoutee(ref));
		}

		routers = new Router(new BroadcastRoutingLogic(), routees);
	}

	private Flow<?, ?, NotUsed> flow = null;

	public WaveInputOutputActor(Output input) throws Exception {
		this.model = input;
		l("Output Unit : Init " + input.getClass().getSimpleName(), "<---");
		if (input.getStreams() != null && input.getStreams().size() > 0) {
			routing(input.getStreams());
		}

		if (input.getOn() != null)
			this.onClass = Class.forName(input.getOn());
		else
			this.onClass = String.class;

		if (input.getTo() != null)
			this.toClass = Class.forName(input.getTo());
		else
			this.onClass = String.class;

		flow = Flow.fromFunction(new MapperFn(input.getType(), input.getRootPath(), toClass, onClass));

	}

	private void l(String msg, Object data) {
		log.info(model + ":" + getSelf().path() + ":" + msg + ":" + data);
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(onClass, this::onRequest).matchAny(this::onReturn).build();
	}

	private void onReturn(Object request) throws Exception {

		getContext().getParent().tell(request, getSelf());
		l("Return Object sent to Parent : " + getContext().getParent(), "<---");

	}

	private void onRequest(Object request) throws Exception {

		if (processCompleted) {
			getContext().parent().tell(request, getSelf());

			l("IO Execute : End " + request.getClass().getName(), "<---" + getContext().parent());

		} else {
			l("IO Execute :  on  " + request.getClass().getName(), "--->");

			Object transformed = null;
			Source<?, NotUsed> source = null;

			if (onClass.isInstance(request)) {
				source = Source.single(request);
			} else if (Iterable.class.isInstance(request)) {
				source = Source.from((Iterable<?>) request);
			} else {
				source = Source.single(request);
			}

			Sink<Object, CompletionStage<Done>> sink = Sink.foreach(new Procedure<Object>() {

				@Override
				public void apply(Object transformed) throws Exception {
					if (routers != null) {

						l("IO Execute : Streaming : " + routers, transformed.getClass().getName() + "--->");
						routers.route(transformed, getSelf());

					} else {

						getContext().parent().tell(transformed, getSelf());
						l("IO Execute : End " + transformed.getClass().getName(), "<---" + getContext().parent());

					}

				}

			});

			// flow.to((Graph) sink).runWith(source,
			// ActorMaterializer.create(getContext()));
			source.via((Graph) flow).runWith((Graph) sink, ActorMaterializer.create(getContext()));

			// else {
			//
			// getContext().parent().tell(transformed, getSelf());
			// l("IO Execute : End " + transformed.getClass().getName(), "<---"
			// + getContext().parent());
			//
			// }
			l("IO Execute :  on  " + request.getClass().getName(), "<---");
			processCompleted = true;
		}

	}

	public static Props props(Input input) {
		return Props.create(WaveInputOutputActor.class, () -> new WaveInputOutputActor(input));
	}

	public static Props props(Output output) {
		return Props.create(WaveInputOutputActor.class, () -> new WaveInputOutputActor(output));
	}
}
