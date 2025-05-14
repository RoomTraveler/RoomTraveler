<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>리뷰 목록</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <style>
        .star-rating {
            color: #FFD700;
            font-size: 1.2rem;
        }
        
        .review-card {
            margin-bottom: 20px;
            transition: transform 0.3s;
        }
        
        .review-card:hover {
            transform: translateY(-5px);
        }
        
        .review-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .review-meta {
            color: #6c757d;
            font-size: 0.9rem;
        }
        
        .review-content {
            margin-top: 15px;
            white-space: pre-line;
        }
        
        .rating-summary {
            background-color: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 30px;
        }
        
        .progress {
            height: 10px;
        }
        
        .progress-bar {
            background-color: #FFD700;
        }
    </style>
</head>
<body>
    <div class="container mt-5">
        <!-- 헤더 포함 -->
        <jsp:include page="../fragments/header.jsp" />
        
        <div class="row mb-4">
            <div class="col">
                <h2>리뷰 목록</h2>
                <p class="text-muted">
                    <c:if test="${not empty accommodationTitle}">
                        ${accommodationTitle}에 대한 
                    </c:if>
                    총 ${reviewCount}개의 리뷰가 있습니다.
                </p>
            </div>
            <div class="col-auto">
                <a href="${root}/review/write/${accommodationId}" class="btn btn-primary">
                    <i class="bi bi-pencil-square"></i> 리뷰 작성
                </a>
            </div>
        </div>
        
        <!-- 평점 요약 -->
        <div class="rating-summary">
            <div class="row align-items-center">
                <div class="col-md-3 text-center">
                    <h1 class="display-4 fw-bold">
                        <c:choose>
                            <c:when test="${averageRating != null}">
                                <fmt:formatNumber value="${averageRating}" pattern="#.#" />
                            </c:when>
                            <c:otherwise>0.0</c:otherwise>
                        </c:choose>
                    </h1>
                    <div class="star-rating">
                        <c:forEach begin="1" end="5" var="i">
                            <c:choose>
                                <c:when test="${i <= averageRating}">
                                    <i class="bi bi-star-fill"></i>
                                </c:when>
                                <c:when test="${i <= averageRating + 0.5}">
                                    <i class="bi bi-star-half"></i>
                                </c:when>
                                <c:otherwise>
                                    <i class="bi bi-star"></i>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </div>
                    <p class="text-muted">${reviewCount}개 리뷰</p>
                </div>
                <div class="col-md-9">
                    <!-- 평점 분포 -->
                    <c:forEach begin="5" end="1" step="-1" var="rating">
                        <div class="row align-items-center mb-2">
                            <div class="col-2">${rating}점</div>
                            <div class="col-8">
                                <div class="progress">
                                    <c:set var="count" value="${ratingDistribution[rating] != null ? ratingDistribution[rating] : 0}" />
                                    <c:set var="percentage" value="${reviewCount > 0 ? (count / reviewCount) * 100 : 0}" />
                                    <div class="progress-bar" role="progressbar" 
                                         style="width: ${percentage}%;" 
                                         aria-valuenow="${percentage}" 
                                         aria-valuemin="0" 
                                         aria-valuemax="100"></div>
                                </div>
                            </div>
                            <div class="col-2 text-end">${count}개</div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
        
        <!-- 리뷰가 없는 경우 -->
        <c:if test="${empty reviews}">
            <div class="alert alert-info">
                아직 리뷰가 없습니다. 첫 번째 리뷰를 작성해보세요!
            </div>
        </c:if>
        
        <!-- 리뷰 목록 -->
        <div class="row">
            <c:forEach var="review" items="${reviews}">
                <div class="col-md-6">
                    <div class="card review-card">
                        <div class="card-body">
                            <div class="review-header">
                                <h5 class="card-title">${review.title}</h5>
                                <div class="star-rating">
                                    <c:forEach begin="1" end="${review.rating}">
                                        <i class="bi bi-star-fill"></i>
                                    </c:forEach>
                                    <c:forEach begin="${review.rating + 1}" end="5">
                                        <i class="bi bi-star"></i>
                                    </c:forEach>
                                </div>
                            </div>
                            <div class="review-meta">
                                <span><i class="bi bi-person-circle"></i> ${review.username}</span>
                                <span class="ms-3"><i class="bi bi-calendar3"></i> 
                                    <fmt:formatDate value="${review.stayDate}" pattern="yyyy-MM-dd" />
                                </span>
                                <span class="ms-3"><i class="bi bi-clock"></i> 
                                    <fmt:formatDate value="${review.createdAt}" pattern="yyyy-MM-dd" />
                                </span>
                            </div>
                            <p class="review-content">${review.content}</p>
                            
                            <!-- 리뷰 작성자 또는 관리자만 수정/삭제 가능 -->
                            <c:if test="${review.userId == userId || role == 'ADMIN'}">
                                <div class="d-flex justify-content-end">
                                    <a href="${root}/review/edit/${review.reviewId}" class="btn btn-sm btn-outline-primary me-2">수정</a>
                                    <a href="${root}/review/delete/${review.reviewId}" 
                                       class="btn btn-sm btn-outline-danger"
                                       onclick="return confirm('정말 삭제하시겠습니까?');">삭제</a>
                                </div>
                            </c:if>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</body>
</html>