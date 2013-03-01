<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="<c:url value="/static/app.css"/>" type="text/css">
<link rel="shortcut icon" href="<c:url value="/favicon.ico"/>"/>
<style type="text/css">
ul.tree,ul.tree ul {
	list-style-type: none;
	background: url(<c:url value="/static/images/vline.png"/>) repeat-y;
	margin: 0;
	padding: 0;
}
ul.tree ul {
	margin-left: 10px;
}
ul.tree li {
	margin: 0;
	padding: 0 12px;
	line-height: 20px;
	background: url(<c:url value="/static/images/node.png"/>) no-repeat;
	color: #369;
	font-weight: bold;
}
ul.tree li.last {
	background: #fff url(<c:url value="/static/images/lastnode.png"/>) no-repeat;
}
</style>
<script type="text/javascript">
	window.onload = function() {
		var tree = document.getElementById("tree");
		var lists = [ tree ];
		for ( var i = 0; i < tree.getElementsByTagName("ul").length; i++)
			lists[lists.length] = tree.getElementsByTagName("ul")[i];
		for ( var i = 0; i < lists.length; i++) {
			var item = lists[i].lastChild;
			while (!item.tagName || item.tagName.toLowerCase() != "li")
				item = item.previousSibling;
			item.className += " last";
		}
	};
</script>

<c:choose>
<c:when test="${empty pageTitle}">
<title>Home [${appName}]</title>
</c:when><c:otherwise>
<title>${pageTitle} [${appName}]</title>
</c:otherwise>
</c:choose>
</head>

<body>

<p>
Directory tree
</p>

<ul class="tree" id="tree">
<c:forEach items="${dirs}" var="dir">
<li><a href="<c:url value="/dirfiles"/>?dir=${dir.id}"><c:out value="${dir.name}"/></a>${dir.levelEnd}
</c:forEach>
${closeDirLevel}
</ul>
 
<form action="/dirtree" method="post">
<button type="submit" name="submit" value="refresh" style="float: right;">Refresh</button>
</form>

<p>
Back to <a href="<c:url value="/"/>">home</a> page.
</p>

</body>
</html>

