<%@ include file="/includes/header.jsp" %> 
<%@ page session="false" %>
<body>
<h2>${appName}</h2>
<div style="font-size:0.8em; float:right">Version ${appVersion}</div>
<div style="clear:both"></div>
Demo for Basic JSP Embedded
<br>
<br>Show Tomcat <a href="${domainRoot}/dirtree">directory</a> tree.
<br>
<br>Show performance <a href="${domainRoot}/monitor">monitor</a>
(uses the <a href="http://jolokia.org">Jolokia</a> JMX <a href="${domainRoot}/jmxagent">agent</a>).
<br>Show JMX server <a href="${domainRoot}/jmxinfo">info</a>
<br>
<br>Inherited from the Basic JSP Embed project:
<ul><li>
View <a href="${domainRoot}/log">log</a>
<a href="${domainRoot}/logerror">error</a> messages.
</li><li>
View system <a href="${domainRoot}/sysenv">environment</a> settings.
</li><li>
<a href="${domainRoot}/shutdown">Shutdown</a> application.
</li></ul>
</body>
</html>
