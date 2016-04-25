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
 * Copied from http://stackoverflow.com/a/3593513
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

	public static String PAGES_PATH = "/pages";
	public static String STATIC_PATH = "/static";
	public static String WEBSOCKET_PATH = "/websocket";
	
	protected Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		// The request URI changed from Tomcat version 8.0.28 to 8.0.30:
		// the URI used to end with a / but this does not happen with later versions.
		String path = req.getRequestURI().substring(req.getContextPath().length());
		if (log.isDebugEnabled()) {
			log.debug("Filtering for " + path);
		}
		if (path.startsWith(STATIC_PATH) || path.startsWith(WEBSOCKET_PATH) || path.startsWith("/favicon.ico")) {
			// Allthough "/favicon.ico" should not occur, it happens when in browser Back is clicked.
			chain.doFilter(request, response); // Goes to default servlet.
		} else {
		    request.getRequestDispatcher(PAGES_PATH + path).forward(request, response);
		    // Goes to main (jsp) pages servlet.
		}		
	}

	@Override
	public void destroy() {}

}
