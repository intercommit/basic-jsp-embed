/*  Copyright 2013 Descartes Systems Group
*
*  This file is part of the "BasicJspEmbed" project hosted on https://github.com/intercommit/basic-jsp-embed
*
*  BasicJspEmbed is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  any later version.
*
*  BasicJspEmbed is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with BasicJspEmbed.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package com.descartes.basicjsp.embed;

import java.util.concurrent.Semaphore;

import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;

/**
 * A non-daemon thread that waits for a call to {@link #stopTomcatAsync()} after it is started.
 * Shutting down the web-application from within the web-application creates a chicken-egg problem, 
 * this class prevents that. When the web-application calls <tt>tomcat.stop()</tt>, or starts a thread that
 * calls <tt>tomcat.stop()</tt>, Tomcat will complain that a web-application thread is hanging
 * (this is logged as error).
 * <br>This class works asynchroneous: when {@link #stopTomcatAsync()} is called,
 * the calling thread returns immediatly and this thread will continue running (stopping Tomcat).
 * <br>Another little trick is required to trigger stopping Tomcat, see {@link LaunchWebApp#stopTomcatFromBoot()}. 
 * @author fwiers
 *
 */
public class TomcatStopper extends Thread {

	private Tomcat tomcat;
	private Logger log;
	private Semaphore stopWait;
	private Semaphore stopDone;
	private boolean wasStopped;

	public TomcatStopper(Tomcat tomcat) {
		super();
		this.tomcat = tomcat;
		this.log = LaunchWebApp.log;
		setDaemon(false);
		stopWait = new Semaphore(0);
		stopDone = new Semaphore(0);
	}
	
	/**
	 * Triggers Tomcat to stop running, blocking until Tomcat has stopped running.
	 * See also {@link #stopTomcatAsync()}.
	 */
	public void stopTomcat() {
		
		if (!wasStopped) {
			stopWait.release();
			try {
				stopDone.acquire();
			} catch (Exception e) {
				log.warn("Waiting for Tomcat to stop was interrupted.", e);
			}
		}
	}

	/**
	 * Triggers Tomcat to stop running, non-blocking (does not wait for Tomcat to have stopped running).
	 * See also {@link #stopTomcat()}.
	 */
	public void stopTomcatAsync() {
		stopWait.release();
	}
	
	public void run() {
		
		try {
			stopWait.acquire();
		} catch (Exception e) {
			log.warn("Waiting on stop-trigger for Tomcat interrupted.", e);
		}
		wasStopped = true;
		try {
			log.info("Shutting down ...");
			tomcat.stop();
			log.info("Shutdown complete.");
		} catch (Exception e) {
			log.error("Stopping embedded Tomcat instance failed.", e);
		} finally {
			stopDone.release();
		}
	}

}
