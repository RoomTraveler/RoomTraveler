<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
    <%@ include file="/WEB-INF/views/fragments/header.jsp" %>

    <h1>회원가입</h1>
    <form action="/member/regist-member" method="post" class="m-3">

        <div class="mb-3 row">
            <label for="name" class="col-sm-2 col-form-label">이름</label>
            <div class="col-sm-10">
                <input type="text" name="name" id="name" class="form-control" required />
            </div>
        </div>

        <div class="mb-3 row">
            <label for="email" class="col-sm-2 col-form-label">이메일</label>
            <div class="col-sm-10">
                <input type="email" name="email" id="email" class="form-control" required />
            </div>
        </div>

        <div class="mb-3 row">
            <label for="password" class="col-sm-2 col-form-label">비밀번호</label>
            <div class="col-sm-10">
                <input type="password" name="password" id="password" class="form-control" required />
            </div>
        </div>

        <button type="submit" class="btn btn-primary">등록</button>
    </form>
    <!--TODO: 12-1. 페이지에서 발생한 에러를 출력하는 영역-->
    <c:if test="${!empty error }">
        <div class="alert alert-danger" role="alert">${error }</div>
    </c:if>

	<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
</body>
</html>
