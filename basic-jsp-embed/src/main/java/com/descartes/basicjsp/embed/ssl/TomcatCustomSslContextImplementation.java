package com.descartes.basicjsp.embed.ssl;

import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SSLUtil;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;

/**
 * Tomcat expects all certificates in specific files.
 * "On the fly" SslContext creation is not supported.
 * Use this class to customize SslContext creation,
 * register it via: <pre>{@code
Connector con = tomcat.getConnector();
Http11NioProtocol proto = (Http11NioProtocol) con.getProtocolHandler();
proto.setSslImplementationName(TomcatCustomSslContextImplementation.class.getName());
 * }</pre>
 * This class is initialized via reflection by Tomcat.
 * <br>The options for specifying a custom SslContext must be programmed in the class
 * returned by {@link #getSSLUtil(AbstractEndpoint)}, which in this case is {@link TomcatCustomSslContextSocketFactory}. 
 * @author fwiers
 *
 */
public class TomcatCustomSslContextImplementation extends JSSEImplementation {
	

	/**
	 * Returns {@link TomcatCustomSslContextSocketFactory} instead of the default.
	 */
    @Override
    public SSLUtil getSSLUtil(AbstractEndpoint<?> endpoint) {
        return new TomcatCustomSslContextSocketFactory(endpoint);
    }
    
}
