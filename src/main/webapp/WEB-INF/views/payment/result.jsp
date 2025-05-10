<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>결제 완료</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        .payment-result-container {
            max-width: 700px;
            margin: 0 auto;
        }
        .payment-success-icon {
            font-size: 5rem;
            color: #198754;
            margin-bottom: 20px;
        }
        .payment-info-card {
            background-color: #f8f9fa;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 20px;
        }
        .payment-detail-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 10px;
            padding-bottom: 10px;
            border-bottom: 1px solid #dee2e6;
        }
        .payment-detail-row:last-child {
            border-bottom: none;
        }
        .total-amount {
            font-size: 1.5rem;
            font-weight: bold;
            color: #dc3545;
        }
        .action-buttons {
            margin-top: 30px;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container mt-5 mb-5">
        <div class="payment-result-container text-center">
            <c:if test="${not empty message}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>
            
            <div class="payment-success-icon">
                <i class="bi bi-check-circle-fill"></i>
            </div>
            <h2 class="mb-4">결제가 완료되었습니다</h2>
            <p class="lead mb-5">예약이 확정되었습니다. 아래 결제 정보를 확인해주세요.</p>
            
            <div class="payment-info-card text-start">
                <h4 class="mb-4">결제 정보</h4>
                <div class="payment-detail-row">
                    <span>결제 번호</span>
                    <span>${payment.paymentId}</span>
                </div>
                <div class="payment-detail-row">
                    <span>결제 일시</span>
                    <span><fmt:formatDate value="${payment.paidAt}" pattern="yyyy-MM-dd HH:mm:ss" /></span>
                </div>
                <div class="payment-detail-row">
                    <span>결제 방법</span>
                    <span>
                        <c:choose>
                            <c:when test="${payment.paymentMethod eq 'CARD'}">신용카드</c:when>
                            <c:when test="${payment.paymentMethod eq 'BANK_TRANSFER'}">계좌이체</c:when>
                            <c:when test="${payment.paymentMethod eq 'PHONE'}">휴대폰 결제</c:when>
                            <c:otherwise>${payment.paymentMethod}</c:otherwise>
                        </c:choose>
                    </span>
                </div>
                <div class="payment-detail-row">
                    <span>결제 상태</span>
                    <span>
                        <c:choose>
                            <c:when test="${payment.status eq 'COMPLETED'}">
                                <span class="badge bg-success">결제 완료</span>
                            </c:when>
                            <c:when test="${payment.status eq 'PENDING'}">
                                <span class="badge bg-warning text-dark">처리 중</span>
                            </c:when>
                            <c:when test="${payment.status eq 'FAILED'}">
                                <span class="badge bg-danger">결제 실패</span>
                            </c:when>
                            <c:when test="${payment.status eq 'CANCELLED'}">
                                <span class="badge bg-secondary">결제 취소</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-info">${payment.status}</span>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
                <div class="payment-detail-row">
                    <span>결제 금액</span>
                    <span class="total-amount"><fmt:formatNumber value="${payment.amount}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></span>
                </div>
            </div>
            
            <div class="payment-info-card text-start">
                <h4 class="mb-4">예약 정보</h4>
                <div class="payment-detail-row">
                    <span>예약 번호</span>
                    <span>${payment.reservationId}</span>
                </div>
                <div class="payment-detail-row">
                    <span>숙소</span>
                    <span>${payment.accommodationTitle}</span>
                </div>
                <div class="payment-detail-row">
                    <span>객실</span>
                    <span>${payment.roomName}</span>
                </div>
                <div class="payment-detail-row">
                    <span>체크인</span>
                    <span>${reservation.checkInDate}</span>
                </div>
                <div class="payment-detail-row">
                    <span>체크아웃</span>
                    <span>${reservation.checkOutDate}</span>
                </div>
                <div class="payment-detail-row">
                    <span>인원</span>
                    <span>${reservation.guestCount}명</span>
                </div>
            </div>
            
            <div class="action-buttons">
                <a href="${pageContext.request.contextPath}/reservation/detail/${payment.reservationId}" class="btn btn-primary">예약 상세 보기</a>
                <a href="${pageContext.request.contextPath}/payment/history" class="btn btn-outline-secondary">결제 내역 보기</a>
                <a href="${pageContext.request.contextPath}/" class="btn btn-outline-dark">홈으로</a>
            </div>
            
            <div class="mt-5">
                <div class="alert alert-info" role="alert">
                    <h5 class="alert-heading"><i class="bi bi-info-circle"></i> 알림</h5>
                    <p>결제 내역은 이메일(<strong>${sessionScope.email}</strong>)로도 발송되었습니다.</p>
                    <p>예약 확정 및 취소 관련 문의는 고객센터(1234-5678)로 연락해주세요.</p>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 결제 완료 페이지 로드 시 실행
        document.addEventListener('DOMContentLoaded', function() {
            // 결제 완료 이벤트 추적 (실제 구현에서는 분석 도구와 연동)
            console.log('Payment completed: ${payment.paymentId}');
            
            // 5초 후 자동으로 알림 닫기
            setTimeout(function() {
                const alert = document.querySelector('.alert-dismissible');
                if (alert) {
                    const bsAlert = new bootstrap.Alert(alert);
                    bsAlert.close();
                }
            }, 5000);
        });
    </script>
</body>
</html>