package com.waves.core;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.waves.util.MapUtil;

import akka.japi.function.Function;

public final class MapperFn implements Function {
	private Logger log = LoggerFactory.getLogger(MapperFn.class);

	private MapUtil.Mapper mapper = null;
	private Class fromClass = null;
	private Class toClass = null;
	private String path = null;

	@Override
	public Object apply(Object input) throws Exception {

		if (!fromClass.isInstance(input) && input != null)
			throw new Exception(
					"Mapper["+path+"] : Input Error -> expected: " + fromClass.getName() + " got: " + input.getClass().getName());
		log.debug("Mapper["+path+"] : Input ->  " + input.getClass().getName());
		Object output = mapper!=null?mapper.to(input, toClass):input;
		log.debug("Mapper["+path+"] : Output ->  " + output.getClass().getName());

		if (!toClass.isInstance(output) && output != null)
			throw new Exception(
					"Mapper["+path+"] : Output Error -> expected: " + toClass.getName() + " got: " + output.getClass().getName());

		return output;
	}

	public MapperFn(String p, String rootpath, Class toClass, Class fromClass) {
		this.toClass = toClass;
		this.fromClass = fromClass;
        this.path=p;
        
		String path = null;
		if (p != null) {
			if (p.endsWith(".xml") || p.endsWith(".properties"))
				path = Paths.get(rootpath, p).toString();
			else
				path = p;
			mapper = MapUtil.getInstance(path);
		}
	}
}