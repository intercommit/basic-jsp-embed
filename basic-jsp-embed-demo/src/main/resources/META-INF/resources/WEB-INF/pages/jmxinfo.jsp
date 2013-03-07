<%@ include file="/includes/header.jsp" %> 
<body>
<p>
JMX server info.
</p>
<pre>
<div id="jmxinfo"></div>
</pre>

<script>
var jolokiaAgentUrl = "<c:url value="/jmxagent"/>"; 
</script>

<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="<c:url value="/static/jolokia.js"/>"></script>
<script src="<c:url value="/static/jolokia-simple.js"/>"></script>
<script src="<c:url value="/static/jmx-info.js"/>"></script>

</body>
</html>


