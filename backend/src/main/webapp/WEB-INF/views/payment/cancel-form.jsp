<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>결제 취소</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        .cancel-form-container {
            max-width: 700px;
            margin: 0 auto;
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
        .warning-icon {
            font-size: 3rem;
            color: #dc3545;
            margin-bottom: 20px;
        }
        .refund-policy {
            background-color: #fff3cd;
            border-left: 4px solid #ffc107;
            padding: 15px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container mt-5 mb-5">
        <div class="cancel-form-container">
            <h2 class="mb-4">결제 취소</h2>
            
            <div class="alert alert-warning" role="alert">
                <h5 class="alert-heading"><i class="bi bi-exclamation-triangle"></i> 주의</h5>
                <p>결제 취소 시 예약도 함께 취소됩니다. 이 작업은 되돌릴 수 없습니다.</p>
            </div>
            
            <div class="payment-info-card">
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
            
            <div class="payment-info-card">
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
            </div>
            
            <div class="refund-policy">
                <h5>환불 정책</h5>
                <p>체크인 날짜로부터 남은 기간에 따라 환불 금액이 달라집니다:</p>
                <ul>
                    <li>체크인 7일 이상 전 취소: 100% 환불</li>
                    <li>체크인 5-6일 전 취소: 70% 환불</li>
                    <li>체크인 3-4일 전 취소: 50% 환불</li>
                    <li>체크인 1-2일 전 취소: 환불 불가</li>
                    <li>체크인 당일 취소: 환불 불가</li>
                </ul>
                
                <c:set var="today" value="<%= new java.util.Date() %>" />
                <c:set var="checkInDate" value="${reservation.checkInDate}" />
                <c:set var="daysDiff" value="${(checkInDate.time - today.time) / (1000 * 60 * 60 * 24)}" />
                
                <c:choose>
                    <c:when test="${daysDiff >= 7}">
                        <p class="text-success fw-bold">현재 100% 환불 가능합니다. (체크인까지 ${daysDiff}일 남음)</p>
                        <c:set var="refundAmount" value="${payment.amount}" />
                    </c:when>
                    <c:when test="${daysDiff >= 5}">
                        <p class="text-success fw-bold">현재 70% 환불 가능합니다. (체크인까지 ${daysDiff}일 남음)</p>
                        <c:set var="refundAmount" value="${payment.amount * 0.7}" />
                    </c:when>
                    <c:when test="${daysDiff >= 3}">
                        <p class="text-warning fw-bold">현재 50% 환불 가능합니다. (체크인까지 ${daysDiff}일 남음)</p>
                        <c:set var="refundAmount" value="${payment.amount * 0.5}" />
                    </c:when>
                    <c:otherwise>
                        <p class="text-danger fw-bold">현재 환불이 불가능합니다. (체크인까지 ${daysDiff}일 남음)</p>
                        <c:set var="refundAmount" value="0" />
                    </c:otherwise>
                </c:choose>
                
                <p>예상 환불 금액: <strong><fmt:formatNumber value="${refundAmount}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></strong></p>
            </div>
            
            <form action="${pageContext.request.contextPath}/payment/cancel" method="post">
                <input type="hidden" name="paymentId" value="${payment.paymentId}">
                
                <div class="mb-3">
                    <label for="cancelReason" class="form-label">취소 사유</label>
                    <select class="form-select" id="cancelReason" name="cancelReason" required>
                        <option value="">취소 사유를 선택해주세요</option>
                        <option value="일정 변경">일정 변경</option>
                        <option value="다른 숙소 예약">다른 숙소 예약</option>
                        <option value="개인 사정">개인 사정</option>
                        <option value="기타">기타</option>
                    </select>
                </div>
                
                <div class="mb-3" id="otherReasonContainer" style="display: none;">
                    <label for="otherReason" class="form-label">기타 사유</label>
                    <textarea class="form-control" id="otherReason" rows="3" placeholder="취소 사유를 입력해주세요"></textarea>
                </div>
                
                <div class="form-check mb-4">
                    <input class="form-check-input" type="checkbox" id="confirmCancel" required>
                    <label class="form-check-label" for="confirmCancel">
                        위 환불 정책을 확인하였으며, 결제 취소에 동의합니다.
                    </label>
                </div>
                
                <div class="d-grid gap-2">
                    <button type="submit" class="btn btn-danger" id="cancelButton" disabled>결제 취소하기</button>
                    <a href="${pageContext.request.contextPath}/payment/detail/${payment.paymentId}" class="btn btn-outline-secondary">취소</a>
                </div>
            </form>
        </div>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 취소 사유가 '기타'인 경우 추가 입력 필드 표시
        document.getElementById('cancelReason').addEventListener('change', function() {
            const otherReasonContainer = document.getElementById('otherReasonContainer');
            if (this.value === '기타') {
                otherReasonContainer.style.display = 'block';
            } else {
                otherReasonContainer.style.display = 'none';
            }
        });
        
        // 취소 확인 체크박스 이벤트
        document.getElementById('confirmCancel').addEventListener('change', function() {
            document.getElementById('cancelButton').disabled = !this.checked;
        });
        
        // 폼 제출 전 유효성 검사
        document.querySelector('form').addEventListener('submit', function(event) {
            const cancelReason = document.getElementById('cancelReason').value;
            
            if (!cancelReason) {
                event.preventDefault();
                alert('취소 사유를 선택해주세요.');
                return;
            }
            
            if (cancelReason === '기타') {
                const otherReason = document.getElementById('otherReason').value.trim();
                if (!otherReason) {
                    event.preventDefault();
                    alert('기타 사유를 입력해주세요.');
                    return;
                }
                
                // 기타 사유를 취소 사유 필드에 추가
                const hiddenInput = document.createElement('input');
                hiddenInput.type = 'hidden';
                hiddenInput.name = 'cancelReason';
                hiddenInput.value = '기타: ' + otherReason;
                this.appendChild(hiddenInput);
            }
            
            // 최종 확인
            if (!confirm('정말로 결제를 취소하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
                event.preventDefault();
            }
        });
    </script>
</body>
</html>