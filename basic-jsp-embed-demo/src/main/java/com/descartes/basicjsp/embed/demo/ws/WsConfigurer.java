package com.descartes.basicjsp.embed.demo.ws;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The configurator sits between the {@link WsFilter} and the {@link WsEchoAsync}.
 * See also http://stackoverflow.com/a/17994303.
 */
public class WsConfigurer extends ServerEndpointConfig.Configurator {

	private static final Logger log = LoggerFactory.getLogger(WsFilter.class);

	@Override
	public void modifyHandshake(ServerEndpointConfig config,
			HandshakeRequest request,
			HandshakeResponse response) {

		HttpSession httpSession = (HttpSession) request.getHttpSession();
		if (httpSession == null) {
			log.debug("No session for Websocket request.");
		} else {
			config.getUserProperties().put(HttpSession.class.getName(), httpSession);
		}
	}
}
