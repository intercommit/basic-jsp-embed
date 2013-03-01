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
