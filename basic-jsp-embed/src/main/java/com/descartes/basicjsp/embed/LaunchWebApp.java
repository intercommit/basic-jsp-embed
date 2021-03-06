package com.descartes.basicjsp.embed;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.catalina.Container;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.appboot.AppBoot;
import com.descartes.appboot.BootKeys;

/**
 * Main class to start the web application.
 * Extend this class and set it as main class in your pom for the maven-jar-plugin.
 * Override the {@link #configure()}, {@link #createAppResources()} and {@link #beforeStart()}
 * functions to customize your web-app (see also basic-jsp-embed-demo project). 
 * @author fwiers
 *
 */
public class LaunchWebApp {

	private static final Logger log = LoggerFactory.getLogger(LaunchWebApp.class);
	
	private static volatile LaunchWebApp instance;
	
	/** Set per class-loader! See also {@link #stopTomcatFromBoot()} */
	public static LaunchWebApp getInstance() { return instance; }
	
	public static void main(String[] args) {
		
		try {
			new LaunchWebApp().start("", 0);
		} catch (Exception e) {
			log.error("Failed to start web application.", e);
			throw new RuntimeException(e);
		}
	}
	
	private boolean mavenTest;
	private boolean reloadable;
	private boolean openBrowser;
	private String webAppDir;
	private String contextPath;
	private int portNumber;
	private long maxWaitStartMs = 1000 * 60 * 5; 
	
	protected Tomcat tomcat;
	protected StandardContext webCtx;
	protected WebappLoader webLoader;
	protected TomcatStateListener stateListener;
	protected TomcatShutdownHook stopHook;
	
