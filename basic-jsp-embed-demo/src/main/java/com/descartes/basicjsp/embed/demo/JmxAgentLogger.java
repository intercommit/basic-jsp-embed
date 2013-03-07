package com.descartes.basicjsp.embed.demo;

import org.jolokia.util.LogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxAgentLogger implements LogHandler {

	private static final Logger log = LoggerFactory.getLogger(JmxAgentLogger.class);

	@Override
	public void debug(String message) {
		log.debug(message);
	}

	@Override
	public void info(String message) {
		log.info(message);
	}

	@Override
	public void error(String message, Throwable t) {
		log.error(message, t);
	}

}
