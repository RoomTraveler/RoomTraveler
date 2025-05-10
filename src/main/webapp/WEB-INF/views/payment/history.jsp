<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>결제 내역</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        .payment-history-container {
            max-width: 1000px;
            margin: 0 auto;
        }
        .payment-card {
            transition: transform 0.3s;
            margin-bottom: 20px;
            border-radius: 10px;
            overflow: hidden;
        }
        .payment-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .payment-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 15px;
            background-color: #f8f9fa;
            border-bottom: 1px solid #dee2e6;
        }
        .payment-body {
            padding: 15px;
        }
        .payment-footer {
            padding: 15px;
            background-color: #f8f9fa;
            border-top: 1px solid #dee2e6;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .payment-amount {
            font-size: 1.2rem;
            font-weight: bold;
            color: #dc3545;
        }
        .filter-section {
            background-color: #f8f9fa;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 20px;
        }
        .empty-state {
            text-align: center;
            padding: 50px 0;
        }
        .empty-state-icon {
            font-size: 4rem;
            color: #6c757d;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />
    
    <div class="container mt-5 mb-5">
        <div class="payment-history-container">
            <h2 class="mb-4">결제 내역</h2>
            
            <c:if test="${not empty message}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>
            
            <!-- 필터 섹션 -->
            <div class="filter-section">
                <form action="${pageContext.request.contextPath}/payment/history" method="get" class="row g-3">
                    <div class="col-md-3">
                        <label for="startDate" class="form-label">시작일</label>
                        <input type="date" class="form-control" id="startDate" name="startDate" value="${param.startDate}">
                    </div>
                    <div class="col-md-3">
                        <label for="endDate" class="form-label">종료일</label>
                        <input type="date" class="form-control" id="endDate" name="endDate" value="${param.endDate}">
                    </div>
                    <div class="col-md-3">
                        <label for="status" class="form-label">결제 상태</label>
                        <select class="form-select" id="status" name="status">
                            <option value="">전체</option>
                            <option value="COMPLETED" ${param.status == 'COMPLETED' ? 'selected' : ''}>결제 완료</option>
                            <option value="PENDING" ${param.status == 'PENDING' ? 'selected' : ''}>처리 중</option>
                            <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected' : ''}>취소됨</option>
                            <option value="FAILED" ${param.status == 'FAILED' ? 'selected' : ''}>실패</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label for="paymentMethod" class="form-label">결제 방법</label>
                        <select class="form-select" id="paymentMethod" name="paymentMethod">
                            <option value="">전체</option>
                            <option value="CARD" ${param.paymentMethod == 'CARD' ? 'selected' : ''}>신용카드</option>
                            <option value="BANK_TRANSFER" ${param.paymentMethod == 'BANK_TRANSFER' ? 'selected' : ''}>계좌이체</option>
                            <option value="PHONE" ${param.paymentMethod == 'PHONE' ? 'selected' : ''}>휴대폰 결제</option>
                        </select>
                    </div>
                    <div class="col-12 d-flex justify-content-end">
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-search"></i> 검색
                        </button>
                        <a href="${pageContext.request.contextPath}/payment/history" class="btn btn-outline-secondary ms-2">
                            <i class="bi bi-arrow-clockwise"></i> 초기화
                        </a>
                    </div>
                </form>
            </div>
            
            <!-- 결제 내역 목록 -->
            <c:choose>
                <c:when test="${empty payments}">
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <i class="bi bi-credit-card"></i>
                        </div>
                        <h4>결제 내역이 없습니다</h4>
                        <p class="text-muted">아직 결제 내역이 없습니다. 숙소를 예약하고 결제해보세요.</p>
                        <a href="${pageContext.request.contextPath}/accommodation/list" class="btn btn-primary mt-3">
                            <i class="bi bi-search"></i> 숙소 찾아보기
                        </a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="row">
                        <c:forEach var="payment" items="${payments}">
                            <div class="col-md-6">
                                <div class="card payment-card">
                                    <div class="payment-header">
                                        <div>
                                            <h5 class="mb-0">${payment.accommodationTitle}</h5>
                                            <small class="text-muted">${payment.roomName}</small>
                                        </div>
                                        <div>
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
                                        </div>
                                    </div>
                                    <div class="payment-body">
                                        <div class="row mb-2">
                                            <div class="col-md-6">
                                                <p><strong>결제 번호:</strong> ${payment.paymentId}</p>
                                                <p><strong>결제 방법:</strong> 
                                                    <c:choose>
                                                        <c:when test="${payment.paymentMethod eq 'CARD'}">신용카드</c:when>
                                                        <c:when test="${payment.paymentMethod eq 'BANK_TRANSFER'}">계좌이체</c:when>
                                                        <c:when test="${payment.paymentMethod eq 'PHONE'}">휴대폰 결제</c:when>
                                                        <c:otherwise>${payment.paymentMethod}</c:otherwise>
                                                    </c:choose>
                                                </p>
                                                <p><strong>결제 일시:</strong> <fmt:formatDate value="${payment.paidAt}" pattern="yyyy-MM-dd HH:mm:ss" /></p>
                                            </div>
                                            <div class="col-md-6">
                                                <p><strong>예약 번호:</strong> ${payment.reservationId}</p>
                                                <p><strong>체크인:</strong> <fmt:formatDate value="${reservation.checkInDate}" pattern="yyyy-MM-dd" /></p>
                                                <p><strong>체크아웃:</strong> <fmt:formatDate value="${reservation.checkOutDate}" pattern="yyyy-MM-dd" /></p>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="payment-footer">
                                        <div class="payment-amount">
                                            <fmt:formatNumber value="${payment.amount}" type="currency" currencySymbol="₩" maxFractionDigits="0"/>
                                        </div>
                                        <div>
                                            <a href="${pageContext.request.contextPath}/payment/detail/${payment.paymentId}" class="btn btn-sm btn-outline-primary">
                                                상세 보기
                                            </a>
                                            <c:if test="${payment.status eq 'COMPLETED'}">
                                                <a href="${pageContext.request.contextPath}/payment/cancel/${payment.paymentId}" class="btn btn-sm btn-outline-danger">
                                                    결제 취소
                                                </a>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 날짜 필터 초기화
        document.addEventListener('DOMContentLoaded', function() {
            // 시작일이 설정되지 않은 경우 3개월 전으로 설정
            const startDateInput = document.getElementById('startDate');
            if (!startDateInput.value) {
                const threeMonthsAgo = new Date();
                threeMonthsAgo.setMonth(threeMonthsAgo.getMonth() - 3);
                startDateInput.value = threeMonthsAgo.toISOString().split('T')[0];
            }
            
            // 종료일이 설정되지 않은 경우 오늘로 설정
            const endDateInput = document.getElementById('endDate');
            if (!endDateInput.value) {
                const today = new Date();
                endDateInput.value = today.toISOString().split('T')[0];
            }
        });
    </script>
</body>
</html>