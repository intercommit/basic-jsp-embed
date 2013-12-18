<%@ include file="/includes/header.jsp" %> 
<%@ page session="false" %>
<body>
<p style="float:right">Back to <a href="${domainRoot}/">home</a> page.</p>
Memory usage
<pre>
<c:out value="${memoryUsage}"/>
</pre>
Operating system settings
<pre>
<c:out value="${systemEnv}"/>
</pre>
Java system properties
<pre>
<c:out value="${systemProps}"/>
</pre>
<p>
Back to <a href="${domainRoot}/">home</a> page.</p>
</body>
</html>