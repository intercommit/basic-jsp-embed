package com.descartes.basicjsp.embed.demo;

import java.net.URL;

import javax.naming.directory.DirContext;

import org.apache.catalina.core.StandardContext;
import org.apache.naming.resources.FileDirContext;
import org.apache.naming.resources.VirtualDirContext;

import com.descartes.basicjsp.embed.LaunchWebApp;

public class Launch extends LaunchWebApp {
	
	public static void main(String[] args) {
		
		try {
			new Launch().start("", 0);
		} catch (Exception e) {
			log.error("Failed to start web application.", e);
		}
	}

	/**
	 * Overriden to add the resources from basic-jsp-embed to the web-context.
	 * Some of the resources in basic-jsp-embed are re-used in this demo.
	 */
	@Override
	public void addResources(StandardContext webCtx, boolean inTest, String classesDir) throws Exception {
		
        DirContext resources = null;
        if (inTest) {
        	log.debug("Restarting web application when classes change.");
        	//declare an alternate location for "WEB-INF/classes" dir    
        	VirtualDirContext vres = new VirtualDirContext();
        	resources = vres;
        	vres.setExtraResourcePaths("/WEB-INF/classes=" + classesDir);
        	URL jarFile = LaunchWebApp.class.getProtectionDomain().getCodeSource().getLocation();
        	log.debug("Loading resources from " + jarFile.toURI().toString());
        	// URL must start with "jar:" so that "openConnection" returns a java.net.JarURLConnection
        	vres.addResourcesJar(new URL("jar:" + jarFile.toURI().toString() + "!/"));
        } else {
        	FileDirContext fres = new FileDirContext();
        	resources = fres;
        	URL jarFile = Launch.class.getProtectionDomain().getCodeSource().getLocation();
        	log.debug("Loading resources from " + jarFile.toURI().toString());
        	// URL must start with "jar:" so that "openConnection" returns a java.net.JarURLConnection
        	fres.addResourcesJar(new URL("jar:" + jarFile.toURI().toString() + "!/"));
        	jarFile = LaunchWebApp.class.getProtectionDomain().getCodeSource().getLocation();
        	log.debug("Loading resources from " + jarFile.toURI().toString());
        	// URL must start with "jar:" so that "openConnection" returns a java.net.JarURLConnection
        	fres.addResourcesJar(new URL("jar:" + jarFile.toURI().toString() + "!/"));
        }
        webCtx.setResources(resources);
	}

}
