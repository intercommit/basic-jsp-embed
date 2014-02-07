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

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.descartes.basicjsp.embed.controller.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main servlet that receives requests for <tt>/pages/*</tt>.
 * The servet calls controllers to handle the requests.
 * <br>Overload this class to handle additional controllers
 * and update the servlet-class in <tt>web.xml</tt> under the <tt>MainServlet</tt> servlet-name.  
 * @author fwiers
 *
 */
@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {
	
	protected Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Overload this method to get the controller that should handle the request for the given path.
	 * <br>By default:
	 * <br>  {@link #getHomeController()} is called for paths <tt>/, /home, /index</tt>
	 * <br>  {@link Log#getInstance()} is called for <tt>/log, /logerror</tt>
	 * <br>  {@link SysEnv#getInstance()} is called for <tt>/sysenv</tt>
	 * <br>  and {@link #getShutdownController()} is called for <tt>/shutdown</tt>
	 * @param path page-path (e.g. <tt>/index</tt>), see alse {@link WebUtil#getPagePath(HttpServletRequest)}. 
	 * @return the controller handling the request, or null if no controller was found.
	 */
	protected Controller getController(String path) {
		
		if (path.equals("/") || path.equals("/home") || path.equals("/index")) {
			return getHomeController();
		}
		if (path.equals("/log") || path.equals("/logerror")) {
			return Log.getInstance();
		}
		if (path.equals("/sysenv")) {
			return SysEnv.getInstance();
		}
		if (path.equals("/shutdown")) {
			return getShutdownController();
		}
		return null;
	}
	
	/**
	 * Overload to set web-app home. 
	 * @return {@link Home#getInstance()}.
	 */
	protected Controller getHomeController() {
		return Home.getInstance();
	}

	/**
	 * Overload to set a secure shutdown page. 
	 * @return {@link Shutdown#getInstance()}.
	 */
	protected Controller getShutdownController() {
		return Shutdown.getInstance();
	}

	/** 
	 * Calls {@link #doPost(HttpServletRequest, HttpServletResponse)}
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * Finds a controller by calling {@link #getController(String)} and use the controller to handle the request.
	 * If the controller throws an error, an internal error is shown ({@link WebUtil#respondInternalError(HttpServletResponse, String, String)}).
	 * If the controller returns a string (usually a jsp-page like <tt>/WEB-INF/pages/home.jsp</tt>)
	 * the request-dispatcher is used to further handle the request.
	 * If there is no request-dispatcher for the string returned by the controller, an internal error is shown.
	 * 
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		String pagePath = WebUtil.getPagePath(request);
		Controller controller = getController(pagePath);
		if (controller == null) {
			log.info("Request for unavailable page: " + request.getRequestURI());
			WebUtil.respondNotFound(response, "No controller found for " + pagePath, null);
			return;
		}
		String viewName = null;
		try {
			viewName = controller.handleRequest(request, response);
		} catch (Exception e) {
			log.error("Controller " + controller.getClass().getName() + " failed to handle request.", e);
			WebUtil.respondInternalError(response, "Controller " + controller.getClass().getSimpleName() + " failed to handle request: " + e, null);
			return;
		}
		if (viewName != null) {
			RequestDispatcher view = request.getRequestDispatcher(viewName);
			if (view == null) {
				log.error("Controller " + controller.getClass().getName() + " returned unavailable view " + viewName);
				WebUtil.respondInternalError(response, "Controller " + controller.getClass().getSimpleName() + " returned unavailable view " + viewName, null);
			}
			view.forward(request, response);
		}
	}

}
