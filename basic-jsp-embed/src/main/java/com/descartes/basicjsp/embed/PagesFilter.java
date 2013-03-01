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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes requests for static content to the default (Tomcat) servlet
 * and routes requests for dynamic content to the jsp-pages servlet.
 * Routing is based on the requested URL path without the context-path.
 * <br>If the path starts with <tt>/static</tt> the request is forwarded to the default (Tomcat) servlet.
 * This servlet will look for the static content in <tt>&lt;resources-dir&gt;/static</tt>
 * which is at the same (folder) level as the <tt>WEB-INF</tt> folder, but NOT in the <tt>WEB-INF</tt> folder.
 * <br>Any other paths are pre-fixed with <tt>/pages</tt> and forwared to the jsp-pages servlet 
 * that is registered in <tt>web.xml</tt> with url-pattern <tt>/pages/*</tt>.
 * <p>
 * Copied from http://stackoverflow.com/questions/132052/servlet-for-serving-static-content
 * <p>
 * Register this class in <tt>web.xml</tt> in the following manner:
<pre>&lt;filter&gt;
	&lt;filter-name&gt;PagesFilter&lt;/filter-name&gt;
	&lt;filter-class&gt;com.descartes.webapp.PagesFilter&lt;/filter-class&gt;
&lt;/filter&gt;
&lt;filter-mapping&gt;
	&lt;filter-name&gt;PagesFilter&lt;/filter-name&gt;
	&lt;url-pattern&gt;/*&lt;/url-pattern&gt;
&lt;/filter-mapping&gt;</pre>
 *
 */
public class PagesFilter implements Filter {

	protected Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		String path = req.getRequestURI().substring(req.getContextPath().length());
		if (log.isDebugEnabled()) {
			log.debug("Filtering for " + path);
		}
		if (path.startsWith("/static") || path.equals("/favicon.ico")) {
			// Allthough "/favicon.ico" should not occur, it happens when in browser Back is clicked.
			chain.doFilter(request, response); // Goes to default servlet.
		} else {
		    request.getRequestDispatcher("/pages" + path).forward(request, response);
		    // Goes to main (jsp) pages servlet.
		}		
	}

	@Override
	public void destroy() {}

}
