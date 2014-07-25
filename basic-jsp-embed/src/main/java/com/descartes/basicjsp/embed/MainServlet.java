package com.descartes.basicjsp.embed;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.descartes.basicjsp.embed.controller.Home;
import com.descartes.basicjsp.embed.controller.Log;
import com.descartes.basicjsp.embed.controller.Shutdown;
import com.descartes.basicjsp.embed.controller.SysEnv;

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
	
	public static final String PATH_HOME_ROOT = "/";
	public static final String PATH_HOME_HOME = "/home";
	public static final String PATH_HOME_INDEX = "/index";
	public static final String PATH_LOG = "/log";
	public static final String PATH_LOG_ERROR = "/logerror";
	public static final String PATH_SYS_ENV = "/sysenv";
	public static final String PATH_SHUTDOWN = "/shutdown";
	
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	protected Map<String, ControllerFactory> controllerFactories = new HashMap<String, ControllerFactory>();
	
	/**
	 * Calls {@link MainServlet#buildControllerFactories()}.
	 */
	public MainServlet() {
		super();
		buildControllerFactories();
	}
	
	/**
	 * Fills {@link #controllerFactories} with {@link ControllerFactorySingleton}
	 * for paths registered as <code>PATH_</code> constants in this class.
	 * <br>Overload this method to add controllers for your web-application. 
	 */
	protected void buildControllerFactories() {
		
		ControllerFactorySingleton homeFactory = new ControllerFactorySingleton(Home.getInstance()); 
		controllerFactories.put(PATH_HOME_ROOT, homeFactory);
		controllerFactories.put(PATH_HOME_HOME, homeFactory);
		controllerFactories.put(PATH_HOME_INDEX, homeFactory);
		ControllerFactorySingleton logFactory = new ControllerFactorySingleton(Log.getInstance()); 
		controllerFactories.put(PATH_LOG, logFactory);
		controllerFactories.put(PATH_LOG_ERROR, logFactory);
		controllerFactories.put(PATH_SYS_ENV, new ControllerFactorySingleton(SysEnv.getInstance()));
		controllerFactories.put(PATH_SHUTDOWN, new ControllerFactorySingleton(Shutdown.getInstance()));
	}
	
	protected Map<String, ControllerFactory> getControllerFactories() {
		return controllerFactories;
	}

	/**
	 * Uses {@link #getControllerFactories()} to get/create a Controller.
	 * @param path page-path (e.g. <tt>/index</tt>), see also {@link WebUtil#getPagePath(HttpServletRequest)}. 
	 * @return the controller handling the request, or null if no controller was found.
	 */
	protected Controller getController(String path) {
		
		Controller c = null;
		ControllerFactory cf = (path == null ? null : getControllerFactories().get(path));
		if (cf != null) {
			c = cf.build(path);
		}
		return c;
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
	 * If there is no controller, a 404 is returned (see {@link WebUtil#respondNotFound(HttpServletResponse, String, String)}).
	 * If the controller throws an error, an internal error is shown ({@link WebUtil#respondInternalError(HttpServletResponse, String, String)}).
	 * If the controller returns a string (usually a jsp-page like <tt>/WEB-INF/pages/home.jsp</tt>)
	 * the request-dispatcher is used to further handle the request.
	 * If there is no request-dispatcher for the string returned by the controller, an internal error is shown.
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
			// view is no longer null for a non-existing jsp-page, check if resource exists
			if (view == null || (viewName.endsWith(".jsp") && request.getServletContext().getResource(viewName) == null)) {
				log.error("Controller " + controller.getClass().getName() + " returned unavailable view " + viewName);
				WebUtil.respondInternalError(response, "Controller " + controller.getClass().getSimpleName() + " returned unavailable view " + viewName, null);
			} else {
				view.forward(request, response);
			}
		}
	}

}
