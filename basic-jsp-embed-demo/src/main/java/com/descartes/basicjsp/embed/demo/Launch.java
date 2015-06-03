package com.descartes.basicjsp.embed.demo;

import org.apache.catalina.webresources.StandardRoot;

import com.descartes.basicjsp.embed.LaunchWebApp;

public class Launch extends LaunchWebApp {
	
	public static void main(String[] args) {
		
		try {
			new Launch().start("", 0);
		} catch (Exception e) {
			log.error("Failed to start web application.", e);
		}
	}

	@Override
	public void configure() {
		
		super.configure();
		// only reload when testing
		setReloadable(isMavenTest());
	}
	
	@Override
	public void addResources(StandardRoot webResources) {
		
		super.addResources(webResources);
		addResourceJar(webResources, LaunchWebApp.class);
	}

}
