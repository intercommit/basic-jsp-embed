/**
 * Securing the Tomcat connector requires some extra configuration.
 * For example, in {@link com.descartes.basicjsp.embed.LaunchWebApp#beforeStart()}, add the following:
 * <code><pre>
Connector con = tomcat.getConnector();
con.setScheme("https");
Http11NioProtocol proto = (Http11NioProtocol) con.getProtocolHandler();
proto.setSecure(true);
proto.setSSLEnabled(true);
proto.setSslProtocol("TLSv1.2");

String certDir = AppBoot.getConfDir();
if (isMavenTest()) {
	certDir = AppBoot.getHomeDir();
}
proto.setKeystoreFile(certDir + "app-server-cert.p12");
proto.setKeystoreType("PKCS12");
proto.setKeystorePass("changeit");
proto.setClientAuth("true"); // requests client certificate always
proto.setTruststoreFile(certDir + "app-truststore.jks");
proto.setTruststoreType("JKS");
proto.setTruststorePass("changeit");</pre></code>
 * 
 * But a custom implementation for providing the key-managers and trust-managers is not possible.
 * The classes in this package make it possible.
 * In the above code, replace the code from <tt>String certDir = ...</tt> with:
<code><pre>
proto.setSslImplementationName(TomcatCustomSslContextImplementation.class.getName());
try {
	MyCertProvider certProvider = new MyCertProvider(...); // implements ITomcatCustomSslContext
	TomcatCustomSslContextSocketFactory.customSslContext.set(certProvider);
} catch (Exception e) {
	 // logging
}</pre></code> 
 * 
 */
package com.descartes.basicjsp.embed.ssl;
