package com.waves.util;

import java.util.Arrays;

import org.dozer.DozerBeanMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;

public class MapUtil {

	public interface Mapper {

		String getName();

		Object to(Object o, Class c) throws Exception;
	}

	static class XMLMapper implements Mapper {

		String xml = null;

		org.dozer.Mapper mapper = null;

		public XMLMapper(String xml) {
			this.xml = xml;
			mapper = new DozerBeanMapper(Arrays.asList(xml));
		}

		public Object to(Object source, Class dest) {
			return mapper.map(source, dest);
		}

		@Override
		public String getName() {
			return DozerBeanMapper.class.getSimpleName();
		}
	}

	@org.mapstruct.Mapper
	static class PropertyMapper implements Mapper {

		private String property = null;

		public PropertyMapper(String property) {
			this.property = property;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object to(Object o, Class c) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	static class ClassMapper implements Mapper {

		Class cls = null;
		ModelMapper modelMapper = new ModelMapper();

		public ClassMapper(Class cls) {
			this.cls = cls;
			modelMapper.getConfiguration().setFieldMatchingEnabled(true)
					.setFieldAccessLevel(AccessLevel.PACKAGE_PRIVATE);
		}

		@Override
		public String getName() {
			return ModelMapper.class.getSimpleName();
		}

		@Override
		public Object to(Object source, Class dest) throws Exception {

			modelMapper.createTypeMap(source.getClass(), dest).setPostConverter((Converter) cls.newInstance());

			return modelMapper.map(source, dest);
		}

	}

	public static Mapper getInstance(String path) {

		Mapper mapper = null;
		if (path != null) {

			if (path.endsWith(".xml")) {
				mapper = new XMLMapper(path);
			} else if (path.endsWith(".properties")) {
				mapper = new PropertyMapper(path);
			} else {
				try {
					Class cls = Class.forName(path);
					mapper = new ClassMapper(cls);

				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

			}
		}
		return mapper;
	}

}
