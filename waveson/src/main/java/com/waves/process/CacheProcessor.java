package com.waves.process;

import com.waves.actor.AbstractProcessActor;
import com.waves.model.Macro;

public class CacheProcessor extends AbstractProcessActor {

	public CacheProcessor(Macro data) {
		super(data);
	}

	protected Object execute(Object p) throws Exception {
		return getMacro().getData();

	}

}
