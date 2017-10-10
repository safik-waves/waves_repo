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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ServerBinding;

public class Java extends AbstractWavesContainer {

	Logger log = LoggerFactory.getLogger(Java.class);

	Map<String, Interface> methods = new HashMap<>();

	public Java(Container container) {
		super(container);
	}

	@Override
	public boolean create(Container container) throws IOException {
		return false;
	}

	@Override
	public void build(Component comp) throws IOException {

		if (comp != null)
			for (Interface i : comp.getInterface()) {
				for (Macro macro : i.getMacros()) {
					register(JavaMethodProcessor.class, macro, i);
				}

			}

	}

}
