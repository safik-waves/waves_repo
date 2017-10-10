package com.waves.test;

import java.util.List;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class ServiceInfo2String implements Converter<List<ServiceInfo>, StringBuilder> {

	@Override
	public StringBuilder convert(MappingContext<List<ServiceInfo>, StringBuilder> context) {
		StringBuilder reader = context.getDestination();
		reader.append("{ \"data\" :[");
		int i = 0;
		List<ServiceInfo> list = context.getSource();
		for (ServiceInfo info : list) {
			i++;
			reader.append("[");

			reader.append("\"" + info.getName() + "\",");
			reader.append("\"" + info.getType() + "\",");
			reader.append("\"" + info.getHost() + "\",");
			reader.append("\"" + info.getPort() + "\",");
			reader.append("\"" + info.getConfigPath() + "\",");
			reader.append("\"" + info.getWaveLength() + "\",");
			reader.append("\"" + info.getCompSize() + "\"");

			reader.append("]");
			if (i < list.size() )
				reader.append(",");
		}

		reader.append("]}");
		return reader;
	}
}
