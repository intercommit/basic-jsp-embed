package com.descartes.basicjsp.embed;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.valves.ErrorReportValve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When you just need plain text error reports, no HTML stuff.
 * <p>
 * Add to the Tomcat launcher {@code beforeStart()} with 		
 * <br>{@code ((StandardHost) tomcat.getHost()).setErrorReportValveClass(PlainTextErrorReportValve.class.getName()); }
 * @author FWiers
 *
 */
public class PlainTextErrorReportValve extends ErrorReportValve {
	
    private static final Logger log = LoggerFactory.getLogger(PlainTextErrorReportValve.class);

    /**
     * Prints out an error message in plain text..
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param throwable The exception that occurred (which possibly wraps
     *  a root cause exception
     */
    protected void report(Request request, Response response,
                          Throwable throwable) {

        // Do nothing on non-HTTP responses
        int statusCode = response.getStatus();

        // Do nothing on a 1xx, 2xx and 3xx status
        // Do nothing if anything has been written already
        if (statusCode < 400 || response.getContentWritten() > 0 ||
                !response.isError()) {
            return;
        }

        String message = RequestUtil.filter(response.getMessage());
        
        try {
            response.setContentType("text/plain");
            response.setCharacterEncoding("utf-8");
            if (message != null) {
            	response.getWriter().print(message);
            }
            response.finishResponse();
        } catch (Throwable t) {
        	log.info("Could not send plain text error response: " + t);
        }
    }

}
