package com.descartes.basicjsp.embed.demo.ws;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.basicjsp.embed.WebUtil;

@WebFilter("/websocket/*")
public class WsFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(WsFilter.class);
	
	public static final String ATTRIB_REMOTE_LOCATION = "remoteLocation";

	@Override
	public void destroy() {
		// NO-OP
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest hr = (HttpServletRequest) request;
		// ensure a session exists and add remote IP
		// see also http://stackoverflow.com/a/23025059
		String rl = WebUtil.getRemoteLocation(hr); 
		hr.getSession().setAttribute(ATTRIB_REMOTE_LOCATION, rl);
		log.debug("Added remote IP {}", rl);
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		// NO-OP
		log.debug("Websocket filter initialized.");
	}

}
