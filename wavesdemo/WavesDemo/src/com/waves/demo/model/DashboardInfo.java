package com.waves.demo.model;

import java.util.ArrayList;
import java.util.List;

public class DashboardInfo {

	private List<Domain> domains=new ArrayList<Domain>();
	
	public void setDomains(List<Domain> domains) {
		this.domains = domains;
	}
	
	public List<Domain> getDomains() {
		return domains;
	}
}
