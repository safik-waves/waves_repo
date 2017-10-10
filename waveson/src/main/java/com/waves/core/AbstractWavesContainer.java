package com.waves.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.waves.actor.AbstractProcessActor;
import com.waves.container.Database;
import com.waves.ex.WavesInitException;
import com.waves.model.Component;
import com.waves.model.Container;
import com.waves.model.Interface;
import com.waves.model.Macro;
import com.waves.model.Waves;
import com.waves.process.JavaMethodProcessor;

import akka.actor.ActorRef;
import akka.actor.Props;

public abstract class AbstractWavesContainer implements WavesContainer {

	Logger log = LoggerFactory.getLogger(AbstractWavesContainer.class);

	public Monitor getMonitor() {
		return null;
	}

	private Container container = null;

	public Container getContainer() {
		return container;
	}

	public AbstractWavesContainer(Container container) {
		this(container, true);
	}

	public AbstractWavesContainer(Container container, boolean processExist) {
		this.container = container;
		try {
			log.info("Initializing... " + container.getName());
			this.container.init(processExist);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start() {
		try {
			if (!create(this.container)) {
				for (Waves w : container.getWaves()) {
					for (Component com : w.getComponent()) {
						Component comp = container.loadComponent(com.getPath());
						build(comp);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public void destory() {
		this.container.getActorSystem().terminate();
	}

	protected void register(Class<? extends AbstractProcessActor> cls, Macro macro, Interface i) {

		ActorRef ref = this.container.getActorSystem().actorOf(AbstractProcessActor.props(cls, macro),
				i.getOn() + "-process");
	}
}
