package com.descartes.basicjsp.embed.ssl;

import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.jsse.JSSESocketFactory;

/**
 * Class returned by {@link TomcatCustomSslContextImplementation} with options 
 * to specify a custom SslContext. Tomcat calls the methods {@link #createSSLContext()}
 * and then intializes the context using {@link #getKeyManagers()} and {@link #getTrustManagers()}.
 * <br>Since these classes are instantiated using reflection,
 * use {@link #customSslContext} to set the class providing the context.  
 * @author fwiers
 *
 */
public class TomcatCustomSslContextSocketFactory extends JSSESocketFactory {
	
	public static final AtomicReference<ITomcatCustomSslContext> customSslContext = new AtomicReference<ITomcatCustomSslContext>();

	public TomcatCustomSslContextSocketFactory(AbstractEndpoint<?> endpoint) {
		super(endpoint);
	}

    @Override
    public SSLContext createSSLContext() throws Exception {
    	return (customSslContext.get() == null ? super.createSSLContext() : customSslContext.get().createSSLContext(this));
    }

    @Override
    public KeyManager[] getKeyManagers() throws Exception {
    	return (customSslContext.get() == null ? super.getKeyManagers() : customSslContext.get().getKeyManagers(this));
    }

    @Override
    public TrustManager[] getTrustManagers() throws Exception {
    	return (customSslContext.get() == null ? super.getTrustManagers() : customSslContext.get().getTrustManagers(this));
    }

}
