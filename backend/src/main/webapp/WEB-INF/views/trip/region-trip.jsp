<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>지역별 관광지 정보</title>
</head>
<body>
    <%@ include file="/WEB-INF/views/fragments/header.jsp" %>

    <h1>지역별 관광지 정보</h1>
    <h3>${sido }</h3>
     <h3>${gugun }</h3>
    
    <table>
    	<c:forEach items="${result }" var="res">
	    	<tr>
	    		<td>${res.title }</td>
	    		<td>${res.image }</td>
	    		<td>${res.addr1 }</td>
	    		<td>${res.addr2 }</td>
	    		<td>${res.tel }</td>
	    	</tr>
    	</c:forEach>
    </table>
   
	<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
</body>
</html>
