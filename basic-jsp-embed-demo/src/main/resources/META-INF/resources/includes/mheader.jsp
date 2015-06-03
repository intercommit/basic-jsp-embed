<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<c:set var="domainRoot" value="${pageContext.request.contextPath}" />
<link rel="shortcut icon" href="${domainRoot}/favicon.ico"/>
<link rel="stylesheet" type="text/css" href="${domainRoot}/static/app.css">
<link rel="stylesheet" type="text/css" href="${domainRoot}/static/chartportlet.css">
<link rel="stylesheet" type="text/css" href="${domainRoot}/static/jquery-ui-1.11.2/jquery-ui.min.css">
<!-- 
<link rel="stylesheet" type="text/css" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.10.1/themes/start/jquery-ui.css"/>
 -->
<title>${pageTitle} [${appName}]</title>
</head>
