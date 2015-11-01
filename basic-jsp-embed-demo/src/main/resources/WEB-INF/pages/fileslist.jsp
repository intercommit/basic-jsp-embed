<%@ include file="/includes/header.jsp" %> 
<%@ page session="false" %>
<body>
<p style="float:right">Back to <a href="${domainRoot}/dirtree">directory</a> tree.</p>

Files in directory ${path}

<table class="gridtable" style="align: center;">
	<tr>
		<th title="Download">D</th>
		<th title="View">V</th>
		<th>Name</th>
		<th>Modified</th>
		<th>Size</th>
	</tr>
	<c:forEach items="${files}" var="file">
	<tr>
		<td style="width : 1em"><a href="${domainRoot}/openfile?dir=${dirId}&amp;name=${file.nameb64}&amp;action=download"><img src="${domainRoot}/static/images/download.png"/></a></td>
		<td style="width : 1em"><a href="${domainRoot}/openfile?dir=${dirId}&amp;name=${file.nameb64}&amp;action=view"><img src="${domainRoot}/static/images/eye.png"/></a></td>
		<td>${file.name}</td>
		<td>${file.modified}</td>
		<td style="text-align:right">${file.size}</td>
	</tr>
	</c:forEach>
</table>

<p>
Back to <a href="${domainRoot}/dirtree">directory</a> tree.
</p>

</body>
</html>
