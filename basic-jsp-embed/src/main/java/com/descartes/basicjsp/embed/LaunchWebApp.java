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

import java.awt.Desktop;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import javax.naming.directory.DirContext;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.naming.resources.FileDirContext;
import org.apache.naming.resources.VirtualDirContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.appboot.AppBoot;
import com.descartes.appboot.BootKeys;

/**
 * Main class to start the web application.
 * Extend this class and set it as main class in your pom for the maven-jar-plugin.
 * Update the {@link #addResources(StandardContext, boolean, String)} method to add your resources.
 * @author fwiers
 *
 */
public class LaunchWebApp {

	public static Logger log = LoggerFactory.getLogger(LaunchWebApp.class);
	
	private static LaunchWebApp instance;
	
	/** Set per class-loader! See also {@link #stopTomcatFromBoot()} */
	public static LaunchWebApp getInstance() { return instance; }
	
	public static void main(String[] args) {
		
		try {
			new LaunchWebApp().start("", 0);
		} catch (Exception e) {
			log.error("Failed to start web application.", e);
		}
	}
	
	protected TomcatStopper stopThread;
	protected TomcatShutdownHook stopHook;
	
	/**
	 * Configures and starts Tomcat, registers a shutdown-hook for Tomcat and starts a {@link TomcatStopper} thread. 
	 * @param contextPath If none, use an empty string (NOT a <tt>/</tt>), else for example <tt>/mywebapp</tt>.
	 * @param portNumber for Tomcat. If 0, defaults to 8080.
	 * @throws Exception
	 */
	public void start(String contextPath, int portNumber) throws Exception {
		
		instance = this;
		if (portNumber == 0) {
			portNumber = 8080;
		}
    	URLClassLoader bootCL = (URLClassLoader)Thread.currentThread().getContextClassLoader();
        WebappClassLoader webCL = new WebappClassLoader(bootCL);
        webCL.setSearchExternalFirst(true);
        webCL.start(); // prevents illegal-state exceptions
        WebappLoader webLoader = new WebappLoader(webCL);
        boolean inTest = System.getProperties().containsKey(BootKeys.APP_MAVEN_TEST);
        String webAppDir = AppBoot.getHomeDir();
        String classesDir = null;
        if (inTest) {
        	classesDir = new File(AppBoot.getHomeDir()).getParent() + File.separator + "classes";
        	webAppDir = classesDir + File.separator + "META-INF" + File.separator + "resources";
        }
        log.debug("Using web application directory " + webAppDir);
        
        final Tomcat tomcat = new Tomcat();
        tomcat.setPort(portNumber);
        StandardContext webCtx = (StandardContext) tomcat.addWebapp(contextPath, webAppDir);
        webCtx.setLoader(webLoader);
        webCtx.setReloadable(inTest);
        
        addResources(webCtx, inTest, classesDir);

        tomcat.start();
        
        // To stop Tomcat via web-page action.
        stopThread = new TomcatStopper(tomcat);
        stopThread.start();
        // To stop Tomcat when JVM receives terminate signal
        Runtime.getRuntime().addShutdownHook(stopHook = new TomcatShutdownHook(stopThread));
        
        if (tomcat.getServer().getPort() > -1) {
        	log.info("Embedded Tomcat listening on port " + tomcat.getServer().getPort() + " for shutdown command " + tomcat.getServer().getShutdown());
        }
        if (Desktop.isDesktopSupported()) {
        	String indexUrl = "http://localhost:" + portNumber + contextPath;
        	try {
        		Desktop.getDesktop().browse(new URI(indexUrl));
        	} catch (Exception e) {
        		log.debug("Could not browse to " + indexUrl, e);
        	}
        }
	}
	
	/**
	 * Overload or copy and adjust this method to load resources from other places/jars.
	 * See the <tt>basic-jsp-embed-demo</tt> project for an example (in the demo-project this method is changed
	 * to also load resources from this project's jar as last). 
	 * @param webCtx the webContext to add the resources to. 
	 * @param inTest true if application is started from the target/test-classes directory.
	 * @param classesDir if inTest is true, this points to the target/classes directory
	 * (containing the META-INF/resources directory with the web-application resources).
	 * @throws Exception anything can go wrong ...
	 */
	public void addResources(StandardContext webCtx, boolean inTest, String classesDir) throws Exception {
		
        DirContext resources = null;
        if (inTest) {
        	log.debug("Restarting web application when classes change.");
        	//declare an alternate location for "WEB-INF/classes" dir    
        	VirtualDirContext vres = new VirtualDirContext();
        	resources = vres;
        	vres.setExtraResourcePaths("/WEB-INF/classes=" + classesDir);
        } else {
        	FileDirContext fres = new FileDirContext();
        	resources = fres;
        	URL jarFile = LaunchWebApp.class.getProtectionDomain().getCodeSource().getLocation();
        	log.debug("Loading resources from " + jarFile.toURI().toString());
        	// URL must start with "jar:" so that "openConnection" returns a java.net.JarURLConnection
        	fres.addResourcesJar(new URL("jar:" + jarFile.toURI().toString() + "!/"));
        }
        webCtx.setResources(resources);
	}
	
	/**
	 * Triggers the {@link TomcatStopper} to continue running (which causes Tomcat to stop).
	 */
	public void stopTomcat() {
		
		if (stopThread != null) {
			stopThread.stopTomcatAsync();
		}
	}
	
	/**
	 * Stops Tomcat via the instance of this class that started Tomcat.
	 * <br>Tomcat was started using this class loaded via the boot class loader. 
	 * The shutdown-servlet runs in the Tomcat's class loader which resets {@link #getInstance()} to null.
	 * This method uses reflection to get this class loaded via the boot class loader (which has instance set to non-null)
	 * and call the {@link #stopTomcat()} method.
	 * @return true if Tomcat shutdown was called, false on error.
	 */
	public static boolean stopTomcatFromBoot() {
		
		boolean tomcatStopped = false;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while (cl != AppBoot.bootClassLoader && cl.getParent() != null) {
        	cl = cl.getParent();
        }
        log.debug("Found boot class loader: " + cl);
        try {
        	Class<?> wac = cl.loadClass(LaunchWebApp.class.getName());
        	Method minstance = wac.getMethod("getInstance", (Class<?>[]) null);
        	Method mshutdown = wac.getMethod("stopTomcat", (Class<?>[]) null);
        	Object o = minstance.invoke((Object[]) null, (Object[]) null);
        	// Object o cannot be cast to this class, that gives a ClassCastException due to conflicting class loaders.  
        	mshutdown.invoke(o, (Object[]) null);
        	tomcatStopped = true;
        } catch (Exception e) {
        	log.error("Could not call shutdown from " + LaunchWebApp.class.getSimpleName() + " from boot class loader.", e);
        }
        return tomcatStopped;
	}
	
}
