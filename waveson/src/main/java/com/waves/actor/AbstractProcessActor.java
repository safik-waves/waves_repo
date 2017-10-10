package com.waves.actor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.waves.model.Interface;
import com.waves.model.Macro;
import com.waves.process.JavaMethodProcessor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

public abstract class AbstractProcessActor extends AbstractActor {

	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	protected void log(String msg, Object data) {
		log.info(getSelf().path() + " : " + msg + " : " + data);
	}

	private Macro macro = null;

	public AbstractProcessActor(Macro macro) {
		this.macro = macro;
	}

	public Macro getMacro() {
		return macro;
	}

	@Override
	public void preStart() throws Exception {
		super.preStart();
		log("PreStart Builder : " + getClass().getSimpleName(), getMacro());
		init();
	}

	protected void init() {

	}

	protected abstract Object execute(Object data) throws Exception;

	private void executeWrapper(Object obj) throws Exception {
		log(" Processing Unit : Execute Start : " + obj.getClass().getSimpleName(), " ---> ");
		Object object = execute(obj);
		log(" Processing Unit : Execute End :" + object.getClass().getSimpleName(), " <--- ");
		getSender().tell(object, getSelf());
	}

	@Override
	public Receive createReceive() {
		ReceiveBuilder builder = ReceiveBuilder.create();
		builder.matchAny(this::executeWrapper);
		Receive r = builder.build();
		getContext().become(r, false);
		return r;

	}

	public static Props props(Class<? extends AbstractProcessActor> cls, Macro w) {
		return Props.create(cls, new Creator() {

			@Override
			public Object create() throws Exception {

				Class[] params = new Class[1];
				Object[] values = new Object[1];

				Object object = null;
				Constructor<? extends AbstractProcessActor> mth = cls.getConstructor(Macro.class);
				if (mth != null) {
					object = mth.newInstance(w);
				}
				return object;
			}
		});
	}
}
