package com.waves.test;

import java.util.HashMap;
import java.util.Map;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class UserInfo2Map implements Converter< com.waves.test.UserInfo,Map<String, String>> {

	@Override
	public Map<String, String> convert(MappingContext<com.waves.test.UserInfo, Map<String, String>> context) {

		Map<String,String> info = new HashMap<String, String>();

		info.put("userid", context.getSource().getUserId());
		
		return info;
	}
}
