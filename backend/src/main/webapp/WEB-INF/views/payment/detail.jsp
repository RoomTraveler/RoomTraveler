<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>결제 상세 정보</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        .payment-detail-container {
            max-width: 800px;
            margin: 0 auto;
        }
        .detail-card {
            border-radius: 10px;
            overflow: hidden;
            margin-bottom: 20px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        .detail-card-header {
            background-color: #f8f9fa;
            padding: 15px 20px;
            border-bottom: 1px solid #dee2e6;
        }
        .detail-card-body {
            padding: 20px;
        }
        .detail-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 15px;
            padding-bottom: 15px;
            border-bottom: 1px solid #eee;
        }
        .detail-row:last-child {
            border-bottom: none;
            margin-bottom: 0;
            padding-bottom: 0;
        }
        .detail-label {
            font-weight: 500;
            color: #6c757d;
        }
        .detail-value {
            font-weight: 600;
            text-align: right;
        }
        .total-amount {
            font-size: 1.5rem;
            font-weight: bold;
            color: #dc3545;
        }
        .payment-status {
            font-size: 1.2rem;
            font-weight: bold;
        }
        .payment-status.completed {
            color: #198754;
        }
        .payment-status.pending {
            color: #ffc107;
        }
        .payment-status.cancelled {
            color: #6c757d;
        }
        .payment-status.failed {
            color: #dc3545;
        }
        .action-buttons {
            margin-top: 30px;
        }
        .timeline {
            position: relative;
            padding-left: 30px;
            margin-top: 20px;
        }
        .timeline-item {
            position: relative;
            padding-bottom: 20px;
        }
        .timeline-item:last-child {
            padding-bottom: 0;
        }
        .timeline-item:before {
            content: '';
            position: absolute;
            left: -30px;
            top: 0;
            width: 20px;
            height: 20px;
            border-radius: 50%;
            background-color: #0d6efd;
            z-index: 1;
        }
        .timeline-item:after {
            content: '';
            position: absolute;
            left: -21px;
            top: 20px;
            width: 2px;
            height: calc(100% - 20px);
            background-color: #dee2e6;
        }
        .timeline-item:last-child:after {
            display: none;
        }
        .timeline-item.cancelled:before {
            background-color: #6c757d;
        }
        .timeline-item.failed:before {
            background-color: #dc3545;
        }
        .timeline-date {
            font-size: 0.8rem;
            color: #6c757d;
            margin-bottom: 5px;
        }
        .timeline-title {
            font-weight: 600;
            margin-bottom: 5px;
        }
        .timeline-content {
            color: #6c757d;
        }
        .qr-code {
            text-align: center;
            margin-top: 20px;
        }
        .qr-code img {
            max-width: 150px;
            margin-bottom: 10px;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container mt-5 mb-5">
        <div class="payment-detail-container">
            <h2 class="mb-4">결제 상세 정보</h2>
            
            <div class="detail-card">
                <div class="detail-card-header d-flex justify-content-between align-items-center">
                    <h4 class="mb-0">결제 정보</h4>
                    <div>
                        <c:choose>
                            <c:when test="${payment.status eq 'COMPLETED'}">
                                <span class="payment-status completed">결제 완료</span>
                            </c:when>
                            <c:when test="${payment.status eq 'PENDING'}">
                                <span class="payment-status pending">처리 중</span>
                            </c:when>
                            <c:when test="${payment.status eq 'CANCELLED'}">
                                <span class="payment-status cancelled">결제 취소</span>
                            </c:when>
                            <c:when test="${payment.status eq 'FAILED'}">
                                <span class="payment-status failed">결제 실패</span>
                            </c:when>
                            <c:otherwise>
                                <span class="payment-status">${payment.status}</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="detail-card-body">
                    <div class="detail-row">
                        <div class="detail-label">결제 번호</div>
                        <div class="detail-value">${payment.paymentId}</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">결제 키</div>
                        <div class="detail-value">${payment.paymentKey}</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">결제 방법</div>
                        <div class="detail-value">
                            <c:choose>
                                <c:when test="${payment.paymentMethod eq 'CARD'}">신용카드</c:when>
                                <c:when test="${payment.paymentMethod eq 'BANK_TRANSFER'}">계좌이체</c:when>
                                <c:when test="${payment.paymentMethod eq 'PHONE'}">휴대폰 결제</c:when>
                                <c:otherwise>${payment.paymentMethod}</c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <c:if test="${not empty payment.cardInfo}">
                        <div class="detail-row">
                            <div class="detail-label">카드 정보</div>
                            <div class="detail-value">${payment.cardInfo}</div>
                        </div>
                    </c:if>
                    <c:if test="${not empty payment.bankInfo}">
                        <div class="detail-row">
                            <div class="detail-label">계좌 정보</div>
                            <div class="detail-value">${payment.bankInfo}</div>
                        </div>
                    </c:if>
                    <c:if test="${not empty payment.phoneInfo}">
                        <div class="detail-row">
                            <div class="detail-label">휴대폰 정보</div>
                            <div class="detail-value">${payment.phoneInfo}</div>
                        </div>
                    </c:if>
                    <div class="detail-row">
                        <div class="detail-label">결제 일시</div>
                        <div class="detail-value"><fmt:formatDate value="${payment.paidAt}" pattern="yyyy-MM-dd HH:mm:ss" /></div>
                    </div>
                    <c:if test="${not empty payment.cancelledAt}">
                        <div class="detail-row">
                            <div class="detail-label">취소 일시</div>
                            <div class="detail-value"><fmt:formatDate value="${payment.cancelledAt}" pattern="yyyy-MM-dd HH:mm:ss" /></div>
                        </div>
                    </c:if>
                    <c:if test="${not empty payment.cancelReason}">
                        <div class="detail-row">
                            <div class="detail-label">취소 사유</div>
                            <div class="detail-value">${payment.cancelReason}</div>
                        </div>
                    </c:if>
                    <c:if test="${not empty payment.failReason}">
                        <div class="detail-row">
                            <div class="detail-label">실패 사유</div>
                            <div class="detail-value">${payment.failReason}</div>
                        </div>
                    </c:if>
                    <div class="detail-row">
                        <div class="detail-label">결제 금액</div>
                        <div class="detail-value total-amount"><fmt:formatNumber value="${payment.amount}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></div>
                    </div>
                </div>
            </div>
            
            <div class="detail-card">
                <div class="detail-card-header">
                    <h4 class="mb-0">예약 정보</h4>
                </div>
                <div class="detail-card-body">
                    <div class="detail-row">
                        <div class="detail-label">예약 번호</div>
                        <div class="detail-value">${payment.reservationId}</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">숙소</div>
                        <div class="detail-value">${payment.accommodationTitle}</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">객실</div>
                        <div class="detail-value">${payment.roomName}</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">체크인</div>
                        <div class="detail-value"><fmt:formatDate value="${reservation.checkInDate}" pattern="yyyy-MM-dd" /></div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">체크아웃</div>
                        <div class="detail-value"><fmt:formatDate value="${reservation.checkOutDate}" pattern="yyyy-MM-dd" /></div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">인원</div>
                        <div class="detail-value">${reservation.guestCount}명</div>
                    </div>
                </div>
            </div>
            
            <div class="detail-card">
                <div class="detail-card-header">
                    <h4 class="mb-0">결제 내역</h4>
                </div>
                <div class="detail-card-body">
                    <div class="timeline">
                        <div class="timeline-item">
                            <div class="timeline-date"><fmt:formatDate value="${payment.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" /></div>
                            <div class="timeline-title">결제 요청</div>
                            <div class="timeline-content">결제가 요청되었습니다.</div>
                        </div>
                        
                        <c:if test="${not empty payment.paidAt}">
                            <div class="timeline-item">
                                <div class="timeline-date"><fmt:formatDate value="${payment.paidAt}" pattern="yyyy-MM-dd HH:mm:ss" /></div>
                                <div class="timeline-title">결제 완료</div>
                                <div class="timeline-content">
                                    <fmt:formatNumber value="${payment.amount}" type="currency" currencySymbol="₩" maxFractionDigits="0"/>이 
                                    <c:choose>
                                        <c:when test="${payment.paymentMethod eq 'CARD'}">신용카드</c:when>
                                        <c:when test="${payment.paymentMethod eq 'BANK_TRANSFER'}">계좌이체</c:when>
                                        <c:when test="${payment.paymentMethod eq 'PHONE'}">휴대폰 결제</c:when>
                                        <c:otherwise>${payment.paymentMethod}</c:otherwise>
                                    </c:choose>
                                    로 결제되었습니다.
                                </div>
                            </div>
                        </c:if>
                        
                        <c:if test="${payment.status eq 'FAILED' and not empty payment.failReason}">
                            <div class="timeline-item failed">
                                <div class="timeline-date"><fmt:formatDate value="${payment.updatedAt}" pattern="yyyy-MM-dd HH:mm:ss" /></div>
                                <div class="timeline-title">결제 실패</div>
                                <div class="timeline-content">사유: ${payment.failReason}</div>
                            </div>
                        </c:if>
                        
                        <c:if test="${payment.status eq 'CANCELLED' and not empty payment.cancelledAt}">
                            <div class="timeline-item cancelled">
                                <div class="timeline-date"><fmt:formatDate value="${payment.cancelledAt}" pattern="yyyy-MM-dd HH:mm:ss" /></div>
                                <div class="timeline-title">결제 취소</div>
                                <div class="timeline-content">
                                    <c:if test="${not empty payment.cancelReason}">
                                        사유: ${payment.cancelReason}
                                    </c:if>
                                </div>
                            </div>
                        </c:if>
                    </div>
                    
                    <c:if test="${payment.status eq 'COMPLETED'}">
                        <div class="qr-code">
                            <img src="https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=PAYMENT_${payment.paymentId}" alt="결제 QR 코드">
                            <p class="text-muted">이 QR 코드를 체크인 시 제시해주세요.</p>
                        </div>
                    </c:if>
                </div>
            </div>
            
            <div class="action-buttons d-flex justify-content-between">
                <div>
                    <a href="${pageContext.request.contextPath}/payment/history" class="btn btn-outline-secondary">
                        <i class="bi bi-arrow-left"></i> 결제 내역으로 돌아가기
                    </a>
                </div>
                <div>
                    <c:if test="${payment.status eq 'COMPLETED'}">
                        <a href="${pageContext.request.contextPath}/payment/cancel/${payment.paymentId}" class="btn btn-outline-danger me-2">
                            <i class="bi bi-x-circle"></i> 결제 취소
                        </a>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/reservation/detail/${payment.reservationId}" class="btn btn-primary">
                        <i class="bi bi-calendar-check"></i> 예약 상세 보기
                    </a>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>