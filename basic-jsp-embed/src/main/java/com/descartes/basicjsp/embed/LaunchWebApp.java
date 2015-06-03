package com.descartes.basicjsp.embed;

import java.awt.Desktop;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.appboot.AppBoot;
import com.descartes.appboot.BootKeys;

/**
 * Main class to start the web application.
 * Extend this class and set it as main class in your pom for the maven-jar-plugin.
 * Override the {@link #configure()}, {@link #addResources(StandardRoot)} and {@link #beforeStart()}
 * functions to customize your web-app (see also basic-jsp-embed-demo project). 
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
	
	private boolean mavenTest;
	private boolean reloadable;
	private boolean openBrowser;
	private String webAppDir;
	private String contextPath;
	private int portNumber;
	
	protected Tomcat tomcat;
	protected StandardContext webCtx;
	protected WebappLoader webLoader;
	
	/**
	 * Configures and starts Tomcat, registers a shutdown-hook for Tomcat and starts a {@link TomcatStopper} thread. 
	 * @param contextPath If none, use an empty string (NOT a <tt>/</tt>), else for example <tt>/mywebapp</tt>.
	 * @param portNumber for Tomcat. If 0, defaults to 8080.
	 * @throws Exception
	 */
	public void start(String contextPath, int portNumber) throws Exception {
		
		instance = this;
		setContextPath(contextPath);
		setPortNumber(portNumber == 0 ? 8080 : portNumber);
		configure();
        log.debug("Using web application directory " + getWebAppDir());
        
        tomcat = new Tomcat();
        tomcat.setPort(getPortNumber());
        
        webCtx = (StandardContext) tomcat.addWebapp(getContextPath(), getWebAppDir());
        webCtx.setReloadable(isReloadable());

        webLoader = new WebappLoader(AppBoot.bootClassLoader);
        // reloadable is copied from context setting.
        webCtx.setLoader(webLoader);
        
        log.info("Reloading loader: " + webLoader.getReloadable());
        
        StandardRoot webResources = new StandardRoot();
        webCtx.setResources(webResources);
        addResources(webResources);
        
        beforeStart();
        
        tomcat.start();
        
        // To stop Tomcat via web-page action.
        stopThread = new TomcatStopper(tomcat);
        stopThread.setName("TomcatShutdown");
        stopThread.start();
        // To stop Tomcat when JVM receives terminate signal
        Runtime.getRuntime().addShutdownHook(stopHook = new TomcatShutdownHook(stopThread));
        
        if (tomcat.getServer().getPort() > -1) {
        	log.info("Embedded Tomcat listening on port " + tomcat.getServer().getPort() + " for shutdown command " + tomcat.getServer().getShutdown());
        }
        if (isOpenBrowser() && Desktop.isDesktopSupported()) {
        	String indexUrl = tomcat.getConnector().getScheme() + "://localhost:" + getPortNumber() + getContextPath();
        	try {
        		Desktop.getDesktop().browse(new URI(indexUrl));
        	} catch (Exception e) {
        		log.debug("Could not browse to " + indexUrl, e);
        	}
        }
	}
	
	/**
	 * Called by start to set all get/set variables.
	 * Overload to set the variables appropriate for your web-app.  
	 */
	public void configure() {
		
		setMavenTest(System.getProperties().containsKey(BootKeys.APP_MAVEN_TEST));
		setReloadable(true);
		if (isMavenTest()) {
			setOpenBrowser(true);
			setWebAppDir(getMavenClassesDir() + File.separator + "META-INF" + File.separator + "resources");
		} else {
			setWebAppDir(AppBoot.getHomeDir());
		}
		setTldJars("*taglibs-standard-impl*", "*");
	}
	
	/**
	 * Sets the jar-file-names (glob-pattern, comma-separated) to scan for {@code *.tld} files,
	 * see also https://tomcat.apache.org/tomcat-8.0-doc/config/jar-scan-filter.html
	 * <br>By default all jar-files are scanned which has a significant impact on boot-time.
	 * @param include set to {@code *taglibs-standard-impl*} by {@link #configure()}
	 * @param exclude set to {@code *} by {@link #configure()}
	 */
	public void setTldJars(String include, String exclude) {
		
		System.setProperty(org.apache.tomcat.util.scan.Constants.SCAN_JARS_PROPERTY, include);
		System.setProperty(org.apache.tomcat.util.scan.Constants.SKIP_JARS_PROPERTY, exclude);
	}
	
	public void addResources(StandardRoot webResources) {
		
		if (isMavenTest()) {
	    	String classesDir = getMavenClassesDir();
	    	log.debug("Restarting web application when classes change in " + classesDir);
	    	// Note: only changes for classes loaded by Tomcat are seen by Tomcat.
	    	DirResourceSet dr = new DirResourceSet(webResources, "/WEB-INF/classes", classesDir, "/");
	    	webResources.addPostResources(dr);
		} else {
			addResourceJar(webResources, getInstance().getClass());
		}
	}
	
	/**
	 * Adds the jar containing the given class as a {@link JarResourceSet} (internal path is set to "/META-INF/resources").
	 */
	public void addResourceJar(StandardRoot webResources, Class<?> classInJar) {
		
		File jarFile = new File(classInJar.getProtectionDomain().getCodeSource().getLocation().getFile());
		log.debug("Adding resource jar file " + jarFile);
		JarResourceSet jr = new JarResourceSet(webResources, "/", jarFile.getAbsolutePath(), "/META-INF/resources");
		webResources.addJarResources(jr);
	}
	
	/**
	 * Adds the libDir as a {@link DirResourceSet} (internal path is set to "/WEB-INF/lib").
	 * This is only needed when web-app should reload when a jar changes
	 * ({@link AppBoot} will already serve classes within the jars to Tomcat). 
	 * <br>WARNING: can have strange side-effects and reloading appears to be broken (webapp is stopped but not started).
	 */
	public void addResourceLibDir(StandardRoot webResources, String libDir) {
		
		log.debug("Adding resource lib directory " + libDir);
		DirResourceSet dr = new DirResourceSet(webResources, "/WEB-INF/lib", libDir, "/");
		webResources.addPostResources(dr);
	}
	
	/**
	 * Called before Tomcat is started, use to setup security etc.
	 * Does nothing by default.
	 */
	public void beforeStart() {
		
	}
	
	/**
	 * Only use when {@link #isMavenTest()} returns true.
	 * @return the Maven target/classes directory.
	 */
	public String getMavenClassesDir() {
		return new File(AppBoot.getHomeDir()).getParent() + File.separator + "classes";
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
		
		if (getInstance() != null) {
			log.debug("LaunchWebApp instance found.");
			getInstance().stopTomcat();
			return true;
		}
		boolean tomcatStopped = false;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl.getParent() != null) {
			log.debug("Using webapp parent classloader.");
			cl = cl.getParent();
		} else {
			log.debug("Using system classloader.");
			cl = ClassLoader.getSystemClassLoader();
		}
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

	/* *** bean methods *** */
	
	public boolean isMavenTest() {
		return mavenTest;
	}

	public void setMavenTest(boolean mavenTest) {
		this.mavenTest = mavenTest;
	}

	public boolean isReloadable() {
		return reloadable;
	}

	public void setReloadable(boolean reloadable) {
		this.reloadable = reloadable;
	}

	public String getWebAppDir() {
		return webAppDir;
	}

	public void setWebAppDir(String webAppDir) {
		this.webAppDir = webAppDir;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public boolean isOpenBrowser() {
		return openBrowser;
	}

	public void setOpenBrowser(boolean openBrowser) {
		this.openBrowser = openBrowser;
	}
	
	/** 
	 * Uses the thread's class-loader to get the path to a file on the class-path.
	 * @return null if resourceName was not found or a file (which may or may not exist).
	 */
	public static File getFile(final String resourceName) {
		return getFile(Thread.currentThread().getContextClassLoader().getResource(resourceName));
	}

	/** 
	 * Converts a URL to a File (e.g. %20 int the path is converted to a space).
	 * @return A file (which may or may not exist) or null if url was null.
	 */
	public static File getFile(final URL url) {
	
		if (url == null) return null;
		File f = null;
		try {
			f = new File(url.toURI());
		} catch(Exception e) {
			f = new File(url.getPath());
		}
		return f;
	}

}
