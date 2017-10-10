package com.waves.actor;

import static akka.pattern.PatternsCS.ask;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.waves.model.Stream;
import com.waves.model.Wave;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.Status;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import akka.util.Timeout;

public class WaveStreamActor extends AbstractActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private Stream stream = null;

	private void l(String msg, Object data) {
		log.info(stream + ":" + getSelf().path() + ":" + msg + ":" + data);
	}

	private SupervisorStrategy strategy = new OneForOneStrategy(false, DeciderBuilder.match(Exception.class, e -> {
		l("Exception . restarting.", e);
		return SupervisorStrategy.stop();
	}).match(Exception.class, e -> {
		l("Exception . restarting.", e);
		notifyConsumerFailure(sender(), e);
		return SupervisorStrategy.stop();
	}).match(Throwable.class, e -> {
		l("Exception . restarting.", e);
		notifyConsumerFailure(sender(), e);
		return SupervisorStrategy.stop();
	}).build());

	@Override
	public SupervisorStrategy supervisorStrategy() {
		return strategy;
	}

	private void notifyConsumerFailure(ActorRef worker, Throwable failure) {
		l("Exception  : " + worker, failure);
		worker.tell(new Status.Failure(failure), getSelf());
	}

	private void notifyConsumerSuccess(ActorRef sender, Object result) throws Exception {
		String url = null;
		Timeout t = new Timeout(3000, TimeUnit.MILLISECONDS);
		if (stream.getContainer() != null) {
			url = "akka.tcp://" + stream.getContainer() + "@" + stream.getHost() + ":" + stream.getPort() + "/user/"
					+ stream.getWave().replaceAll("/", "-");
			ActorSelection selection = getContext().actorSelection(url);
			l(" Stream to Remote Container " + url, " ---> " + selection);
			CompletionStage<Object> f = ask(selection, "ref", t);
			ActorRef ref = (ActorRef) f.toCompletableFuture().get();
			ref.tell(result, sender);
		} else {
			url =  "/user/" + stream.getWave().replaceAll("/", "-");
			ActorSelection selection = getContext().actorSelection(url);
			l(" Stream to Local Container " + url, " ---> " + selection);
			CompletionStage<Object> f = ask(selection, result, t);
			ActorRef ref = (ActorRef) f.toCompletableFuture().get();
			ref.tell(result, sender);
		}

	}

	public WaveStreamActor(Stream stream) {
		this.stream = stream;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().matchAny(expr -> {
			notifyConsumerSuccess(sender(), expr);
		}).build();
	}

	public static Props props(Stream w) {
		return Props.create(WaveStreamActor.class, () -> new WaveStreamActor(w));
	}

}
