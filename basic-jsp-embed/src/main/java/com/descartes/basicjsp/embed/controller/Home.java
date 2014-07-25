package com.descartes.basicjsp.embed.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.descartes.basicjsp.embed.Controller;

/**
 * Returns the jsp home-page.
 * @author fwiers
 *
 */
public class Home implements Controller {

	// Private constructor prevents instantiation from other classes
    private Home() { }

    /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
    private static class SingletonHolder { 
    	public static final Home INSTANCE = new Home();
    }

    public static Home getInstance() {
    	return SingletonHolder.INSTANCE;
    }

	@Override
	public String handleRequest(HttpServletRequest request,	HttpServletResponse response) {
		
		request.setAttribute(PAGE_TITLE, "Home");
		return "/WEB-INF/pages/home.jsp";
	}
}
