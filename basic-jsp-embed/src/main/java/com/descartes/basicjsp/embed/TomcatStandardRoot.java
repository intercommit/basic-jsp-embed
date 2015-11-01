package com.descartes.basicjsp.embed;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.webresources.StandardRoot;

/**
 * Override Tomcat's {@link StandardRoot} so that resource-jars and dirs are (re)loaded
 * when the {@link StandardContext} is (re)started.
 * <br>By default the <tt>StandardContext</tt> closes the <tt>StandardRoot</tt> 
 * which in turn clears all references to resource-jars that need to be loaded.
 * As a consequence, after a reload (see {@link StandardContext#reload()},
 * all of the resources in previously registered resource-jars are no longer available.
 * <p>
 * See also {@link WebResourcePaths}.
 * @author fwiers
 *
 */
public class TomcatStandardRoot extends StandardRoot {
	
	private final WebResourcePaths appResources;
	
	public TomcatStandardRoot(WebResourcePaths appResources) {
		super();
		this.appResources = appResources;
	}
	
	/**
	 * Calls the {@link WebResourcePaths} to add registered resources to this instance.
	 */
	public void loadResources() {
		appResources.registerResources(this);
	}

	/**
	 * Overwritten to load resources (i.e. call {@link #loadResources()}) on each invocation.
	 */
	@Override 
	public void startInternal() throws LifecycleException {

		loadResources();
		super.startInternal();
	}

}
