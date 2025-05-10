<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>호스트 로그인</title>
<!-- Bootstrap CSS -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
    .login-container {
        max-width: 500px;
        margin: 50px auto;
        padding: 30px;
        background-color: #f8f9fa;
        border-radius: 10px;
        box-shadow: 0 0 15px rgba(0,0,0,0.1);
    }
    .login-header {
        text-align: center;
        margin-bottom: 30px;
    }
    .login-header h2 {
        color: #343a40;
    }
    .login-header p {
        color: #6c757d;
    }
    .form-group {
        margin-bottom: 20px;
    }
    .btn-host {
        background-color: #28a745;
        color: white;
    }
    .btn-host:hover {
        background-color: #218838;
        color: white;
    }
</style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />

    <div class="container">
        <div class="login-container">
            <div class="login-header">
                <h2>호스트 로그인</h2>
                <p>숙소 관리를 위해 로그인해주세요</p>
            </div>

            <form action="${pageContext.request.contextPath}/host/login" method="post">
                <div class="form-group">
                    <label for="email" class="form-label">이메일</label>
                    <input type="email" name="email" id="email" class="form-control" required />
                </div>

                <div class="form-group">
                    <label for="password" class="form-label">비밀번호</label>
                    <input type="password" name="password" id="password" class="form-control" required />
                </div>

                <div class="d-grid gap-2">
                    <button type="submit" class="btn btn-host">로그인</button>
                </div>
            </form>

            <!-- 페이지에서 발생한 에러를 출력하는 영역 -->
            <c:if test="${!empty error}">
                <div class="alert alert-danger mt-3" role="alert">${error}</div>
            </c:if>

            <div class="text-center mt-4">
                <p>아직 호스트 계정이 없으신가요? <a href="${pageContext.request.contextPath}/host/regist-user-form">호스트로 가입하기</a></p>
                <p><a href="${pageContext.request.contextPath}/user/login-form">일반 사용자 로그인</a></p>
            </div>
        </div>
    </div>

    <jsp:include page="../fragments/footer.jsp" />

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>