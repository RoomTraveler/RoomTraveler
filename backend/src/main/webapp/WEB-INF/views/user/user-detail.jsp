<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>사용자 정보</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>

<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<div class="container mt-5">

    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card shadow-sm">
                <div class="card-body">
                    <h5 class="card-title text-center mb-3">사용자 정보</h5>
                    <ul class="list-group list-group-flush">
                        <li class="list-group-item"><strong>이름:</strong> ${username}</li>
                        <li class="list-group-item"><strong>이메일:</strong> ${email}</li>
                        <li class="list-group-item"><strong>비밀번호:</strong> ${passwordHash}</li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>


<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>