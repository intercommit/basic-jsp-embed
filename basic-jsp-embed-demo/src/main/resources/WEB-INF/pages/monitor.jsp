<%@ include file="/includes/mheader.jsp" %> 
<%@ page session="false" %>
<body>
<h2>JMX monitor</h2>
<p>
Credits go to Tomasz Nurkiewicz for providing
the <a href="http://nurkiewicz.blogspot.nl/2011/03/jolokia-highcharts-jmx-for-human-beings.html">example</a>
<br>Charts rendered by <a href="http://www.highcharts.com">Highcharts</a>
</p>

<script>
var jolokiaAgentUrl = "${domainRoot}/jmxagent"; 
</script>

<div class="portlet ui-widget-content ui-helper-clearfix ui-corner-all" id="portlet-template">
	<div class="portlet-header ui-widget-header ui-corner-all"><span class='ui-icon ui-icon-minusthick'></span><span class="title">&nbsp;</span></div>
	<div class="portlet-content"></div>
</div>

<div class="column"></div>

<!-- 
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.1/jquery-ui.min.js"></script>
 -->
 
<script src="${domainRoot}/static/jquery-ui-1.11.2/external/jquery/jquery.js"></script>
<script src="${domainRoot}/static/jquery-ui-1.11.2/jquery-ui.min.js"></script>
<script src="${domainRoot}/static/jolokia.js"></script>
<script src="${domainRoot}/static/jolokia-simple.js"></script>
<script src="${domainRoot}/static/highcharts.src.js"></script>
<script src="${domainRoot}/static/jmx-monitor.js"></script>

</body>
</html>
