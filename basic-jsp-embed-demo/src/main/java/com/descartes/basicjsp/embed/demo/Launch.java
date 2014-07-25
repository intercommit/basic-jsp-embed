/*  Copyright 2013 Descartes Systems Group
*
*  This file is part of the "BasicJspEmbedDemo" project hosted on https://github.com/intercommit/basic-jsp-embed
*
*  BasicJspEmbed is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  any later version.
*
*  BasicJspEmbedDemo is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with BasicJspEmbedDemo.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package com.descartes.basicjsp.embed.demo;

import org.apache.catalina.webresources.StandardRoot;

import com.descartes.basicjsp.embed.LaunchWebApp;

public class Launch extends LaunchWebApp {
	
	public static void main(String[] args) {
		
		try {
			new Launch().start("", 0);
		} catch (Exception e) {
			log.error("Failed to start web application.", e);
		}
	}

	@Override
	public void configure() {
		
		super.configure();
		// only reload when testing
		setReloadable(isMavenTest());
	}
	
	@Override
	public void addResources(StandardRoot webResources) {
		
		super.addResources(webResources);
		addBasicJspResource(webResources);
	}

	protected void addBasicJspResource(StandardRoot webResources) {
		addResourceJar(webResources, LaunchWebApp.class);
	}

}
