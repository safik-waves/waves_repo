package com.waves.test;

import java.util.Map;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class Map2UserInfo implements Converter<Map<String, String>, com.waves.test.UserInfo> {

	@Override
	public UserInfo convert(MappingContext<Map<String, String>, com.waves.test.UserInfo> context) {

		UserInfo info = new UserInfo();

		if (context.getSource().get("userId") != null)
			info.setUserId(context.getSource().get("userId"));
		else
			info.setUserId("David");

		info.setFirstName("First");
		info.setLastName("Last");

		return info;
	}
}
