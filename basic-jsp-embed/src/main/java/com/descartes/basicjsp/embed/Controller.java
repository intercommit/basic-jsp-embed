package com.descartes.basicjsp.embed;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Main interface for classes called by the main jsp-pages servlet ({@link MainServlet}) to handle a request.
 *    
 * @author frederikw
 *
 */
public interface Controller {

	/** The name of the attribute for the title of the web-page, used in jsp-files. */
	String PAGE_TITLE = "pageTitle";
	
	/** 
	 * Called by {@link MainServlet} to handle a request.
	 * Any {@link Exception} from this method is catched by the {@link MainServlet}
	 * in which case the {@link MainServlet} will send a 500 "internal server error" response.
	 * @return null (response has been handled by controller) or the jsp-page to display (e.g. <tt>/WEB-INF/pages/home.jsp</tt>).
	 */
	String handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
