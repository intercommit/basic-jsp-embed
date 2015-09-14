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
		LaunchWebApp.stopTomcatFromBoot();
        return null;
	}
}
