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

import com.descartes.appboot.BootUtil;

/**
 * Configuration bean for web application.
 * @author fwiers
 *
 */
public class WebConfig {

	private String defaultEncoding = "UTF-8";
	private String appName = "Name";
	private String appVersion = BootUtil.getPomVersion(WebConfig.class);

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
