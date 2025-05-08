<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>보안 관리</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <style>
        .security-card {
            margin-bottom: 20px;
            transition: transform 0.3s;
        }
        .security-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .security-icon {
            font-size: 2.5rem;
            margin-bottom: 15px;
            color: #0d6efd;
        }
        .warning-icon {
            color: #dc3545;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container mt-5">
        <h2 class="mb-4">보안 관리</h2>
        
        <c:if test="${not empty message}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <div class="row">
            <div class="col-md-6">
                <div class="card security-card">
                    <div class="card-body text-center">
                        <div class="security-icon">
                            <i class="bi bi-shield-lock"></i>
                        </div>
                        <h4 class="card-title">비밀번호 마이그레이션</h4>
                        <p class="card-text">
                            평문 비밀번호를 안전한 암호화된 형식으로 마이그레이션합니다.
                            이 작업은 모든 사용자의 비밀번호를 암호화하여 보안을 강화합니다.
                        </p>
                        <form action="${pageContext.request.contextPath}/admin/security/migrate-passwords" method="post">
                            <button type="submit" class="btn btn-primary" onclick="return confirm('모든 사용자의 비밀번호를 마이그레이션하시겠습니까?')">
                                <i class="bi bi-shield-check"></i> 비밀번호 마이그레이션 실행
                            </button>
                        </form>
                    </div>
                </div>
            </div>
            
            <div class="col-md-6">
                <div class="card security-card">
                    <div class="card-body text-center">
                        <div class="security-icon">
                            <i class="bi bi-graph-up"></i>
                        </div>
                        <h4 class="card-title">마이그레이션 상태</h4>
                        <p class="card-text">
                            현재 비밀번호 마이그레이션 상태를 확인합니다.
                            마이그레이션이 필요한 사용자 수와 이미 마이그레이션된 사용자 수를 확인할 수 있습니다.
                        </p>
                        <a href="${pageContext.request.contextPath}/admin/security/password-migration-status" class="btn btn-outline-primary">
                            <i class="bi bi-clipboard-data"></i> 마이그레이션 상태 확인
                        </a>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="card security-card mt-4">
            <div class="card-body">
                <div class="d-flex align-items-center mb-3">
                    <div class="security-icon warning-icon me-3">
                        <i class="bi bi-exclamation-triangle"></i>
                    </div>
                    <h4 class="card-title mb-0">주의사항</h4>
                </div>
                <ul>
                    <li>비밀번호 마이그레이션은 시스템 부하가 적은 시간에 실행하는 것이 좋습니다.</li>
                    <li>마이그레이션 중에는 사용자의 로그인 요청이 지연될 수 있습니다.</li>
                    <li>마이그레이션 후에는 사용자가 기존 비밀번호로 로그인할 수 있지만, 비밀번호는 암호화되어 저장됩니다.</li>
                    <li>이 작업은 되돌릴 수 없으므로 데이터베이스 백업을 먼저 수행하는 것이 좋습니다.</li>
                </ul>
            </div>
        </div>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>