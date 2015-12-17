package com.descartes.basicjsp.embed.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.basicjsp.embed.LaunchWebApp;
import com.descartes.basicjsp.embed.WebResourcePaths;

public class Launch extends LaunchWebApp {
	
	private static final Logger log = LoggerFactory.getLogger(Launch.class);

	public static void main(String[] args) {
		
		try {
			new Launch().start("/demo", 0);
		} catch (Exception e) {
			log.error("Failed to start web application.", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public WebResourcePaths createAppResources() {
		
		WebResourcePaths wrj = super.createAppResources();
		// enable re-use of pages and resources from basic-jsp-embed
		wrj.addWebJarPath(LaunchWebApp.class);
		return wrj;
	}

}
