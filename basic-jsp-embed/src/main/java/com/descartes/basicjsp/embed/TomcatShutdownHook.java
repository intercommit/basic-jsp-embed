package com.descartes.basicjsp.embed;

import java.util.concurrent.CountDownLatch;

import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * Shutdown hook which stops and destroys Tomcat, and closes the logger.
 * <br>Activated when JVM receives terminate signal (ctrl-C in Windows),
 * or when {@link LaunchWebApp#stopTomcat()} is called.
 * @author FWiers
 *
 */
public class TomcatShutdownHook extends Thread {
	
	private static final Logger log = LoggerFactory.getLogger(TomcatShutdownHook.class);

	private final Tomcat tomcat;
	private final CountDownLatch tomcatServerDestroyed;
	private volatile boolean stopRan;

	/**
	 * @param tomcat The Tomcat instance to stop and destroy.
	 */
	public TomcatShutdownHook(Tomcat tomcat) {
		this(tomcat, null);
	}

	/**
	 * @param tomcat The Tomcat instance to stop and destroy.
	 * @param tomcatServerDestroyed The latch on which the main-thread waits for Tomcat to stop running (can be null).
	 */
	public TomcatShutdownHook(Tomcat tomcat, CountDownLatch tomcatServerDestroyed) {
		super();
		this.tomcat = tomcat;
		this.tomcatServerDestroyed = tomcatServerDestroyed;
	}
	
	/**
	 * Executes the Tomcat stop and destroy methods.
	 * This method will only execute once.
	 */
	@Override
	public void run() {
		
		// protect against stopping twice
		synchronized(this) {
			if (stopRan) {
				return;
			}
			stopRan = true;
		}
		try {
			log.info("Shutting down ...");
			tomcat.stop();
			tomcat.destroy();
			log.info("Shutdown complete.");
		} catch (Exception e) {
			log.error("Stopping embedded Tomcat instance failed.", e);
		} finally {
			if (tomcatServerDestroyed != null) {
				// Just in case tomcat.destroy did not fire the destroy-event. 
				tomcatServerDestroyed.countDown();
			}
			stopLoggerContext();
		}
	}

	/**
	 * Stopping the Logback logger-context is recommended when application exits.
	 * See also <a href="http://logback.qos.ch/manual/configuration.html#stopContext">Stopping logback-classic</a>
	 */
	public void stopLoggerContext() {
		
		try {
			// assume SLF4J is bound to logback-classic in the current environment
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			loggerContext.stop();
		} catch (Exception e) {
			System.out.println("Failed to stop logger context: " + e);
		}
	}

}
