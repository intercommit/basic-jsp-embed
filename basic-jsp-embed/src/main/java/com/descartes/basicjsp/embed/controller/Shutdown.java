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
package com.descartes.basicjsp.embed.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.descartes.basicjsp.embed.Controller;
import com.descartes.basicjsp.embed.LaunchWebApp;
import com.descartes.basicjsp.embed.WebUtil;

/**
 * Stops Tomcat, or if that fails, shuts down the JVM.
 * @author fwiers
 *
 */
public class Shutdown implements Controller {

    private Shutdown() { }
    private static class SingletonHolder { 
    	public static final Shutdown INSTANCE = new Shutdown();
    }
    public static Shutdown getInstance() {
    	return SingletonHolder.INSTANCE;
    }

	@Override
	public String handleRequest(HttpServletRequest request,	HttpServletResponse response) {
		
		WebUtil.respondMsg(response, HttpServletResponse.SC_OK, "Shutting down ...", null);
        if (!LaunchWebApp.stopTomcatFromBoot()) {
        	// fallback: call system-exit.
        	new Thread() {
        		{ setDaemon(true); }
        		public void run() { System.exit(0); }
        	}.start();
        }
        return null;
	}
}
