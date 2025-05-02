<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원 리스트</title>
</head>
<body>
    <%@ include file="/WEB-INF/views/fragments/header.jsp" %>

    <h1>회원 리스트</h1>
    
    <table>
    	<tr>
    		<td>mno</td>
    		<td>name</td>
    		<td>email</td>
    		<td>password</td>
    		<td>role</td>
    	</tr>
    	<c:forEach items="${members }" var="member">
	    	<tr>
	    		<td>${member.mno }</td>
	    		<td>${member.name }</td>
	    		<td>${member.email }</td>
	    		<td>${member.password }</td>
	    		<td>${member.role }</td>
	    	</tr>
    	</c:forEach>
    </table>
    

    <%@ include file="/WEB-INF/views/fragments/header.jsp" %>
	<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
</body>
</html>
