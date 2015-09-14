package com.descartes.basicjsp.embed;

/**
 * Configuration bean for web application.
 * @author fwiers
 *
 */
public class WebConfig {

	private String defaultEncoding = "UTF-8";
	private String appName = "Name";
	private String appVersion = "unknown";

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	/**
	 * Encoding used to send text messages to client. 
	 * By default set to <tt>UTF-8</tt> 
	 */
	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	public String getAppName() {
		return appName;
	}

	/**
	 * The name of the web application shown in the page title, e.g. <tt>Log [appName]</tt>
	 * By default set to <tt>Name</tt>
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

	/**
	 * The version of the web application (usually taken from pom-version).
	 */
	public void setVersion(String appVersion) {
		this.appVersion = appVersion;
	}
}
