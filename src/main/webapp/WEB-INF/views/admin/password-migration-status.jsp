<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>비밀번호 마이그레이션 상태</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <style>
        .status-card {
            margin-bottom: 20px;
            border-radius: 10px;
            overflow: hidden;
        }
        .status-icon {
            font-size: 3rem;
            margin-bottom: 15px;
        }
        .status-icon.success {
            color: #198754;
        }
        .status-icon.warning {
            color: #ffc107;
        }
        .status-icon.danger {
            color: #dc3545;
        }
        .progress {
            height: 25px;
            margin-bottom: 20px;
        }
        .progress-bar {
            font-size: 1rem;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container mt-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>비밀번호 마이그레이션 상태</h2>
            <a href="${pageContext.request.contextPath}/admin/security" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> 보안 관리로 돌아가기
            </a>
        </div>
        
        <!-- 마이그레이션 진행 상태 -->
        <div class="card status-card">
            <div class="card-body">
                <h4 class="card-title mb-4">마이그레이션 진행 상태</h4>
                
                <c:set var="totalUsers" value="${totalUsers}" />
                <c:set var="migratedUsers" value="${migratedUsers}" />
                <c:set var="pendingUsers" value="${totalUsers - migratedUsers}" />
                <c:set var="progressPercentage" value="${totalUsers > 0 ? (migratedUsers / totalUsers) * 100 : 0}" />
                
                <div class="progress">
                    <div class="progress-bar bg-success" role="progressbar" 
                         style="width: ${progressPercentage}%;" 
                         aria-valuenow="${progressPercentage}" 
                         aria-valuemin="0" 
                         aria-valuemax="100">
                        ${progressPercentage}%
                    </div>
                </div>
                
                <div class="row text-center">
                    <div class="col-md-4">
                        <div class="status-icon">
                            <i class="bi bi-people-fill"></i>
                        </div>
                        <h5>전체 사용자</h5>
                        <p class="fs-4">${totalUsers}명</p>
                    </div>
                    <div class="col-md-4">
                        <div class="status-icon success">
                            <i class="bi bi-shield-check"></i>
                        </div>
                        <h5>마이그레이션 완료</h5>
                        <p class="fs-4">${migratedUsers}명</p>
                    </div>
                    <div class="col-md-4">
                        <div class="status-icon ${pendingUsers > 0 ? 'warning' : 'success'}">
                            <i class="bi bi-shield-exclamation"></i>
                        </div>
                        <h5>마이그레이션 필요</h5>
                        <p class="fs-4">${pendingUsers}명</p>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 마이그레이션 작업 -->
        <div class="card status-card">
            <div class="card-body">
                <h4 class="card-title mb-4">마이그레이션 작업</h4>
                
                <c:choose>
                    <c:when test="${pendingUsers > 0}">
                        <div class="alert alert-warning">
                            <i class="bi bi-exclamation-triangle-fill"></i>
                            아직 ${pendingUsers}명의 사용자 비밀번호가 마이그레이션되지 않았습니다.
                        </div>
                        <p>모든 사용자의 비밀번호를 안전하게 암호화하려면 마이그레이션을 실행하세요.</p>
                        <form action="${pageContext.request.contextPath}/admin/security/migrate-passwords" method="post" class="mt-3">
                            <button type="submit" class="btn btn-primary" onclick="return confirm('모든 사용자의 비밀번호를 마이그레이션하시겠습니까?')">
                                <i class="bi bi-shield-check"></i> 비밀번호 마이그레이션 실행
                            </button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-success">
                            <i class="bi bi-check-circle-fill"></i>
                            모든 사용자의 비밀번호가 안전하게 암호화되었습니다.
                        </div>
                        <p>모든 사용자의 비밀번호가 마이그레이션되었습니다. 추가 작업이 필요하지 않습니다.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>