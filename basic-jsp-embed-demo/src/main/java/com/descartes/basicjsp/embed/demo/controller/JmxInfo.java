package com.descartes.basicjsp.embed.demo.controller;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.basicjsp.embed.Controller;

public class JmxInfo implements Controller {

	private static final Logger log = LoggerFactory.getLogger(DirFiles.class);

	@Override
	public String handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		request.setAttribute(PAGE_TITLE, "JmxInfo");
		String jmxInfo = null;
		try {
	        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
	        jmxInfo = (String) server.invoke(new ObjectName("jolokia:type=ServerHandler,qualifier=jspdemo"), "mBeanServersInfo", null, null);
			if (jmxInfo != null) {
				request.setAttribute("jmxInfoText", jmxInfo);
			}
		} catch (Exception e) {
			log.error("Could not get JmxInfo", e);
		}
		return "/WEB-INF/pages/jmxinfo.jsp";
	}

}
