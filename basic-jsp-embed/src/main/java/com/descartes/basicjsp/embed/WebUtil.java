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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebUtil {

	protected static Logger log = LoggerFactory.getLogger(WebUtil.class);

	/**
	 * Constructs the home-page URL as seen by the client.
	 * <br>Copied from http://stackoverflow.com/questions/2222238/httpservletrequest-to-complete-url
	 */
	public static String getAppHomeUrl(HttpServletRequest req) {

		String scheme = req.getScheme();             // http
		String serverName = req.getServerName();     // hostname.com
		int serverPort = req.getServerPort();        // 80
		String contextPath = req.getContextPath();   // /mywebapp

		// Reconstruct original requesting URL
		StringBuilder url = new StringBuilder();
		url.append(scheme).append("://").append(serverName);
		if ((serverPort != 80) && (serverPort != 443)) {
			url.append(":").append(serverPort);
		}
		url.append(contextPath);
		return url.toString();
	}

	/**
	 * Removes the context-path and "/pages" prefix from the request-URL so that only the requested page is returned.
	 * E.g. <tt>http://localhost/mywebapp/pages/index --&gt; index</tt>
	 * <br>This method can be used together with the {@link PagesFilter}.
	 * @param request
	 */
	public static String getPagePath(HttpServletRequest request) {
		return request.getRequestURI().substring(request.getContextPath().length()).substring("/pages".length());
	}
	
	/**
	 * Description of remote location between []. If used in log-statements, 
	 * it is best to cache this value in a local String. 
	 * @return [RemoteIP:port] from request.
	 */
	public static String getRemoteLocation(final HttpServletRequest request) {
		return "[" + request.getRemoteAddr() + ":" + request.getRemotePort() + "]";
	}

	/** Returns a String as bytes using the default encoding. */
	public static byte[] getBytes(String s) {
		return (s == null ? new byte[0] : s.getBytes(WebSetup.getInstance().getEncoding()));
	}

	/** Returns null or a trimmed, non-empty value for the parameter. */
	public static String getParamTrimmed(final HttpServletRequest request, final String paramName) {
		
		String v = request.getParameter(paramName);
		if (v != null) v = v.trim();
		return (v == null || v.isEmpty() ? null : v);
	}

	/** Sends a "404 Not Found: " response in plain text with the message appended. */
	public static void respondNotFound(HttpServletResponse response, String msg, String rloc) {
		respondMsg(response, HttpServletResponse.SC_NOT_FOUND, "404 Not Found: " + msg, rloc);
	}

	/** Sends a "500 Internal Server Error: " response in plain text with the message appended. */
	public static void respondInternalError(HttpServletResponse response, String msg, String rloc) {
		respondMsg(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "500 Internal Server Error: " + msg, rloc);
	}

	/** Sends a 200 "OK" response in plain text. */
	public static void respondOK(HttpServletResponse response, String rloc) {
		respondMsg(response, HttpServletResponse.SC_OK, "OK", rloc);
	}
	
	/**
	 * The default response method is wrapped by Tomcat into a html-page.
	 * This method sends the response in plain text.
	 * @param statusCode One of {@link HttpServletResponse} static "SC" codes. 
	 * @param statusMsg may not be null or empty.
	 */
	public static void respondMsg(HttpServletResponse response, int statusCode, String statusMsg, String rloc) {
		
		response.setStatus(statusCode);
		response.setHeader("Content-Type", "text/plain");
		try {
			byte[] msg = getBytes(statusMsg);
			response.setHeader("Content-Length", Integer.toString(msg.length));
			ServletOutputStream out = response.getOutputStream();
			out.write(msg);
			out.flush();
		} catch (Exception e) {
			log.warn((rloc == null ? "" : rloc + " ") + "Could not send response " + statusCode + ": " + statusMsg, e);
		}
	}
	
	/**
	 * Logs in debug-mode the headers, parameters and query-string of the request. 
	 * @param log Logger to use.
	 * @param request The request to log details about.
	 */
	public static void logRequestDetails(final Logger log, final HttpServletRequest request) {
		
		if (!log.isDebugEnabled()) return;
		StringBuilder sb = new StringBuilder(128);
		sb.append(getRemoteLocation(request)).append(" ");
		sb.append(request.getMethod()).append(" request details:");
		Enumeration<String> headerNames = request.getHeaderNames();
		if (headerNames == null || !headerNames.hasMoreElements()) {
			sb.append("\nNo headers.");
		} else {
			while (headerNames.hasMoreElements()) {
				String hname = headerNames.nextElement();
				String hvalue = request.getHeader(hname);
				if (hvalue != null) {
					sb.append("\nHeader ").append(hname).append(": ").append(hvalue);
				}
			}
		}
		Map<String, String[]> params = request.getParameterMap();
		int numberOfParams = params.keySet().size(); 
		if (numberOfParams == 0) {
			sb.append("\nNo parameters.");
		} else { 
			for(String paramName: params.keySet()) {
				sb.append("\nParam ").append(paramName).append(": ").append(Arrays.toString(params.get(paramName)));
			}
		}
		if (request.getQueryString() == null) {
			sb.append("\nNo query string.");
		} else {
			String q = request.getQueryString();
			try { q = URLDecoder.decode(request.getQueryString(), "UTF-8"); } catch (Exception ignored) {}
			sb.append("\nQuery string:\n").append(q);
		}
		log.debug(sb.toString());
	}
	
	/**
	 * Tries to read the contents from the Reader of the request.
	 * @param request the (post) request.
	 * @return null (failed to get any contents) or an non-empty String.
	 */
	public static String getRequestContent(final HttpServletRequest request) {
		
		boolean haveSomething = false;
		final StringBuilder sb = new StringBuilder("");
		BufferedReader r = null;
		try {
			r = request.getReader();
			char[] cbuf = new char[1024];
			int read = 0;
			while ((read = r.read(cbuf)) > 0) {
				haveSomething = true;
				sb.append(cbuf, 0, read);
			}
		} catch (Exception e) {
			log.warn("Could not read text from request: " + e);
			haveSomething = false;
		} finally {
			close(r);
		}
		return (haveSomething ? sb.toString() : null);
	}
	
	/** 
	 * Same as {@link #writeResponse(HttpServletResponse, String, String, String)} 
	 * but uses the default encoding for character set.
	 */
	public static void writeResponse(final HttpServletResponse response, final String contentType, final String output) {
		writeResponse(response, contentType, WebSetup.getInstance().getEncoding().name(), output);
	}

	/**
	 * Sends output to the client. Commits the response (no further writing possible).
	 * @param response may not already be committed.
	 * @param contentType Mandatory e.g. text/plain text/html text/xml
	 * @param encoding Mandatory e.g. UTF-8 or ISO-8859-1 (the default)
	 * @param output The text to send as output.
	 */
	public static void writeResponse(final HttpServletResponse response, final String contentType, final String encoding, final String output) {
		
		// Must call setContentType before setCharacterEncoding, else latter has no effect.
		try {
			response.setContentType(contentType);
			response.setCharacterEncoding(encoding);
			PrintWriter pw = response.getWriter();
			pw.write(output);
			pw.flush();
		} catch (Exception e) {
			log.warn("Could not write text-response: " + e);
		}
	}

	/** 
	 * Calls {@link #writeResponse(HttpServletResponse, String, String, InputStreamReader, boolean)} 
	 * but uses the default encoding for character set (see {@link WebSetup#getEncoding()}).
	 */
	public static void writeResponse(final HttpServletResponse response, final String contentType, 
			final InputStreamReader reader, final boolean closeReader) {
		writeResponse(response, contentType, WebSetup.getInstance().getEncoding().name(), reader, closeReader);
	}

	/** 
	 * Same as {@link #writeResponse(HttpServletResponse, String, String, String)} 
	 * but reads the text to send from the given InputStreamReader.
	 * Commits the response (no further writing possible).
	 * @param closeReader If true, given reader is always closed.
	 */
	public static void writeResponse(final HttpServletResponse response, final String contentType, final String encoding, 
			final InputStreamReader reader, final boolean closeReader) {
		
		// Must call setContentType before setCharacterEncoding, else latter has no effect.
		try {
			response.setContentType(contentType);
			response.setCharacterEncoding(encoding);
			PrintWriter pw = response.getWriter();
			char[] cbuf = new char[8192];
			int len = 0;
			while ((len = reader.read(cbuf)) > 0) {
				pw.write(cbuf, 0, len);
			}
			pw.flush();
		} catch (Exception e) {
			log.warn("Could not write text-response from reader: " + e);
		} finally {
			if (closeReader) close(reader);
		}
	}

	/** 
	 * Calls {@link #writeResponse(HttpServletResponse, String, String, InputStream, boolean)}
	 *  with a general content type for bytes (causes download windows to appear in browser). 
	 */
	public static void writeResponse(final HttpServletResponse response, final String fileName, 
			final InputStream in, final boolean closeIn) {
		writeResponse(response, "application/octet-stream", fileName, in, closeIn);
	}

	/** 
	 * Sends a file in bytes back to the client. Commits the response (no further writing possible).  
	 * General content-type is "application/octet-stream", but for example for pdf use "application/pdf". 
	 * @param closeIn If true, given input-stream is always closed.
	 */
	public static void  writeResponse(final HttpServletResponse response, final String contentType, 
			final String fileName, final InputStream in, final boolean closeIn) {
		
		OutputStream out = null;
		try {
			response.setContentType(contentType);
			response.setHeader("Content-Disposition","attachment;filename="+fileName);
			out = response.getOutputStream();
			copyStreams(in, out);
			out.flush();
		} catch (Exception e) {
			log.warn("Could not write byte-response from inputstream: " + e);
		} finally {
			if (closeIn) close(in);
		}
	}

	/** Copies all bytes from inputstream to outputstream. Does NOT close the streams. */
	public static final void copyStreams(final InputStream in, final OutputStream out) throws IOException {
		copyStreams(in , out, new byte[16384]);
	}
	
	/** Copies all bytes from inputstream to outputstream using the provided buffer (must have size > 0). 
	 * Use this when many copy-operations are done in a thread-safe manner to save memory. Does NOT close the streams. */
	public static final void copyStreams(final InputStream in, final OutputStream out, final byte[] buf) throws IOException {

		// Transfer bytes from in to out
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	}

	/**
	 * Closes the closable if it is not null.
	 * Logs a warning when an error occurs.
	 */
	public static void close(final Closeable closable) {
		if (closable != null) {
			try { closable.close(); } catch (IOException ioe) {
				log.warn("Failed to close stream: " + ioe);
			}
		}
	}

}
