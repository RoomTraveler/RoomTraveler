<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>호스트 숙소 관리</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.3/font/bootstrap-icons.css">
    <style>
        /* 숙소 카드 스타일 */
        .accommodation-card {
            margin-bottom: 20px;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease;
            height: 100%;
        }
        .accommodation-card:hover {
            transform: translateY(-5px);
        }

        /* 객실 카드 스타일 */
        .room-card {
            margin-bottom: 15px;
            border-radius: 8px;
            border: 1px solid #dee2e6;
        }

        /* 이미지 스타일 */
        .card-img-top {
            height: 200px;
            object-fit: cover;
            border-top-left-radius: 10px;
            border-top-right-radius: 10px;
        }

        /* 상태 배지 스타일 */
        .status-badge {
            font-size: 0.8rem;
            padding: 5px 10px;
            border-radius: 20px;
        }
        .status-active {
            background-color: #198754;
            color: white;
        }
        .status-inactive {
            background-color: #dc3545;
            color: white;
        }
        .status-pending {
            background-color: #ffc107;
            color: #212529;
        }

        /* 통계 카드 스타일 */
        .stats-card {
            text-align: center;
            margin-bottom: 20px;
        }

        /* 추가 버튼 스타일 */
        .add-btn {
            position: fixed;
            bottom: 30px;
            right: 30px;
            width: 60px;
            height: 60px;
            border-radius: 50%;
            background-color: #0d6efd;
            color: white;
            display: flex;
            justify-content: center;
            align-items: center;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
            z-index: 1000;
            font-size: 1.5rem;
            text-decoration: none;
        }
        .add-btn:hover {
            background-color: #0b5ed7;
            color: white;
        }
    </style>
</head>
<body>
    <!-- 헤더 포함 -->
    <jsp:include page="../fragments/header.jsp" />

    <div class="container mt-5 mb-5">
        <h1 class="mb-4">
            <i class="bi bi-houses"></i> 호스트 숙소 관리
        </h1>

        <!-- 알림 메시지 표시 -->
        <c:if test="${not empty message}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <!-- 숙소 통계 -->
        <div class="row mb-4">
            <div class="col-md-3">
                <div class="card stats-card">
                    <div class="card-body">
                        <h5 class="card-title">총 숙소</h5>
                        <p class="card-text fs-2">${accommodations.size()}</p>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card stats-card">
                    <div class="card-body">
                        <h5 class="card-title">총 객실</h5>
                        <p class="card-text fs-2">${totalRooms}</p>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card stats-card">
                    <div class="card-body">
                        <h5 class="card-title">활성 숙소</h5>
                        <p class="card-text fs-2">${activeAccommodations}</p>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card stats-card">
                    <div class="card-body">
                        <h5 class="card-title">평균 평점</h5>
                        <p class="card-text fs-2">
                            <span class="text-warning">
                                <i class="bi bi-star-fill"></i>
                            </span>
                            <span>${averageRating}</span>
                        </p>
                    </div>
                </div>
            </div>
        </div>

        <!-- 숙소 목록 -->
        <c:choose>
            <c:when test="${empty accommodations}">
                <div class="alert alert-info">
                    <i class="bi bi-info-circle"></i> 등록된 숙소가 없습니다. 아래 + 버튼을 클릭하여 새 숙소를 등록하세요.
                </div>
            </c:when>
            <c:otherwise>
                <div class="row">
                    <c:forEach var="accommodation" items="${accommodations}">
                        <div class="col-md-6 col-lg-4 mb-4">
                            <div class="card accommodation-card">
                                <c:choose>
                                    <c:when test="${not empty accommodation.mainImageUrl}">
                                        <img src="${accommodation.mainImageUrl}" class="card-img-top" alt="${accommodation.title}">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="/img/default-accommodation.jpg" class="card-img-top" alt="기본 이미지">
                                    </c:otherwise>
                                </c:choose>
                                <div class="card-header d-flex justify-content-between align-items-center">
                                    <h5 class="card-title mb-0">${accommodation.title}</h5>
                                    <span class="status-badge status-${accommodation.status.toLowerCase()}">${accommodation.status}</span>
                                </div>
                                <div class="card-body">
                                    <p class="card-text">
                                        <i class="bi bi-geo-alt"></i> ${accommodation.address}
                                    </p>
                                    <p class="card-text">
                                        <i class="bi bi-telephone"></i> ${accommodation.phone}
                                    </p>
                                    <!-- 객실 정보 -->
                                    <div class="mt-3">
                                        <h6>객실</h6>
                                        <c:set var="rooms" value="${roomsByAccommodation[accommodation.accommodationId]}" />
                                        <c:choose>
                                            <c:when test="${empty rooms}">
                                                <p class="text-muted">등록된 객실이 없습니다.</p>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach var="room" items="${rooms}" varStatus="status">
                                                    <div class="room-card p-2 ${status.last ? '' : 'mb-2'}">
                                                        <div class="d-flex justify-content-between align-items-center">
                                                            <div>
                                                                <strong>${room.name}</strong>
                                                                <span class="ms-2 text-muted">
                                                                    <fmt:formatNumber value="${room.price}" type="currency" currencySymbol="₩" />
                                                                </span>
                                                            </div>
                                                            <div>
                                                                <a href="${pageContext.request.contextPath}/accommodation/update-room-form?roomId=${room.roomId}" class="btn btn-sm btn-outline-primary">
                                                                    <i class="bi bi-pencil"></i>
                                                                </a>
                                                                <button class="btn btn-sm btn-outline-danger" 
                                                                        onclick="if(confirm('정말로 이 객실을 삭제하시겠습니까?')) location.href='${pageContext.request.contextPath}/accommodation/delete-room?roomId=${room.roomId}'">
                                                                    <i class="bi bi-trash"></i>
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>

                                        <!-- 객실 추가 버튼 -->
                                        <a href="${pageContext.request.contextPath}/accommodation/register-room-form?accommodationId=${accommodation.accommodationId}" class="btn btn-sm btn-outline-success w-100 mt-2">
                                            <i class="bi bi-plus-circle"></i> 객실 추가
                                        </a>
                                    </div>
                                </div>
                                <div class="card-footer">
                                    <div class="d-flex justify-content-between">
                                        <a href="${pageContext.request.contextPath}/accommodation/detail?accommodationId=${accommodation.accommodationId}" class="btn btn-sm btn-info">
                                            <i class="bi bi-eye"></i> 보기
                                        </a>
                                        <div>
                                            <a href="${pageContext.request.contextPath}/accommodation/update-form?accommodationId=${accommodation.accommodationId}" class="btn btn-sm btn-primary">
                                                <i class="bi bi-pencil"></i> 수정
                                            </a>
                                            <button class="btn btn-sm btn-danger" 
                                                    onclick="if(confirm('정말로 이 숙소를 삭제하시겠습니까? 모든 객실 정보도 함께 삭제됩니다.')) location.href='${pageContext.request.contextPath}/accommodation/delete?accommodationId=${accommodation.accommodationId}'">
                                                <i class="bi bi-trash"></i> 삭제
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>

        <!-- 숙소 추가 버튼 -->
        <a href="${pageContext.request.contextPath}/accommodation/register-form" class="add-btn">
            <i class="bi bi-plus-lg"></i>
        </a>
    </div>

    <!-- 푸터 포함 -->
    <jsp:include page="../fragments/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
