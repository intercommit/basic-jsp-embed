<%@ include file="/includes/header.jsp" %> 
<%@ page session="false" %>
<body>
<p>
JMX server info.
</p>
<pre>
<c:out value="${jmxInfoText}"/>
</pre>

<!--  No longer used -->
<!-- 
<script>
var jolokiaAgentUrl = "${domainRoot}/jmxagent"; 
</script>

<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="${domainRoot}/static/jolokia.js"></script>
<script src="${domainRoot}/static/jolokia-simple.js"></script>
<script src="${domainRoot}/static/jmx-info.js"></script>
 -->
</body>
</html>


