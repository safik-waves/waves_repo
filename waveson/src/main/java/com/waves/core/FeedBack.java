package com.waves.core;

import java.util.concurrent.CompletionStage;

import com.waves.model.Input;
import com.waves.model.Wave;

import akka.Done;
import akka.stream.javadsl.Sink;

public interface FeedBack {

	Object input(Input model,Object input) throws Exception;
	void output(Wave model,Object output,Object result) throws Exception;
}
