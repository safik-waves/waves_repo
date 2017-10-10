package com.waves.actor;

import com.waves.model.Wave;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.Status;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;

public class WaveActor extends AbstractActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private Wave wave = null;

	private long execution_count = 0;

	private long access_count = 0;

	private void l(String msg, Object data) {
		log.info(wave + ":" + getSelf().path() + " : " + msg + " : " + data);
	}

	private SupervisorStrategy strategy = new OneForOneStrategy(false, DeciderBuilder.match(Exception.class, e -> {
		log.warning("Evaluation of a top level expression failed, restarting." + e);
		failure(e);
		return SupervisorStrategy.stop();
	}).match(Exception.class, e -> {
		log.error("Evaluation failed because of: {}", e.getMessage());
		failure(e);
		return SupervisorStrategy.stop();
	}).match(Throwable.class, e -> {
		log.error("Unexpected failure: {}", e.getMessage());
		failure(e);
		return SupervisorStrategy.stop();
	}).matchAny(e -> SupervisorStrategy.escalate()).build());

	@Override
	public SupervisorStrategy supervisorStrategy() {
		return strategy;
	}

	private void failure(String msg) {
		failure(new Exception(msg));
	}

	private void failure(Throwable failure) {
		failure.printStackTrace();
		getSender().tell(new Status.Failure(failure), getSelf());
	}

	@Override
	public void preStart() throws Exception {
		l("preStart()", "....");

	}

	public WaveActor(Wave wave) throws Exception {
		this.wave = wave;

	}

	@Override
	public Receive createReceive() {

		ReceiveBuilder builder = ReceiveBuilder.create();
		builder.matchEquals("wave", this::wave);
		builder.matchEquals("ref", this::ref);
		Receive r = builder.build();
		return r;

	}

	public void wave(Object object) {
		l("Returning Wave Model", object.getClass().getName());
		access_count++;
		getSender().tell(wave, getSelf());
	}

	public void count(Object object) {
		l("Returning Execution Count", object.getClass().getName());
		getSender().tell(wave, getSelf());
	}

	public void ref(Object object) {
		execution_count++;
		l("Returning Wave Reference", object.getClass().getName());
		ActorRef ref = getContext().actorOf(WaveExecuteActor.props(wave));
		getSender().tell(ref, getSelf());
	}

	public static Props props(Wave w) {
		return Props.create(WaveActor.class, () -> new WaveActor(w));
	}

}
