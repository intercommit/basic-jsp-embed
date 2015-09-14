<%@ include file="/includes/header.jsp" %> 
<%@ page session="false" %>
<body>
<p style="float:right">Back to <a href="${domainRoot}/">home</a> page.</p>
<p>
${logTextInfo}
</p>
<pre>
<c:out value="${logText}"/>
</pre>
<p>
Back to <a href="${domainRoot}/">home</a> page.
</p>
</body>
</html>
