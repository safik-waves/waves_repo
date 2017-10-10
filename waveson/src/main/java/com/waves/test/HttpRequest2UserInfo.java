package com.waves.test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class HttpRequest2UserInfo implements Converter<HttpServletRequest, UserInfo> {

	@Override
	public UserInfo convert(MappingContext<HttpServletRequest, UserInfo> context) {

		UserInfo info = context.getDestination();
		HttpSession session = context.getSource().getSession(true);
		info.setUserId("safik");
		session.setAttribute("userInfo", info);

		return info;
	}
}
