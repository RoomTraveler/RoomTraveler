<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원 수정</title>
</head>
<body>
    <%@ include file="/WEB-INF/views/fragments/header.jsp" %>
    
    <h1>회원 수정</h1>
    <form action="/member/update" method="post" class="m-3">

        <div class="mb-3 row">
            <label for="name" class="col-sm-2 col-form-label">이름</label>
            <div class="col-sm-10">
                <input type="text" name="name" id="name" class="form-control" required />
            </div>
        </div>

        <div class="mb-3 row">
            <label for="password" class="col-sm-2 col-form-label">비밀번호</label>
            <div class="col-sm-10">
                <input type="password" name="password" id="password" class="form-control" required />
            </div>
        </div>

        <button type="submit" class="btn btn-primary">수정</button>
    </form>
    
	<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
</body>
</html>
