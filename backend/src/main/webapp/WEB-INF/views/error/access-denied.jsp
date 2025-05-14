<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>접근 거부</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <style>
        .error-container {
            text-align: center;
            padding: 100px 0;
        }
        .error-icon {
            font-size: 5rem;
            color: #dc3545;
            margin-bottom: 20px;
        }
        .error-title {
            font-size: 2.5rem;
            margin-bottom: 20px;
        }
        .error-message {
            font-size: 1.2rem;
            margin-bottom: 30px;
            color: #6c757d;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container">
        <div class="error-container">
            <div class="error-icon">
                <i class="bi bi-shield-lock-fill"></i>
            </div>
            <h1 class="error-title">접근 거부</h1>
            <p class="error-message">
                죄송합니다. 이 페이지에 접근할 권한이 없습니다.<br>
                필요한 권한이 없거나 로그인이 필요할 수 있습니다.
            </p>
            <div class="d-flex justify-content-center gap-3">
                <a href="/" class="btn btn-primary">
                    <i class="bi bi-house-door"></i> 홈으로 돌아가기
                </a>
                <a href="/user/login-form" class="btn btn-outline-secondary">
                    <i class="bi bi-box-arrow-in-right"></i> 로그인
                </a>
            </div>
        </div>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>