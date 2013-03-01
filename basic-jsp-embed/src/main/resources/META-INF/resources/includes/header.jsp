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
<c:choose>
<c:when test="${empty pageTitle}">
<title>Home [${appName}]</title>
</c:when><c:otherwise>
<title>${pageTitle} [${appName}]</title>
</c:otherwise>
</c:choose>
</head>
