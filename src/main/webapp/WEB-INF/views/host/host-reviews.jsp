<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>호스트 리뷰 관리</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.3/font/bootstrap-icons.css">
    <style>
        /* 리뷰 카드 스타일 */
        .review-card {
            margin-bottom: 20px;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease;
        }
        .review-card:hover {
            transform: translateY(-5px);
        }

        /* 별점 스타일 */
        .stars {
            color: #ffc107;
            font-size: 1.2rem;
        }

        /* 필터 카드 스타일 */
        .filter-card {
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <!-- 헤더 포함 -->
    <jsp:include page="../fragments/header.jsp" />

    <div class="container mt-5 mb-5">
        <h1 class="mb-4">
            <i class="bi bi-star"></i> 호스트 리뷰 관리
        </h1>

        <!-- 알림 메시지 표시 -->
        <c:if test="${not empty message}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <!-- 필터링 옵션 -->
        <div class="card filter-card">
            <div class="card-body">
                <h5 class="card-title">리뷰 필터링</h5>
                <form action="/host/reviews" method="get" class="row g-3">
                    <div class="col-md-4">
                        <label for="accommodationId" class="form-label">숙소 선택</label>
                        <select class="form-select" id="accommodationId" name="accommodationId">
                            <option value="">모든 숙소</option>
                            <c:forEach var="accommodation" items="${accommodations}">
                                <option value="${accommodation.accommodationId}">${accommodation.title}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label for="rating" class="form-label">별점</label>
                        <select class="form-select" id="rating" name="rating">
                            <option value="">모든 별점</option>
                            <option value="5">5점</option>
                            <option value="4">4점</option>
                            <option value="3">3점</option>
                            <option value="2">2점</option>
                            <option value="1">1점</option>
                        </select>
                    </div>
                    <div class="col-md-4 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary">필터 적용</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- 리뷰 통계 -->
        <div class="row mb-4">
            <div class="col-md-6">
                <div class="card text-center">
                    <div class="card-body">
                        <h5 class="card-title">총 리뷰</h5>
                        <p class="card-text fs-2">${totalReviews}</p>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card text-center">
                    <div class="card-body">
                        <h5 class="card-title">평균 별점</h5>
                        <p class="card-text fs-2">
                            <span class="stars">
                                <c:forEach begin="1" end="5" var="i">
                                    <i class="bi ${i <= averageRating ? 'bi-star-fill' : (i <= averageRating + 0.5 ? 'bi-star-half' : 'bi-star')}"></i>
                                </c:forEach>
                            </span>
                            <span class="ms-2">${averageRating}</span>
                        </p>
                    </div>
                </div>
            </div>
        </div>

        <!-- 리뷰 목록 -->
        <c:choose>
            <c:when test="${empty reviews}">
                <div class="alert alert-info">
                    <i class="bi bi-info-circle"></i> 리뷰가 없습니다.
                </div>
            </c:when>
            <c:otherwise>
                <div class="row">
                    <c:forEach var="review" items="${reviews}">
                        <div class="col-md-6">
                            <div class="card review-card">
                                <div class="card-header d-flex justify-content-between align-items-center">
                                    <div>
                                        <h5 class="mb-0">${review.accommodationTitle}</h5>
                                    </div>
                                    <div class="stars">
                                        <c:forEach begin="1" end="5" var="i">
                                            <i class="bi ${i <= review.rating ? 'bi-star-fill' : 'bi-star'}"></i>
                                        </c:forEach>
                                    </div>
                                </div>
                                <div class="card-body">
                                    <div class="d-flex justify-content-between mb-3">
                                        <div>
                                            <strong>${review.username}</strong>
                                            <small class="text-muted ms-2">
                                                <fmt:formatDate value="${review.createdAt}" pattern="yyyy-MM-dd" />
                                            </small>
                                        </div>
                                    </div>

                                    <c:if test="${not empty review.title}">
                                        <h6 class="card-subtitle mb-2">${review.title}</h6>
                                    </c:if>

                                    <p class="card-text">${review.content}</p>

                                    <div class="mt-3">
                                        <a href="/accommodation/detail?accommodationId=${review.accommodationId}" class="btn btn-sm btn-outline-primary">
                                            <i class="bi bi-building"></i> 숙소 보기
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- 푸터 포함 -->
    <jsp:include page="../fragments/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 페이지 로드 시 URL 파라미터에 따라 필터 선택 상태 설정
        document.addEventListener('DOMContentLoaded', function() {
            const urlParams = new URLSearchParams(window.location.search);
            const accommodationId = urlParams.get('accommodationId');
            const rating = urlParams.get('rating');

            if (accommodationId) {
                document.getElementById('accommodationId').value = accommodationId;
            }

            if (rating) {
                document.getElementById('rating').value = rating;
            }
        });
    </script>
</body>
</html>
