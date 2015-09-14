package com.descartes.basicjsp.embed.demo;

import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.basicjsp.embed.LaunchWebApp;

public class Launch extends LaunchWebApp {
	
	private static final Logger log = LoggerFactory.getLogger(Launch.class);

	public static void main(String[] args) {
		
		try {
			new Launch().start("", 0);
		} catch (Exception e) {
			log.error("Failed to start web application.", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addResources(StandardRoot webResources) {
		
		super.addResources(webResources);
		addResourceJar(webResources, LaunchWebApp.class);
	}

}
