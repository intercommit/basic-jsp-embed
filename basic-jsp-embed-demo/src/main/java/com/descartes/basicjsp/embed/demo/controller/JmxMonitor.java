package com.descartes.basicjsp.embed.demo.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.descartes.basicjsp.embed.Controller;

public class JmxMonitor implements Controller {

	@Override
	public String handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

		request.setAttribute(PAGE_TITLE, "Monitor");
		return "/WEB-INF/pages/monitor.jsp";
	}

}