	/**
	 * Configures and starts Tomcat, registers a shutdown-hook for Tomcat.
	 * The thread calling this method must be a non-daemon thread to keep the JVM from exiting.
	 * When this method exits, Tomcat has failed to start or has been shutdown and destroyed. 
	 * @param contextPath If there is none, use an empty string (NOT a <tt>/</tt>), else for example <tt>/mywebapp</tt>.
	 * @param portNumber The port-number for Tomcat connector to listen on. A value smaller as 1 defaults to 8080.
	 * @throws Exception When setup or startup fails in an unexpected manner, or when Tomcat server failed to start.
	 */
	public void start(String contextPath, int portNumber) throws Exception {
		
		instance = this;
		setContextPath(contextPath);
		setPortNumber(portNumber < 1 ? 8080 : portNumber);
		configure();
        log.debug("Using web application directory " + getWebAppDir());
        
        tomcat = new Tomcat();
        tomcat.setPort(getPortNumber());
        
        webCtx = (StandardContext) tomcat.addWebapp(getContextPath(), getWebAppDir());
        webCtx.setReloadable(isReloadable());
        webCtx.setFailCtxIfServletStartFails(true);

        webLoader = new WebappLoader(AppBoot.bootClassLoader);
        
        // Delegating to parent class-loader would make a method like stopTomcatFromBoot() unnecessary.
        // But a side effect is that Tomcat will not reload when classes change since the loader does not manage these classes anymore.
        // webLoader.setDelegate(true);
        
        // reloadable is copied from context setting.
        webCtx.setLoader(webLoader);
        log.info("Reloading loader: " + webLoader.getReloadable());
        
        WebResourceRoot webResources = createResourceRoot();
        webCtx.setResources(webResources);
        
        beforeStart();
        tomcat.start();
        
        boolean awaitStart = false;
        try {
        	awaitStart = stateListener.tomcatServerStarted.await(maxWaitStartMs, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) { 
        	log.debug("Waiting for Tomcat server start interrupted.");
        }
        if (!awaitStart || stateListener.isFailedStart()) {
        	try {
        		afterStart(false);
        	} finally {
        		stopHook.run();
        	}
        	throw new RuntimeException("Tomcat server failed to start properly, please examine log-files.");
        }
        // To stop Tomcat when JVM receives terminate signal
        Runtime.getRuntime().addShutdownHook(stopHook);
    	afterStart(true);

        if (tomcat.getServer().getPort() > -1) {
        	log.info("Tomcat listening for shutdown command [" + tomcat.getServer().getShutdown() 
        			+ "] on port " + tomcat.getServer().getPort());
        }
        if (isOpenBrowser() && Desktop.isDesktopSupported()) {
        	String indexUrl = tomcat.getConnector().getScheme() + "://localhost:" + getPortNumber() + getContextPath();
        	try {
        		Desktop.getDesktop().browse(new URI(indexUrl));
        	} catch (Exception e) {
        		log.debug("Could not browse to " + indexUrl, e);
        	}
        }
        // Keep this non-daemon thread alive, else the JVM will exit.
        try {
        	stateListener.tomcatServerDestroyed.await();
            log.debug("Tomcat server stopped and destroyed.");
        } catch (InterruptedException ie) {
        	log.error("Interrupted while waiting for Tomcat server to stop.");
        }
        // JVM will exit, triggering the stopHook if "stopTomcat()" was not called.
	}
	
	/**
	 * Called by start to set all get/set variables.
	 * Overload to set the variables appropriate for your web-app.  
	 */
	public void configure() {
		
		setMavenTest(System.getProperties().containsKey(BootKeys.APP_MAVEN_TEST));
		if (isMavenTest()) {
			setReloadable(true);
			setOpenBrowser(true);
			setWebAppDir(getMavenClassesDir());
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
	
	/**
	 * Called by the {@link #start(String, int)} method after {@link #configure()} and before the {@link #beforeStart()} method.
	 * <br>This method calls {@link #createAppResources()} for configuration.
	 * @return By default, returns an instance of {@link TomcatStandardRoot}.
	 */
	public WebResourceRoot createResourceRoot() {
		return new TomcatStandardRoot(createAppResources());
	}
	
	/**
	 * Called by {@link #createResourceRoot()}. By default this method creates a {@link WebResourcePaths}
	 * instance with a Maven classes directory if {@link #isMavenTest()} is true, 
	 * else it sets the main-jar using the class from {@link #getInstance()}. 
	 */
	public WebResourcePaths createAppResources() {
		
		WebResourcePaths wrj = (isMavenTest() ? new WebResourcePaths(getMavenClassesDir()) : new WebResourcePaths(getInstance().getClass()));
		return wrj;
	}
	
	/**
	 * Called before Tomcat is started, creates and initializes the Tomcat state listener and Tomcat shutdown hook.
	 * Also disables session persistence between restarts.
	 * Can be overloaded to setup security etc. (see also {@link com.descartes.basicjsp.embed.ssl}),
	 * in which case it is a good idea to call <tt>super.beforeStart()</tt>.
	 * <br>Example adding an AJP-connector (configured via Apache2 mod-jk):
	 * <pre><code>Connector c = new Connector("AJP/1.3"); 
c.setPort(8009);
tomcat.getService().addConnector(c);
super.beforeStart();</code></pre>
	 * 
	 */
	public void beforeStart() {
		
        stateListener = new TomcatStateListener(tomcat);
        // stateListener.logVerbose = true;
        stateListener.init();
        stopHook = new TomcatShutdownHook(tomcat, stateListener.tomcatServerDestroyed);
	}
	
	/**
	 * Tomcat will save/restore sessions between restarts. 
	 * This can cause errors like:
	 * <br><code>Caused by: java.io.NotSerializableException: java.lang.Object</code>.
	 * <br>Call this method from {@link #beforeStart()} to disable session persistence.
	 */
	public void disableSessionPersistence() {
		
		Container[] hostChilds = tomcat.getHost().findChildren();
		int disabled = 0;
		for (Container c : hostChilds) {
			if (c instanceof StandardContext) {
				StandardContext sc = (StandardContext) c;
				if (sc.getManager() == null) {
					log.debug("No context manager available, adding standard manager with session persistence disabled.");
					StandardManager sm = new StandardManager();
					sm.setPathname(null);
					sc.setManager(sm);
					disabled++;
				} else if (sc.getManager() instanceof StandardManager) {
					StandardManager sm = (StandardManager) sc.getManager(); 
					sm.setPathname(null);
					log.debug("Disabled session persistence for context manager.");
					disabled++;
				} else {
					log.debug("Unable to disable session persistence for context manager of unknown class.");
				}
			}
		}
		if (disabled == 0) {
			log.info("Unable to disable session persistence in context manager.");
		}
	}
	
	/**
	 * Called after Tomcat has started. Does nothing by default.
	 * @param startOk true if Tomcat server started OK. If false, the start-method will throw a RuntimeException after calling this method.
	 */
	public void afterStart(boolean startOk) {}
	
	/**
	 * Only use when {@link #isMavenTest()} returns true.
	 * @return the Maven target/classes directory.
	 */
	public String getMavenClassesDir() {
		return new File(AppBoot.getHomeDir()).getParent() + File.separator + "classes";
	}
	
	/**
	 * Starts the {@link TomcatShutdownHook} thread which stops Tomcat.
	 * This is an async-method (will return before Tomcat has actually stopped).
	 * If no {@link TomcatShutdownHook} is available, the {@link #stopTomcatFromBoot()} method
	 * is used to find the instance of this class that does have the shutdown-hook.
	 * <br>See also {@link #stopTomcatFromBoot()}.
	 */
	public void stopTomcat() {
		
		if (stopHook == null) {
			if (getInstance() == null) {
				stopTomcatFromBoot();
			} else {
				log.info("Cannot stop Tomcat: there is no Tomcat shutdown hook set.");
			}
		} else {
			Runtime.getRuntime().removeShutdownHook(stopHook);
			stopHook.start();
		}
	}

	/**
	 * Uses {@link AppClassLoader} to call {@link #stopTomcat()} on the boot-loaded instance of this class.
	 * @return true if Tomcat shutdown was called, false on error.
	 */
	public static boolean stopTomcatFromBoot() {
		
		boolean tomcatStopped = false;
        try {
        	AppClassLoader.invokeOnInstance(AppClassLoader.getInstance(LaunchWebApp.class.getName()), "stopTomcat");
    		tomcatStopped = true;
        } catch (Exception e) {
        	log.error("Could not call shutdown from boot class loader.", e);
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

	/** See {@link LaunchWebApp#setMaxWaitStartMs(long)} */
	public long getMaxWaitStartMs() {
		return maxWaitStartMs;
	}

	/** Maximum time in miliseconds to wait for the Tomcat server to start. Default 5 minutes. */
	public void setMaxWaitStartMs(long maxWaitStartMs) {
		this.maxWaitStartMs = maxWaitStartMs;
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
