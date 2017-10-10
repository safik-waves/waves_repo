package com.waves.container;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.waves.core.AbstractWavesContainer;
import com.waves.core.Builder;
import com.waves.model.Component;
import com.waves.model.Container;
import com.waves.model.Interface;
import com.waves.model.Wave;
import com.waves.model.Waves;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.http.javadsl.ServerBinding;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

public class REST extends AbstractWavesContainer {

	public REST(Container c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	Logger log = LoggerFactory.getLogger(REST.class);

	@Override
	public boolean create(Container container) throws IOException {
		return false;
	}

	@Override
	public void build(Component component) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destory() {
		// TODO Auto-generated method stub

	}

}
