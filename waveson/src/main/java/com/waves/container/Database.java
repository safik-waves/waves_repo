package com.waves.container;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.waves.core.AbstractWavesContainer;
import com.waves.model.Component;
import com.waves.model.Container;
import com.waves.model.Interface;
import com.waves.model.Macro;
import com.waves.model.Waves;
import com.waves.process.JavaMethodProcessor;
import com.waves.process.SQLProcessor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ServerBinding;


public class Database extends AbstractWavesContainer {

	Logger log = LoggerFactory.getLogger(Database.class);

	Map<String, Interface> methods = new HashMap<>();

	private ActorSystem system = null;
	private String hook = "-method";

	public Database(Container container) {
		super(container);
	}

	@Override
	public boolean create(Container container) throws IOException {
		system = container.getActorSystem();
		return false;
	}

	@Override
	public void build(Component comp) throws IOException {

		if (comp != null)
			for (Interface i : comp.getInterface()) {
				for (Macro m : i.getMacros()) {
					i.setPath(getContainer().getRootPath());
					register(SQLProcessor.class, m, i);
				}
			}

	}

	public void destory() {
		system.terminate();
	}

}
