package com.descartes.basicjsp.embed.ssl;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.tomcat.util.net.jsse.JSSESocketFactory;

/**
 * Interface providing a custom SSL Context for Tomcat.
 * See {@link TomcatCustomSslContextSocketFactory}.
 * @author fwiers
 *
 */
public interface ITomcatCustomSslContext {

	/**
	 * Create a SSL Context, it will be intialized using {@link #getKeyManagers(JSSESocketFactory)} and {@link #getTrustManagers(JSSESocketFactory)}.
	 * If no special context is required, use {@link JSSESocketFactory#createSSLContext()}.
	 */
	SSLContext createSSLContext(JSSESocketFactory factory) throws Exception;
	KeyManager[] getKeyManagers(JSSESocketFactory factory) throws Exception;
	TrustManager[] getTrustManagers(JSSESocketFactory factory) throws Exception;
	
}
