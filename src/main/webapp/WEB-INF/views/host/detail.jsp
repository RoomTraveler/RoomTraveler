<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>호스트 상세 정보</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .detail-container {
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f8f9fa;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        .info-group {
            margin-bottom: 1.5rem;
            border-bottom: 1px solid #dee2e6;
            padding-bottom: 1rem;
        }
        .info-group:last-child {
            border-bottom: none;
        }
        .info-label {
            font-weight: bold;
            color: #495057;
        }
        .status-badge {
            font-size: 0.9rem;
            padding: 0.5rem 0.75rem;
            border-radius: 50px;
        }
        .status-pending {
            background-color: #ffc107;
            color: #212529;
        }
        .status-approved {
            background-color: #28a745;
            color: white;
        }
        .status-rejected {
            background-color: #dc3545;
            color: white;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />

    <div class="container mt-5 mb-5">
        <h2 class="text-center mb-4">호스트 상세 정보</h2>

        <div class="detail-container">
            <c:if test="${not empty host}">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h3>${host.businessName}</h3>
                    <c:choose>
                        <c:when test="${host.hostStatus eq 'PENDING'}">
                            <span class="badge status-badge status-pending">승인 대기 중</span>
                        </c:when>
                        <c:when test="${host.hostStatus eq 'APPROVED'}">
                            <span class="badge status-badge status-approved">승인됨</span>
                        </c:when>
                        <c:when test="${host.hostStatus eq 'REJECTED'}">
                            <span class="badge status-badge status-rejected">거부됨</span>
                        </c:when>
                    </c:choose>
                </div>

                <div class="info-group">
                    <h5>사업자 정보</h5>
                    <div class="row mb-2">
                        <div class="col-md-4 info-label">사업자명</div>
                        <div class="col-md-8">${host.businessName}</div>
                    </div>
                    <div class="row mb-2">
                        <div class="col-md-4 info-label">사업자 등록번호</div>
                        <div class="col-md-8">${host.businessRegNo}</div>
                    </div>
                </div>

                <div class="info-group">
                    <h5>계좌 정보</h5>
                    <div class="row mb-2">
                        <div class="col-md-4 info-label">정산 계좌 정보</div>
                        <div class="col-md-8">${host.bankAccount}</div>
                    </div>
                </div>

                <c:if test="${not empty host.profileText}">
                    <div class="info-group">
                        <h5>호스트 소개</h5>
                        <p>${host.profileText}</p>
                    </div>
                </c:if>

                <div class="info-group">
                    <h5>등록 정보</h5>
                    <div class="row mb-2">
                        <div class="col-md-4 info-label">등록일</div>
                        <div class="col-md-8">
                            <fmt:formatDate value="${host.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </div>
                    </div>
                    <c:if test="${not empty host.updatedAt}">
                        <div class="row mb-2">
                            <div class="col-md-4 info-label">최종 수정일</div>
                            <div class="col-md-8">
                                <fmt:formatDate value="${host.updatedAt}" pattern="yyyy-MM-dd HH:mm:ss"/>
                            </div>
                        </div>
                    </c:if>
                </div>

                <!-- 호스트 본인 또는 관리자만 수정 가능 -->
                <c:if test="${sessionScope.userId eq host.hostId || sessionScope.role eq 'ADMIN'}">
                    <div class="d-grid gap-2 d-md-flex justify-content-md-end mt-4">
                        <a href="${pageContext.request.contextPath}/host/update-form/${host.hostId}" class="btn btn-primary">정보 수정</a>

                        <!-- 승인된 호스트만 숙소 등록 가능 -->
                        <c:if test="${host.hostStatus eq 'APPROVED' && (sessionScope.userId eq host.hostId || sessionScope.role eq 'ADMIN')}">
                            <a href="${pageContext.request.contextPath}/host/register-accommodation" class="btn btn-success">숙소 등록</a>
                            <a href="${pageContext.request.contextPath}/accommodation/host/accommodations" class="btn btn-info">내 숙소 관리</a>
                        </c:if>

                        <!-- 관리자만 상태 변경 가능 -->
                        <c:if test="${sessionScope.role eq 'ADMIN' && host.hostStatus eq 'PENDING'}">
                            <form action="${pageContext.request.contextPath}/host/update-status/${host.hostId}" method="post" style="display: inline;">
                                <input type="hidden" name="hostStatus" value="APPROVED">
                                <button type="submit" class="btn btn-success">승인</button>
                            </form>
                            <form action="${pageContext.request.contextPath}/host/update-status/${host.hostId}" method="post" style="display: inline;">
                                <input type="hidden" name="hostStatus" value="REJECTED">
                                <button type="submit" class="btn btn-danger">거부</button>
                            </form>
                        </c:if>
                    </div>
                </c:if>
            </c:if>

            <c:if test="${empty host}">
                <div class="alert alert-warning">
                    호스트 정보를 찾을 수 없습니다.
                </div>
            </c:if>

            <div class="d-grid gap-2 mt-4">
                <a href="${pageContext.request.contextPath}/host/list" class="btn btn-secondary">호스트 목록으로</a>
            </div>
        </div>
    </div>

    <jsp:include page="../fragments/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
