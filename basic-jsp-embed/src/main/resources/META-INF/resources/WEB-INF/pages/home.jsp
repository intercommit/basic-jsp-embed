<%@ include file="/includes/header.jsp" %> 
<%@ page session="false" %>
<body>
<h2>${appName}</h2>
<div style="font-size:0.8em; float:right">Version ${appVersion}</div>
<div style="clear:both"></div>
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
