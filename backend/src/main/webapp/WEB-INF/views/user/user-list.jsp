<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>사용자 리스트</title>
</head>
<body>
    <%@ include file="/WEB-INF/views/fragments/header.jsp" %>

    <h1>사용자 리스트</h1>
    
    <table>
    	<tr>
    		<td>userId</td>
    		<td>username</td>
    		<td>email</td>
    		<td>passwordHash</td>
    		<td>role</td>
    	</tr>
    	<c:forEach items="${users}" var="user">
	    	<tr>
	    		<td>${user.userId}</td>
	    		<td>${user.username}</td>
	    		<td>${user.email}</td>
	    		<td>${user.passwordHash}</td>
	    		<td>${user.role}</td>
	    	</tr>
    	</c:forEach>
    </table>

	<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
</body>
</html>