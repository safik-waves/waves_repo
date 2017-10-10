package com.waves.process;

import com.waves.actor.AbstractProcessActor;
import com.waves.model.Macro;

public class SQLProcessor extends AbstractProcessActor {

	public SQLProcessor(Macro data) {
		super(data);
	}

	protected Object execute(Object p) throws Exception {
		if ("insert".equalsIgnoreCase(getMacro().getTo())) {

		} else if ("update".equalsIgnoreCase(getMacro().getTo())) {

		}
		return null;
	}

}
