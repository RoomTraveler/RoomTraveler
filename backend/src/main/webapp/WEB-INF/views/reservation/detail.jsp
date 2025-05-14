<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>예약 상세</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        .reservation-card {
            margin-bottom: 20px;
        }
        .status-badge {
            font-size: 0.9rem;
            padding: 0.5rem 0.75rem;
        }
        .price-detail {
            border-top: 1px solid #dee2e6;
            padding-top: 15px;
            margin-top: 15px;
        }
        .total-price {
            font-size: 1.2rem;
            font-weight: bold;
        }
        .review-card {
            margin-top: 20px;
        }
        .star-rating {
            color: #ffc107;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container mt-5 mb-5">
        <c:if test="${not empty message}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>예약 상세</h2>
            <div>
                <a href="${pageContext.request.contextPath}/reservation/my-reservations" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left"></i> 예약 목록으로
                </a>
            </div>
        </div>
        
        <!-- 예약 상태 -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <h4 class="card-title">예약 번호: ${reservation.reservationId}</h4>
                            <div>
                                <c:choose>
                                    <c:when test="${reservation.status == 'PENDING'}">
                                        <span class="badge bg-warning status-badge">대기중</span>
                                    </c:when>
                                    <c:when test="${reservation.status == 'CONFIRMED'}">
                                        <span class="badge bg-success status-badge">확정</span>
                                    </c:when>
                                    <c:when test="${reservation.status == 'CANCELLED'}">
                                        <span class="badge bg-danger status-badge">취소됨</span>
                                    </c:when>
                                    <c:when test="${reservation.status == 'COMPLETED'}">
                                        <span class="badge bg-info status-badge">완료</span>
                                    </c:when>
                                </c:choose>
                                
                                <c:choose>
                                    <c:when test="${reservation.paymentStatus == 'UNPAID'}">
                                        <span class="badge bg-secondary status-badge">미결제</span>
                                    </c:when>
                                    <c:when test="${reservation.paymentStatus == 'PAID'}">
                                        <span class="badge bg-success status-badge">결제완료</span>
                                    </c:when>
                                    <c:when test="${reservation.paymentStatus == 'REFUNDED'}">
                                        <span class="badge bg-warning status-badge">환불됨</span>
                                    </c:when>
                                </c:choose>
                            </div>
                        </div>
                        <p class="text-muted">예약일: <fmt:formatDate value="${reservation.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" /></p>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 예약 정보 -->
        <div class="row mb-4">
            <div class="col-md-8">
                <div class="card reservation-card">
                    <div class="card-body">
                        <h4 class="card-title">예약 정보</h4>
                        <div class="row">
                            <div class="col-md-4">
                                <c:choose>
                                    <c:when test="${not empty room.mainImageUrl}">
                                        <img src="${room.mainImageUrl}" class="img-fluid rounded" alt="${room.name}">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${pageContext.request.contextPath}/resources/images/no-image.jpg" class="img-fluid rounded" alt="이미지 없음">
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="col-md-8">
                                <h5>${room.name}</h5>
                                <p class="text-muted">${accommodation.title}</p>
                                <p><i class="bi bi-geo-alt"></i> ${accommodation.address}</p>
                                <p><i class="bi bi-calendar-check"></i> 체크인: <strong><fmt:formatDate value="${reservation.checkInDate}" pattern="yyyy-MM-dd" /></strong> (${accommodation.checkInTime})</p>
                                <p><i class="bi bi-calendar-x"></i> 체크아웃: <strong><fmt:formatDate value="${reservation.checkOutDate}" pattern="yyyy-MM-dd" /></strong> (${accommodation.checkOutTime})</p>
                                <p><i class="bi bi-people"></i> 인원: <strong>${reservation.guestCount}명</strong></p>
                                <p><i class="bi bi-telephone"></i> 숙소 연락처: ${accommodation.phone}</p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- 예약자 정보 -->
                <div class="card reservation-card">
                    <div class="card-body">
                        <h4 class="card-title">예약자 정보</h4>
                        <p><i class="bi bi-person"></i> 이름: ${reservation.userName}</p>
                        <p><i class="bi bi-envelope"></i> 이메일: ${reservation.userEmail}</p>
                        <p><i class="bi bi-telephone"></i> 전화번호: ${reservation.userPhone}</p>
                    </div>
                </div>
                
                <!-- 요청 사항 -->
                <c:if test="${not empty reservation.specialRequests}">
                    <div class="card reservation-card">
                        <div class="card-body">
                            <h4 class="card-title">요청 사항</h4>
                            <p>${reservation.specialRequests}</p>
                        </div>
                    </div>
                </c:if>
                
                <!-- 리뷰 -->
                <c:choose>
                    <c:when test="${not empty review}">
                        <div class="card review-card">
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-center mb-3">
                                    <h4 class="card-title">내 리뷰</h4>
                                    <div>
                                        <c:forEach var="i" begin="1" end="5">
                                            <c:choose>
                                                <c:when test="${i <= review.rating}">
                                                    <i class="bi bi-star-fill star-rating"></i>
                                                </c:when>
                                                <c:otherwise>
                                                    <i class="bi bi-star star-rating"></i>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:forEach>
                                        <span class="ms-2">${review.rating}/5</span>
                                    </div>
                                </div>
                                <p>${review.comment}</p>
                                <p class="text-muted">작성일: <fmt:formatDate value="${review.createdAt}" pattern="yyyy-MM-dd" /></p>
                                <div class="d-flex justify-content-end">
                                    <a href="${pageContext.request.contextPath}/reservation/update-review-form/${review.reviewId}" class="btn btn-outline-primary me-2">수정</a>
                                    <a href="${pageContext.request.contextPath}/reservation/delete-review/${review.reviewId}" class="btn btn-outline-danger" onclick="return confirm('정말 삭제하시겠습니까?')">삭제</a>
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:when test="${reservation.status == 'COMPLETED' && empty review}">
                        <div class="card review-card">
                            <div class="card-body">
                                <h4 class="card-title">리뷰 작성</h4>
                                <p>숙박은 어떠셨나요? 다른 사용자들에게 도움이 될 수 있는 리뷰를 작성해주세요.</p>
                                <div class="d-grid">
                                    <a href="${pageContext.request.contextPath}/reservation/review-form/${reservation.reservationId}" class="btn btn-primary">리뷰 작성하기</a>
                                </div>
                            </div>
                        </div>
                    </c:when>
                </c:choose>
            </div>
            
            <div class="col-md-4">
                <!-- 가격 정보 -->
                <div class="card mb-4">
                    <div class="card-body">
                        <h4 class="card-title">가격 정보</h4>
                        <p><i class="bi bi-currency-dollar"></i> 1박 요금: <fmt:formatNumber value="${room.price}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                        <p><i class="bi bi-calendar-week"></i> 숙박 일수: ${reservation.nights}박</p>
                        <div class="price-detail">
                            <p>객실 요금: <fmt:formatNumber value="${room.price * reservation.nights}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                            <p>세금 및 수수료: <fmt:formatNumber value="${room.price * reservation.nights * 0.1}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                            <p class="total-price">총 요금: <fmt:formatNumber value="${reservation.totalPrice}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></p>
                        </div>
                    </div>
                </div>
                
                <!-- 예약 관리 -->
                <div class="card mb-4">
                    <div class="card-body">
                        <h4 class="card-title">예약 관리</h4>
                        
                        <!-- 결제 상태에 따른 버튼 -->
                        <c:if test="${reservation.paymentStatus == 'UNPAID' && reservation.status != 'CANCELLED'}">
                            <form action="${pageContext.request.contextPath}/reservation/update-payment/${reservation.reservationId}" method="post" class="mb-3">
                                <input type="hidden" name="paymentStatus" value="PAID">
                                <div class="d-grid">
                                    <button type="submit" class="btn btn-success">결제하기</button>
                                </div>
                            </form>
                        </c:if>
                        
                        <!-- 예약 상태에 따른 버튼 -->
                        <c:if test="${reservation.status == 'PENDING' || reservation.status == 'CONFIRMED'}">
                            <div class="d-grid mb-3">
                                <a href="${pageContext.request.contextPath}/reservation/cancel/${reservation.reservationId}" class="btn btn-danger" onclick="return confirm('정말 취소하시겠습니까? 환불 정책에 따라 수수료가 부과될 수 있습니다.')">예약 취소</a>
                            </div>
                        </c:if>
                        
                        <!-- 숙소 상세 페이지 링크 -->
                        <div class="d-grid">
                            <a href="${pageContext.request.contextPath}/accommodation/detail/${accommodation.accommodationId}" class="btn btn-outline-primary">숙소 상세 보기</a>
                        </div>
                    </div>
                </div>
                
                <!-- 환불 정책 -->
                <div class="card">
                    <div class="card-body">
                        <h4 class="card-title">환불 정책</h4>
                        <ul class="list-unstyled">
                            <li><i class="bi bi-check-circle text-success"></i> 체크인 7일 전 취소: 100% 환불</li>
                            <li><i class="bi bi-check-circle text-success"></i> 체크인 5일 전 취소: 70% 환불</li>
                            <li><i class="bi bi-check-circle text-warning"></i> 체크인 3일 전 취소: 50% 환불</li>
                            <li><i class="bi bi-x-circle text-danger"></i> 체크인 1일 전 취소: 환불 불가</li>
                            <li><i class="bi bi-x-circle text-danger"></i> 노쇼(No-show): 환불 불가</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>