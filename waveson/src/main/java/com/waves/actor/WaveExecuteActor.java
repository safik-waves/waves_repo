package com.waves.actor;

import java.nio.file.Paths;

import com.waves.model.Wave;

import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.Status;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.function.Function;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.javadsl.Flow;

public class WaveExecuteActor extends AbstractActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private Wave wave = null;

	private void l(String msg, Object data) {
		log.info(wave + ":" + getSelf().path() + ":" + msg + ":" + data);
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

	private Class inputOnCls = null;
	private Class outputOnCls = null;
	private Class inputToCls = null;
	private Class outputToCls = null;

	private boolean outputUnitCompleted = false;
	private boolean inputUnitCompleted = false;
	private ActorRef sender = null;
	private boolean processCompleted = false;

	@Override
	public void preStart() throws Exception {
		l("preStart()", "....");
		outputUnitCompleted = false;
		inputUnitCompleted = false;
		ActorRef sender = null;
		processCompleted = false;
	}

	public WaveExecuteActor(Wave wave) throws Exception {
		log.info("Executing Wave : "+wave);
		this.wave = wave;
		if (wave.getInput() != null) {
			wave.getInput().setRootPath(wave.getRootPath());
			inputOnCls = Class.forName(wave.getInput().getOn());
			if (wave.getInput().getTo() != null)
				inputToCls = Class.forName(wave.getInput().getTo());

		}

		if (wave.getOutput() != null) {
			wave.getOutput().setRootPath(wave.getRootPath());
			outputOnCls = Class.forName(wave.getOutput().getOn());
			if (wave.getOutput().getTo() != null)
				outputToCls = Class.forName(wave.getOutput().getTo());
		}

	}

	private void process(Object obj) throws Exception {
		processCompleted = true;
		if (wave.isProcessExist()) {

			ActorSelection selection = getContext()
					.actorSelection("/user/" + wave.getOn().replaceAll("/", "-") + "-process");

			l(" Processing Unit :" + obj.getClass().getSimpleName(), " ---> " + selection);

			if (selection != null) {
				selection.tell(obj, getSender());
			} else {
				l(" NO Processing Unit :" + obj.getClass().getSimpleName(), " ---> " + selection);

			}
		} else {
			l(" NO Processing Unit : Forward to Receive Block " + obj.getClass().getSimpleName(), " .... ");
			getSelf().forward(obj, getContext());
		}

	}

	private void input(Object input) {
		wave.getInput().setRootPath(wave.getRootPath());
		ActorRef inputRef = getContext().actorOf(WaveInputOutputActor.props(wave.getInput()), "input");
		l(" Input Unit :" + input.getClass().getSimpleName(), " ---> " + inputRef.path());

		sender = sender();
		inputRef.tell(input, sender());
		inputUnitCompleted = true;
	}

	private void output(Object output) {

		outputUnitCompleted = true;
		ActorRef outputRef = getContext().actorOf(WaveInputOutputActor.props(wave.getOutput()), "output");
		l(" Output unit :" + output.getClass().getSimpleName(), " <--- " + outputRef);
		if (sender == null)
			sender = sender();
		outputRef.tell(output, sender);

	}

	private void returnObj(Object obj) {
		if (outputToCls != null) {
			if (outputToCls.isInstance(obj)) {
				sender.tell(obj, getSelf());
			} else {
				// failure("Output Object Type Invalid :" +
				// obj.getClass().getSimpleName() + " Excepted :"
				// + outputToCls.getSimpleName());
				l("TODO", "To Class is not matching still returning this object");
				// getSender().tell(obj, getSelf());
			}
		} else {
			sender.tell(obj, getSelf());
		}

	}

	@Override
	public Receive createReceive() {

		ReceiveBuilder builder = ReceiveBuilder.create();

		// do some other stuff in between
		if (inputOnCls != null)

			builder.match(inputOnCls, input -> {
				if (!inputUnitCompleted) {
					input(input);
				} else {
					if (!processCompleted) {
						process(input);
					} else if (!outputUnitCompleted && outputOnCls != null && outputOnCls.isInstance(input)) {
						output(input);
					} else {
						returnObj(input);
					}
				}
			});

		if (outputOnCls != null)
			builder.match(outputOnCls, output -> {
				if (!outputUnitCompleted && !processCompleted) {
					process(output);
				} else if (!outputUnitCompleted && outputOnCls.isInstance(output)) {
					output(output);
				} else {
					returnObj(output);
				}
			});

		builder.matchAny(this::returnObj);
		Receive r = builder.build();
		return r;

	}

	public static Props props(Wave w) {
		return Props.create(WaveExecuteActor.class, () -> new WaveExecuteActor(w));
	}

}
