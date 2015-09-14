package com.descartes.basicjsp.embed;

import java.nio.charset.Charset;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context listener for setting up and clearing variables/resources used throughout the web-application.  
 * <br>Overload this class to setup your web-application properties
 * and update the listener-class in <tt>web.xml</tt>.  
 * @author fwiers
 *
 */
public class WebSetup implements ServletContextListener {
	
	protected static WebSetup instance;
	
	public static WebSetup getInstance() {
		return instance;
	}
	
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	private ServletContext servletContext;
	private Charset encoding = Charset.defaultCharset();
	
	/**
	 * Returns the servlet context.
	 */
	public ServletContext getContext() {
		return servletContext;
	}
	
	/**
	 * Returns the default encoding for text.
	 */
	public Charset getEncoding() {
		return encoding;
	}
	
	public WebConfig getConfig() {
		return new WebConfig() {{
			setVersion(AppClassLoader.getVersion(WebSetup.class));
		}};
	}

	/**
	 * Sets the instance, servletContext and encoding.
	 * Registers <tt>appName</tt> and <tt>appVersion</tt> in the servletContext.
	 * <br>Overload to setup your web application.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
		log.debug("Initializing web application.");
		instance = this;
		servletContext = sce.getServletContext();
		WebConfig config = getConfig();
		try {
			encoding = Charset.forName(config.getDefaultEncoding());
			log.debug("Default encoding set to {}", config.getDefaultEncoding());
		} catch (Exception e) {
			log.error("Default encoding not available: " + config.getDefaultEncoding(), e);
			log.info("Using default encoding " + Charset.defaultCharset());
		}
		servletContext.setAttribute("appName", config.getAppName());
		servletContext.setAttribute("appVersion", config.getAppVersion());
	}

	/**
	 * Does nothing. Overload to cleanup/detroy resources used by your web application.
	 * <br>Officially, the logback context should be destroyed 
	 * (see comments in {@link TomcatShutdownHook#stopLoggerContext()}).
	 * But since the logback-jars are not loaded as part of the web-app
	 * (only the resource-jars are), that will close the logger
	 * before the application exits.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {

		log.debug("Web application closed.");
	}

}
