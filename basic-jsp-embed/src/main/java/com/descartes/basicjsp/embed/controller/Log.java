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

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.CyclicBufferAppender;

import com.descartes.basicjsp.embed.Controller;
import com.descartes.basicjsp.embed.WebUtil;

/**
 * Uses the log-buffer <tt>CYCLIC(ERROR)</tt> (must be specified in <tt>logback.xml</tt> to show the last (error) log-messages.
 * @author fwiers
 *
 */
public class Log implements Controller {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(Log.class);

    private Log() { }
    private static class SingletonHolder { 
    	public static final Log INSTANCE = new Log();
    }
    public static Log getInstance() {
    	return SingletonHolder.INSTANCE;
    }

	PatternLayout logLayout;
	CyclicBufferAppender<ILoggingEvent> logBuffer;

	@Override
	public String handleRequest(HttpServletRequest request,	HttpServletResponse response) {
		
		boolean logError = WebUtil.getPagePath(request).endsWith("error");
		request.setAttribute(PAGE_TITLE, (logError ? "Error Log" : "Log"));
		
		logBuffer = getLogBuffer("CYCLIC" + (logError ? "ERROR" : ""));
		if (logBuffer == null) {
			request.setAttribute("logTextInfo", "Log buffer is not available (please check log configuration for CYCLIC appender).");
		} else if (logBuffer.getLength() == 0) {
			request.setAttribute("logTextInfo", "No log events available, log buffer is empty.");
		} else {
			// logLayout cannot be stored: it no longer works when the log configuration file is updated. 
			logLayout = new PatternLayout();
			logLayout.setContext(getLoggerContext());
			logLayout.setPattern("%d{dd/MM HH:mm:ss:SSS} %-5level %logger{35} - %msg%n%rEx");
			logLayout.start();

			int maxEvents = logBuffer.getLength(); 
			request.setAttribute("logTextInfo", logLayout.doLayout(
					createLoggingEvent("Showing " + maxEvents + " log events, last event first.")));
			StringBuilder sb = new StringBuilder();
			LoggingEvent le;
			for (int i = maxEvents-1; i >= 0; i--) {
				le = (LoggingEvent) logBuffer.get(i);
				String line = logLayout.doLayout(le); 
				sb.append(line);
			}
			request.setAttribute("logText", sb.toString());
			log.debug("Returning {} log events as text", maxEvents);
			logLayout.stop();
		}

		return "/WEB-INF/pages/log.jsp";
	}
	
	public static LoggingEvent createLoggingEvent(String msg) {
		
		LoggingEvent le = new LoggingEvent();
		le.setTimeStamp(System.currentTimeMillis());
		le.setLevel(Level.INFO);
		le.setThreadName(Thread.currentThread().getName());
		le.setLoggerName(Log.class.getName());
		le.setMessage(msg);
		return le;
	}

	public static LoggerContext getLoggerContext() {
		//Logger rootLogger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//return rootLogger.getLoggerContext();

		// assume SLF4J is bound to logback in the current environment
		return (LoggerContext) LoggerFactory.getILoggerFactory();
	}

	public static Logger getRootLogger() {
		return getLoggerContext().getLogger(Logger.ROOT_LOGGER_NAME);
	}

	public static CyclicBufferAppender<ILoggingEvent> getLogBuffer(String name) {
		return (CyclicBufferAppender<ILoggingEvent>) getRootLogger().getAppender(name);
	}

}
