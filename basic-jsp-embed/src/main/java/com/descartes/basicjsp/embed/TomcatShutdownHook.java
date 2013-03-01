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

/**
 * Waits for Tomcat to stop running.
 * Activcated when JVM receives terminate signal (ctrl-C in Windows).
 * @author FWiers
 *
 */
public class TomcatShutdownHook extends Thread {
	
	private TomcatStopper tomcatStopper;

	public TomcatShutdownHook(TomcatStopper tomcatStopper) {
		super();
		this.tomcatStopper = tomcatStopper;
	}
	
	@Override
	public void run() {
		
		if (tomcatStopper != null) {
			tomcatStopper.stopTomcat();
		}
	}

}
