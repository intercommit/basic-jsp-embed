package com.descartes.basicjsp.embed;

import javax.servlet.http.HttpServletRequest;

/**
 * Used by {@link MainServlet} to get a {@link Controller} for handling a request.
 * Controllers can be created for each request or the same controller can be returned for each request.
 * This interface exists to allow for these different cases.
 * @author fwiers
 *
 */
public interface ControllerFactory {

	/**
	 * Returns a controller suitable for handling the request at the given path.
	 * One controller factory can be registered for different paths,
	 * but it may also be registered for one path in which case the path will always be the same
	 * (see also {@link MainServlet#getController(String)}).
	 * @param path see also {@link WebUtil#getPagePath(HttpServletRequest)}.
	 */
	public Controller build(String path);
	
}
