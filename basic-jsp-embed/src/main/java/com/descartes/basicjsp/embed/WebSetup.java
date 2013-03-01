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
		return new WebConfig();
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
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {

		log.debug("Web application closed.");
	}

}
