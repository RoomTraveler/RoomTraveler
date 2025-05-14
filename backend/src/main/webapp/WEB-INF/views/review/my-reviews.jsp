<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>내 리뷰 목록</title>
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
        
        .accommodation-link {
            color: #0d6efd;
            text-decoration: none;
            font-weight: bold;
        }
        
        .accommodation-link:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="container mt-5">
        <!-- 헤더 포함 -->
        <jsp:include page="../fragments/header.jsp" />
        
        <div class="row mb-4">
            <div class="col">
                <h2>내 리뷰 목록</h2>
                <p class="text-muted">
                    내가 작성한 리뷰 ${reviews.size()}개
                </p>
            </div>
        </div>
        
        <!-- 리뷰가 없는 경우 -->
        <c:if test="${empty reviews}">
            <div class="alert alert-info">
                작성한 리뷰가 없습니다. 숙소를 이용한 후 리뷰를 작성해보세요!
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
                                <a href="${root}/accommodation/detail?accommodationId=${review.accommodationId}" class="accommodation-link">
                                    <i class="bi bi-building"></i> ${review.accommodationTitle}
                                </a>
                                <span class="ms-3"><i class="bi bi-calendar3"></i> 
                                    <fmt:formatDate value="${review.stayDate}" pattern="yyyy-MM-dd" />
                                </span>
                                <span class="ms-3"><i class="bi bi-clock"></i> 
                                    <fmt:formatDate value="${review.createdAt}" pattern="yyyy-MM-dd" />
                                </span>
                            </div>
                            <p class="review-content">${review.content}</p>
                            
                            <div class="d-flex justify-content-end">
                                <a href="${root}/review/edit/${review.reviewId}" class="btn btn-sm btn-outline-primary me-2">수정</a>
                                <a href="${root}/review/delete/${review.reviewId}" 
                                   class="btn btn-sm btn-outline-danger"
                                   onclick="return confirm('정말 삭제하시겠습니까?');">삭제</a>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</body>
</html>