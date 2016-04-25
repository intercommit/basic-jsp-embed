package com.descartes.basicjsp.embed.demo.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// http://svn.apache.org/viewvc/tomcat/trunk/webapps/examples/WEB-INF/classes/websocket/echo/EchoAsyncAnnotation.java?view=markup

@javax.websocket.server.ServerEndpoint(value = "/websocket/echoAsyncAnnotation", configurator = WsConfigurer.class)
public class WsEchoAsync {

	private static final Logger log = LoggerFactory.getLogger(WsEchoAsync.class);

	private static final Future<Void> COMPLETED = new CompletedFuture();

	Future<Void> f = COMPLETED;
	StringBuilder sb;
	ByteArrayOutputStream bytes;
	String rloc = StringUtils.EMPTY;
	
	// Show the remote IP
	// http://stackoverflow.com/a/17994303
	@OnOpen
    public void open(Session session, EndpointConfig config) {

		HttpSession hsession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
		if (hsession == null) {
			log.debug("No HTTP session available for new websocket connection.");
		} else {
			rloc = (String) hsession.getAttribute(WsFilter.ATTRIB_REMOTE_LOCATION);
			if (rloc == null) {
				rloc = session.getId();
				log.debug("{} No remote location found in HTTP session for new websocket connection.", rloc);
			} else {
				log.debug("{} new websocket connection.", rloc);
			}
		}
    }
	
	@OnClose
    public void close(Session session, CloseReason reason) {
		
		if (log.isDebugEnabled()) {
			log.debug("{} Websocket connection closed: {}", rloc, toString(reason));
		}
	}
	
	public static String toString(CloseReason cr) {
		
		if (cr == null || (cr.getCloseCode() == null && cr.getReasonPhrase() == null)) {
			return "<unknown reason>";
		}
		StringBuilder sb = new StringBuilder();
		if (cr.getCloseCode() != null) {
			sb.append(cr.getCloseCode().toString()).append(" (").append(cr.getCloseCode().getCode()).append(')');
		}
		if (StringUtils.isNotBlank(cr.getReasonPhrase())) {
			sb.append(" - ").append(cr.getReasonPhrase());
		}
		return sb.toString();
	}
	
	@OnMessage
	public void echoTextMessage(Session session, String msg, boolean last) {
		
		log.debug("{} Received text message (last: {}): {}", rloc, last, msg);
		if (sb == null) {
			sb = new StringBuilder();
		}
		sb.append(msg);
		if (last) {
			// Before we send the next message, have to wait for the previous
			// message to complete
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				// Let the container deal with it
				throw new RuntimeException(e);
			}
			String text = sb.toString();
			log.debug("{} Sending text message: {}", rloc, text);
			f = session.getAsyncRemote().sendText(text);
			sb = null;
		}
	}

	@OnMessage
	public void echoBinaryMessage(byte[] msg, Session session, boolean last) throws IOException {
		
		log.debug("{} Received binary message (last: {}) with size {}", rloc, last, msg.length);
		if (bytes == null) {
			bytes = new ByteArrayOutputStream();
		}
		bytes.write(msg);
		if (last) {
			// Before we send the next message, have to wait for the previous
			// message to complete
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				// Let the container deal with it
				throw new RuntimeException(e);
			}
			byte[] buf = bytes.toByteArray();
			log.debug("{} Sending binary message with size {}", rloc, buf.length);
			f = session.getAsyncRemote().sendBinary(ByteBuffer.wrap(buf));
			bytes = null;
		}
	}

	/**
	 * Process a received pong. This is a NO-OP.
	 *
	 * @param pm    Ignored.
	 */
	@OnMessage
	public void echoPongMessage(PongMessage pm) {
		// NO-OP
		log.debug("{} Received pong message.", rloc);
	}

	private static class CompletedFuture implements Future<Void> {

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return true;
		}

		@Override
		public Void get() throws InterruptedException, ExecutionException {
			return null;
		}

		@Override
		public Void get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			return null;
		}
	}
}
