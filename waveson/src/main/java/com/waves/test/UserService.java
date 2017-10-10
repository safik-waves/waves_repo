package com.waves.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.waves.core.WavesContainer;
import com.waves.model.Component;
import com.waves.model.Container;
import com.waves.model.Domain;
import com.waves.model.Wave;
import com.waves.model.Waves;

public class UserService {

	public List<ServiceInfo> getAllServices(ArrayList<String> files) throws Exception {

		System.out.println("Get All Services ...." + files);
		List<ServiceInfo> list = new ArrayList<>();

		for (String f : files) {
			JAXBContext jc = JAXBContext.newInstance(Domain.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			Domain domain = (Domain) unmarshaller.unmarshal(new File(f));
			List<Container> cont = domain.getContainer();
			for (Container c : cont) {
				ServiceInfo s = new ServiceInfo();
				s.setDomainName(new File(f).getName());
				s.setName(c.getName());
				s.setPort(c.getPort());
				s.setConfigPath(c.getPath());
				s.setHost(c.getHost());
				s.setTimeout(c.getTimeout());
				s.setType(c.getType());
				s.setWaveLength(c.getWaves().size());
				if (c.getWaves() != null) {
					for (Waves w : c.getWaves()) {
						if (w.getComponent() != null)
							s.setCompSize(s.getCompSize() + w.getComponent().size());
					}
				}
				list.add(s);
			}

		}
		return list;
	}

	public String monitor(UserInfo userInfo) {
		return "monitored";
	}

}
