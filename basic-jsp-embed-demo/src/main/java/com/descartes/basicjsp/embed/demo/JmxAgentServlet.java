package com.descartes.basicjsp.embed.demo;

import javax.servlet.ServletConfig;

import org.jolokia.http.AgentServlet;
import org.jolokia.restrictor.Restrictor;
import org.jolokia.util.LogHandler;

@SuppressWarnings("serial")
public class JmxAgentServlet extends AgentServlet {
	
	public JmxAgentServlet() {
		this(null);
	}
	
    public JmxAgentServlet(Restrictor pRestrictor) {
    	super(pRestrictor);
    }
	
    protected LogHandler createLogHandler(ServletConfig pServletConfig) {
    	return new JmxAgentLogger();
    }

}
