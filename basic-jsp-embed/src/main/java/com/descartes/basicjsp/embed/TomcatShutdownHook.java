package com.descartes.basicjsp.embed;

/**
 * Waits for Tomcat to stop running.
 * Activcated when JVM receives terminate signal (ctrl-C in Windows).
 * @author FWiers
 *
 */
public class TomcatShutdownHook extends Thread {
	
	private TomcatStopper tomcatStopper;

	public TomcatShutdownHook(TomcatStopper tomcatStopper) {
		super();
		this.tomcatStopper = tomcatStopper;
	}
	
	@Override
	public void run() {
		
		if (tomcatStopper != null) {
			tomcatStopper.stopTomcat();
		}
	}

}
