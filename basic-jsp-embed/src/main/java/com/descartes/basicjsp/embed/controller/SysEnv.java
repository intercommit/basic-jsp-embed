package com.descartes.basicjsp.embed.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.descartes.basicjsp.embed.Controller;
import com.descartes.basicjsp.embed.SysPropsUtil;


/**
 * Show the system environment using methods from {@link SysPropsUtil}.
 * @author frederikw
 *
 */
public class SysEnv implements Controller {

    private SysEnv() { }
    private static class SingletonHolder { 
    	public static final SysEnv INSTANCE = new SysEnv();
    }
    public static SysEnv getInstance() {
    	return SingletonHolder.INSTANCE;
    }

	@Override
	public String handleRequest(final HttpServletRequest request, final HttpServletResponse response) {
		
		request.setAttribute(PAGE_TITLE, "System");
		request.setAttribute("memoryUsage", SysPropsUtil.getMemoryUsage());
		request.setAttribute("systemEnv", SysPropsUtil.getSystemEnv());
		request.setAttribute("systemProps", SysPropsUtil.getSystemProps());
		return "/WEB-INF/pages/sysenv.jsp";
	}
}
