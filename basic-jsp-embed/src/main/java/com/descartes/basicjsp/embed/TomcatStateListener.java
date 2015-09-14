package com.descartes.basicjsp.embed;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If components used by the Tomcat server fail to start,
 * the Tomcat server itself will still run and the application using embedded Tomcat will not exit. 
 * This state-listener verifies no components have failed to start 
 * and also listens for the "tomcat server started" event (fired after all other components have started).
 * <br>Before starting Tomcat, the method {@link #init()} must be called.
 * @author FWiers
 */
public class TomcatStateListener implements LifecycleListener {

	private static final Logger log = LoggerFactory.getLogger(TomcatStateListener.class);

	public boolean logVerbose;
	
	/** 
	 * Waits for the "Tomat server has started" event.
	 * This event is fired after all other components (should have) already started. 
	 * <br>Use a time-out when awaiting the Tomcat server started event:
	 * if Tomcat server initialization fails, the started phase is never reached.
	 */
	public final CountDownLatch tomcatServerStarted = new CountDownLatch(1);

	/**
	 * Waits for the "Tomat server was destroyed" event.
	 * This event is triggered by the {@link TomcatShutdownHook}.
	 */
	public final CountDownLatch tomcatServerDestroyed = new CountDownLatch(1);

	protected final Tomcat tomcat;
	protected final Set<Lifecycle> registeredLifecycles;
	protected volatile Lifecycle failedLc;
	
	/**
	 * @param tomcat the Tomcat server to listen for state events.
	 */
	public TomcatStateListener(Tomcat tomcat) {
		super();
		this.tomcat = tomcat;
		this.registeredLifecycles = new HashSet<Lifecycle>();
	}
	
	/**
	 * Register this listener to receive events from Tomcat server life-cycle components.
	 * See also {@link #unregister()}. 
	 */
	public void init() {
		
		registerLifecycle(tomcat.getServer());
		for (Container container : tomcat.getHost().findChildren()) {
			registerLifecycle(container);
			// containers have children, ignore them for now.
		}
		for (Service service : tomcat.getServer().findServices()) {
			registerLifecycle(service);
			// no connectors are available when Tomcat has not been started yet,
			// so this for-loop is kind of useless at the moment.
			for (Connector connector : service.findConnectors()) {
				registerLifecycle(connector);
			}
		}
		if (logVerbose) {
			log.debug("Listener registered for {} components: {}", registeredLifecycles.size(), registeredLifecycles);
		} else {
			log.debug("Listener registered for {} components", registeredLifecycles.size());
		}
	}
	
	/**
	 * Add the life-cycle component to the set of components for which {@link #lifecycleEvent(LifecycleEvent)}
	 * expects events. Also ensure there is no "double listener registering" for the same life-cycle component. 
	 */
	protected boolean registerLifecycle(Lifecycle lc) {
		
		boolean added = false;
		if (registeredLifecycles.contains(lc)) {
			log.debug("Listener already registered for {}", lc);
		} else {
			lc.addLifecycleListener(this);
			registeredLifecycles.add(lc);
			added = true;
			if (logVerbose) {
				log.debug("Listener registered for {} ({})", lc, lc.getClass());
			}
		}
		return added;
	}
	
	/**
	 * Remove this listener from all Tomcat server components.
	 * Note that this means that {@link #tomcatServerDestroyed} and {@link #tomcatServerStarted}
	 * will no longer "count down". 
	 */
	public void unregister() {
		
		for (Lifecycle lc : registeredLifecycles) {
			lc.removeLifecycleListener(this);
		}
		registeredLifecycles.clear();
	}

	/**
	 * Listens for component life-cycle events and checks for a failed status.
	 * Triggers the countdown-latches on relevant Tomcat server life-cycle events.
	 */
	@Override
	public void lifecycleEvent(LifecycleEvent event) {

		if (!registeredLifecycles.contains(event.getSource())) {
			if (event.getSource() instanceof Lifecycle) {
				if (logVerbose) {
					log.debug("Received event from foreign source {}", event.getSource());
				}
			} else {
				// event source is not a life-cycle component
				if (event.getSource() != null) {
					log.warn("Received event from foreign non-lifecycle source {} ({})", event.getSource(), event.getSource().getClass());
				}
				return;
			}
		}
		Lifecycle eventSource = (Lifecycle) event.getSource();
		LifecycleState state = eventSource.getState();
		if (logVerbose) {
			log.debug("Lifecycle event from {} of type {} in state {}", eventSource, event.getType(), state);
		}
		if (LifecycleState.FAILED.equals(state)) {
			failedLc = eventSource;
			log.debug("Lifecycle component {} failed to start.", eventSource);
		}
		if (eventSource == tomcat.getServer()) {
			if (LifecycleState.FAILED.equals(state)) {
				failedLc = eventSource;
				log.info("Tomcat server failed at event type {}", event.getType());
				tomcatServerStarted.countDown();
			} else if (Server.AFTER_START_EVENT.equals(event.getType())) {
				checkForFailedLc();
				if (isFailedStart()) {
					log.info("A Tomcat server component failed to start: {}", getFailedLc());
				} else {
					log.info("Tomcat server ready.");
				}
				tomcatServerStarted.countDown();
			} else if (Server.AFTER_DESTROY_EVENT.equals(event.getType())) {
				tomcatServerDestroyed.countDown();
			}
		}
	}
	
	/**
	 * True if the server has started and one or more server components failed.
	 */
	public boolean isFailedStart() { return (failedLc != null);	}
	
	/**
	 * The first found component that failed to start.  
	 */
	public Lifecycle getFailedLc() { return failedLc; }
		
	/**
	 * Components can fail without without firing an event (e.g. connector trying to listen to a port that is already in use).
	 * Sets {@link #failedLc} if a failed component is found (does not overwrite a non-null value for {@link #failedLc}).
	 */
	protected void checkForFailedLc() {
		
		if (failedLc == null) {
			for (Lifecycle lc : registeredLifecycles) {
				if (LifecycleState.FAILED.equals(lc.getState())) {
					failedLc = lc;
					break;
				}
			}
		}
		if (failedLc == null) {
			// connectors are added during startup
			for (Service service : tomcat.getServer().findServices()) {
				for (Connector connector : service.findConnectors()) {
					if (LifecycleState.FAILED.equals(connector.getState())) {
						failedLc = connector;
						break;
					}
				}
			}
		}
	}

}
