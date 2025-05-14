<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>호스트 목록</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .host-card {
            transition: transform 0.3s;
            margin-bottom: 20px;
            height: 100%;
        }
        .host-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        }
        .filter-section {
            background-color: #f8f9fa;
            padding: 20px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        .status-badge {
            font-size: 0.8rem;
            padding: 0.4rem 0.6rem;
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
        <h2 class="mb-4">호스트 목록</h2>

        <!-- 필터 섹션 (관리자용) -->
        <c:if test="${sessionScope.role eq 'ADMIN'}">
            <div class="filter-section mb-4">
                <form action="${pageContext.request.contextPath}/host/list" method="get" class="row g-3">
                    <div class="col-md-4">
                        <label for="status" class="form-label">상태</label>
                        <select class="form-select" id="status" name="status">
                            <option value="">전체</option>
                            <option value="PENDING" ${param.status eq 'PENDING' ? 'selected' : ''}>승인 대기 중</option>
                            <option value="APPROVED" ${param.status eq 'APPROVED' ? 'selected' : ''}>승인됨</option>
                            <option value="REJECTED" ${param.status eq 'REJECTED' ? 'selected' : ''}>거부됨</option>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label for="keyword" class="form-label">검색어</label>
                        <input type="text" class="form-control" id="keyword" name="keyword" 
                               placeholder="사업자명, 사업자 등록번호" value="${param.keyword}">
                    </div>
                    <div class="col-md-4 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary w-100">검색</button>
                    </div>
                </form>
            </div>
        </c:if>

        <!-- 호스트 목록 -->
        <div class="row">
            <c:if test="${empty hosts}">
                <div class="col-12 text-center py-5">
                    <p class="lead">등록된 호스트가 없습니다.</p>
                    <a href="${pageContext.request.contextPath}/host/regist-form" class="btn btn-primary">호스트 등록하기</a>
                </div>
            </c:if>

            <c:forEach var="host" items="${hosts}">
                <div class="col-md-4 mb-4">
                    <div class="card host-card">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <h5 class="card-title">${host.businessName}</h5>
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

                            <p class="card-text"><strong>사업자 등록번호:</strong> ${host.businessRegNo}</p>
                            <p class="card-text"><strong>등록일:</strong> 
                                <fmt:formatDate value="${host.createdAt}" pattern="yyyy-MM-dd"/>
                            </p>

                            <div class="d-grid gap-2">
                                <a href="${pageContext.request.contextPath}/host/detail/${host.hostId}" 
                                   class="btn btn-outline-primary">상세 보기</a>

                                <!-- 관리자만 볼 수 있는 빠른 승인/거부 버튼 -->
                                <c:if test="${sessionScope.role eq 'ADMIN' && host.hostStatus eq 'PENDING'}">
                                    <div class="d-flex justify-content-between mt-2">
                                        <form action="${pageContext.request.contextPath}/host/update-status/${host.hostId}" 
                                              method="post" style="width: 48%;">
                                            <input type="hidden" name="hostStatus" value="APPROVED">
                                            <button type="submit" class="btn btn-success btn-sm w-100">승인</button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/host/update-status/${host.hostId}" 
                                              method="post" style="width: 48%;">
                                            <input type="hidden" name="hostStatus" value="REJECTED">
                                            <button type="submit" class="btn btn-danger btn-sm w-100">거부</button>
                                        </form>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>

        <!-- 호스트 등록 버튼 (일반 사용자용) -->
        <c:if test="${sessionScope.role ne 'ADMIN' && sessionScope.userId ne null}">
            <div class="d-grid gap-2 col-md-6 mx-auto mt-4">
                <a href="${pageContext.request.contextPath}/host/regist-form" class="btn btn-primary">호스트 등록하기</a>
            </div>
        </c:if>
    </div>

    <jsp:include page="../fragments/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
