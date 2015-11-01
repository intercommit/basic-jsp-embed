<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%-- 
Domain root is required for retrieving static resources.
See also 
http://stackoverflow.com/questions/3655316/browser-cant-access-find-relative-resources-like-css-images-and-links-when-cal
 --%>
<c:set var="domainRoot" value="${pageContext.request.contextPath}" scope="request"/>
<link rel="shortcut icon" href="${domainRoot}/favicon.ico"/>
<link rel="stylesheet" href="${domainRoot}/static/app.css" type="text/css">
<c:choose>
<c:when test="${empty pageTitle}">
<title>Home [${appName}]</title>
</c:when><c:otherwise>
<title>${pageTitle} [${appName}]</title>
</c:otherwise>
</c:choose>
</head>
