package com.waves.process;

import java.lang.reflect.Method;

import com.waves.actor.AbstractProcessActor;
import com.waves.model.Interface;
import com.waves.model.Macro;

public class JavaMethodProcessor extends AbstractProcessActor {

	public JavaMethodProcessor(Macro w) {
		super(w);
	}

	protected Object execute(Object p) throws Exception {

		Class cls = Class.forName(getMacro().getPath());
		Class[] params = new Class[1];
		Object[] values = new Object[1];

		params[0] = p.getClass();
		values[0] = p;

		Object object = null;
		Method mth = cls.getMethod(getMacro().getTo(), params);
		if (mth != null) {
			Object obj = cls.newInstance();
			object = mth.invoke(obj, values);

		}
		log(" Return object for " + getMacro().getTo(), object);
		return object;
	}

}
