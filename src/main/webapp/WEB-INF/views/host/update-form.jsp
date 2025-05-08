<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>호스트 정보 수정</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .form-container {
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f8f9fa;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        .required-field::after {
            content: " *";
            color: red;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />

    <div class="container mt-5 mb-5">
        <h2 class="text-center mb-4">호스트 정보 수정</h2>

        <div class="form-container">
            <c:if test="${not empty host}">
                <form action="${pageContext.request.contextPath}/host/update" method="post">
                    <input type="hidden" name="hostId" value="${host.hostId}">

                    <div class="mb-3">
                        <label for="businessName" class="form-label required-field">사업자명</label>
                        <input type="text" class="form-control" id="businessName" name="businessName" 
                               value="${host.businessName}" required>
                    </div>

                    <div class="mb-3">
                        <label for="businessRegNo" class="form-label required-field">사업자 등록번호</label>
                        <input type="text" class="form-control" id="businessRegNo" name="businessRegNo" 
                               value="${host.businessRegNo}" placeholder="000-00-00000" required>
                        <div class="form-text">하이픈(-)을 포함하여 입력해주세요.</div>
                    </div>


                    <div class="mb-3">
                        <label for="bankAccount" class="form-label required-field">정산 계좌 정보</label>
                        <input type="text" class="form-control" id="bankAccount" name="bankAccount" 
                               value="${host.bankAccount}" placeholder="은행명 계좌번호 (예금주)" required>
                        <div class="form-text">예시: KB국민은행 123456789012 (홍길동)</div>
                    </div>

                    <div class="mb-3">
                        <label for="profileText" class="form-label">호스트 소개</label>
                        <textarea class="form-control" id="profileText" name="profileText" rows="4"
                                  placeholder="호스트로서 자신을 소개해주세요. (선택사항)">${host.profileText}</textarea>
                    </div>

                    <!-- 관리자만 상태 변경 가능 -->
                    <c:if test="${sessionScope.role eq 'ADMIN'}">
                        <div class="mb-3">
                            <label for="hostStatus" class="form-label">호스트 상태</label>
                            <select class="form-select" id="hostStatus" name="hostStatus">
                                <option value="PENDING" ${host.hostStatus eq 'PENDING' ? 'selected' : ''}>승인 대기 중</option>
                                <option value="APPROVED" ${host.hostStatus eq 'APPROVED' ? 'selected' : ''}>승인됨</option>
                                <option value="REJECTED" ${host.hostStatus eq 'REJECTED' ? 'selected' : ''}>거부됨</option>
                            </select>
                        </div>
                    </c:if>

                    <div class="alert alert-info">
                        <p><strong>호스트 정보 수정 안내:</strong></p>
                        <ul>
                            <li>사업자 정보 변경 시 관리자의 재승인이 필요할 수 있습니다.</li>
                            <li>사업자 정보는 실제 등록된 정보와 일치해야 합니다.</li>
                            <li>허위 정보 기재 시 호스트 자격이 박탈될 수 있습니다.</li>
                        </ul>
                    </div>

                    <div class="d-grid gap-2">
                        <button type="submit" class="btn btn-primary">정보 수정</button>
                        <a href="${pageContext.request.contextPath}/host/detail/${host.hostId}" class="btn btn-secondary">취소</a>
                    </div>
                </form>
            </c:if>

            <c:if test="${empty host}">
                <div class="alert alert-warning">
                    호스트 정보를 찾을 수 없습니다.
                </div>
                <div class="d-grid gap-2">
                    <a href="${pageContext.request.contextPath}/host/list" class="btn btn-secondary">호스트 목록으로</a>
                </div>
            </c:if>
        </div>
    </div>

    <jsp:include page="../fragments/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 사업자 등록번호 형식 검증
        document.getElementById('businessRegNo').addEventListener('blur', function() {
            const regex = /^\d{3}-\d{2}-\d{5}$/;
            if (!regex.test(this.value) && this.value !== '') {
                alert('사업자 등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)');
                this.focus();
            }
        });
    </script>
</body>
</html>
