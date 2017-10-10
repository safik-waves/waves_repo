package com.waves.test;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class Request2Map implements Converter<HttpServletRequest, Map<String, String>> {

	@Override
	public Map<String, String> convert(MappingContext<HttpServletRequest, Map<String, String>> context) {

		Map<String, String> map = new HashMap<>();

		if (context.getSource().getUserPrincipal() != null)
			map.putIfAbsent("userId", context.getSource().getUserPrincipal().getName());
		else
			map.putIfAbsent("userId", "David");

		map.put("roles", "User");

		return map;
	}
}
