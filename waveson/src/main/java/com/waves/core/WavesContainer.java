package com.waves.core;

import java.io.IOException;

import com.waves.model.Component;
import com.waves.model.Container;

public interface WavesContainer {

	void start();

	boolean create(Container container) throws Exception;

	void build(Component component) throws Exception;

	void destory();

	Monitor getMonitor();
}
