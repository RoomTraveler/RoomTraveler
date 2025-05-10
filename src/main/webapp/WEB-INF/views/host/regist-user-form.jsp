<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>호스트 회원가입</title>
<!-- Bootstrap CSS -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
    .registration-container {
        max-width: 700px;
        margin: 50px auto;
        padding: 30px;
        background-color: #f8f9fa;
        border-radius: 10px;
        box-shadow: 0 0 15px rgba(0,0,0,0.1);
    }
    .registration-header {
        text-align: center;
        margin-bottom: 30px;
    }
    .registration-header h2 {
        color: #343a40;
    }
    .registration-header p {
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
    .required-field::after {
        content: " *";
        color: red;
    }
</style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />

    <div class="container">
        <div class="registration-container">
            <div class="registration-header">
                <h2>호스트 회원가입</h2>
                <p>숙소를 등록하고 관리하기 위한 호스트 계정을 만들어보세요</p>
            </div>

            <form action="${pageContext.request.contextPath}/host/regist-user" method="post">
                <!-- 호스트 역할 설정을 위한 hidden 필드 -->
                <input type="hidden" name="role" value="HOST">
                
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="username" class="form-label required-field">이름</label>
                            <input type="text" name="username" id="username" class="form-control" required />
                        </div>
                    </div>
                    
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="email" class="form-label required-field">이메일</label>
                            <input type="email" name="email" id="email" class="form-control" required />
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="password" class="form-label required-field">비밀번호</label>
                            <input type="password" name="password" id="password" class="form-control" required />
                        </div>
                    </div>
                    
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="confirmPassword" class="form-label required-field">비밀번호 확인</label>
                            <input type="password" name="confirmPassword" id="confirmPassword" class="form-control" required />
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <label for="phone" class="form-label">휴대폰 번호</label>
                    <input type="text" name="phone" id="phone" class="form-control" placeholder="예: 010-1234-5678" />
                </div>

                <div class="alert alert-info">
                    <p><strong>호스트 가입 안내:</strong></p>
                    <ul>
                        <li>가입 후 호스트 정보를 추가로 등록해야 합니다.</li>
                        <li>사업자 정보는 실제 등록된 정보와 일치해야 합니다.</li>
                        <li>허위 정보 기재 시 호스트 자격이 박탈될 수 있습니다.</li>
                    </ul>
                </div>

                <div class="d-grid gap-2">
                    <button type="submit" class="btn btn-host">호스트로 가입하기</button>
                </div>
            </form>

            <!-- 페이지에서 발생한 에러를 출력하는 영역 -->
            <c:if test="${!empty error}">
                <div class="alert alert-danger mt-3" role="alert">${error}</div>
            </c:if>

            <div class="text-center mt-4">
                <p>이미 계정이 있으신가요? <a href="${pageContext.request.contextPath}/host/login-form">호스트 로그인</a></p>
                <p><a href="${pageContext.request.contextPath}/user/regist-user-form">일반 사용자로 가입하기</a></p>
            </div>
        </div>
    </div>

    <jsp:include page="../fragments/footer.jsp" />

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        // 비밀번호 확인 검증
        document.querySelector('form').addEventListener('submit', function(e) {
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            if (password !== confirmPassword) {
                e.preventDefault();
                alert('비밀번호가 일치하지 않습니다.');
                document.getElementById('confirmPassword').focus();
            }
        });
    </script>
</body>
</html>