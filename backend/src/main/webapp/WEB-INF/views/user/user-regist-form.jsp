<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>사용자 등록</title>
</head>
<body>
    <%@ include file="/WEB-INF/views/fragments/header.jsp" %>

    <h1>사용자 등록</h1>
    <form action="/user/regist-user" method="post" class="m-3">

        <div class="mb-3 row">
            <label for="username" class="col-sm-2 col-form-label">이름</label>
            <div class="col-sm-10">
                <input type="text" name="username" id="username" class="form-control" required />
            </div>
        </div>

        <div class="mb-3 row">
            <label for="email" class="col-sm-2 col-form-label">이메일</label>
            <div class="col-sm-10">
                <input type="email" name="email" id="email" class="form-control" required />
            </div>
        </div>

        <div class="mb-3 row">
            <label for="passwordHash" class="col-sm-2 col-form-label">비밀번호</label>
            <div class="col-sm-10">
                <input type="password" name="passwordHash" id="passwordHash" class="form-control" required />
            </div>
        </div>

        <div class="mb-3 row">
            <label for="phone" class="col-sm-2 col-form-label">휴대폰 번호</label>
            <div class="col-sm-10">
                <input type="text" name="phone" id="phone" class="form-control" />
            </div>
        </div>

        <button type="submit" class="btn btn-primary">등록</button>
    </form>
    <!-- 페이지에서 발생한 에러를 출력하는 영역 -->
    <c:if test="${!empty error }">
        <div class="alert alert-danger" role="alert">${error }</div>
    </c:if>

	<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>
</body>
</html>