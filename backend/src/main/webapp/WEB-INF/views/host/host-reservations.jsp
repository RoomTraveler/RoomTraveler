<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>호스트 예약 관리</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.3/font/bootstrap-icons.css">
    <style>
        .reservation-card {
            margin-bottom: 20px;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease;
        }
        .reservation-card:hover {
            transform: translateY(-5px);
        }
        .status-badge {
            font-size: 0.9rem;
            padding: 5px 10px;
            border-radius: 20px;
        }
        .status-pending {
            background-color: #ffc107;
            color: #212529;
        }
        .status-confirmed {
            background-color: #198754;
            color: white;
        }
        .status-cancelled {
            background-color: #dc3545;
            color: white;
        }
        .status-completed {
            background-color: #0d6efd;
            color: white;
        }
    </style>
</head>
<body>
    <!-- 헤더 포함 -->
    <jsp:include page="../fragments/header.jsp" />

    <div class="container mt-5 mb-5">
        <h1 class="mb-4">
            <i class="bi bi-calendar-check"></i> 호스트 예약 관리
        </h1>
        
        <!-- 알림 메시지 표시 -->
        <c:if test="${not empty message}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <!-- 필터링 옵션 -->
        <div class="card mb-4">
            <div class="card-body">
                <h5 class="card-title">예약 필터링</h5>
                <form action="/reservation/host-reservations" method="get" class="row g-3">
                    <div class="col-md-3">
                        <label for="status" class="form-label">예약 상태</label>
                        <select class="form-select" id="status" name="status">
                            <option value="">모든 상태</option>
                            <option value="PENDING">대기 중</option>
                            <option value="CONFIRMED">확정됨</option>
                            <option value="CANCELLED">취소됨</option>
                            <option value="COMPLETED">완료됨</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label for="accommodationId" class="form-label">숙소 선택</label>
                        <select class="form-select" id="accommodationId" name="accommodationId">
                            <option value="">모든 숙소</option>
                            <c:forEach var="accommodation" items="${accommodations}">
                                <option value="${accommodation.accommodationId}">${accommodation.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-3 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary">필터 적용</button>
                    </div>
                </form>
            </div>
        </div>
        
        <!-- 예약 목록 -->
        <c:choose>
            <c:when test="${empty reservations}">
                <div class="alert alert-info">
                    <i class="bi bi-info-circle"></i> 예약 내역이 없습니다.
                </div>
            </c:when>
            <c:otherwise>
                <div class="row">
                    <c:forEach var="reservation" items="${reservations}">
                        <div class="col-md-6">
                            <div class="card reservation-card">
                                <div class="card-header d-flex justify-content-between align-items-center">
                                    <h5 class="mb-0">예약 #${reservation.reservationId}</h5>
                                    <span class="status-badge status-${reservation.status.toLowerCase()}">${reservation.status}</span>
                                </div>
                                <div class="card-body">
                                    <div class="mb-3">
                                        <strong>숙소:</strong> ${reservation.roomName} (${reservation.accommodationName})
                                    </div>
                                    <div class="mb-3">
                                        <strong>게스트:</strong> ${reservation.userName} (${reservation.userEmail})
                                    </div>
                                    <div class="mb-3">
                                        <strong>체크인:</strong> <fmt:formatDate value="${reservation.checkInDate}" pattern="yyyy-MM-dd" />
                                        <strong class="ms-3">체크아웃:</strong> <fmt:formatDate value="${reservation.checkOutDate}" pattern="yyyy-MM-dd" />
                                    </div>
                                    <div class="mb-3">
                                        <strong>인원수:</strong> ${reservation.guestCount}명
                                    </div>
                                    <div class="mb-3">
                                        <strong>총 가격:</strong> <fmt:formatNumber value="${reservation.totalPrice}" type="currency" currencySymbol="₩" />
                                    </div>
                                    <div class="mb-3">
                                        <strong>결제 상태:</strong> ${reservation.paymentStatus}
                                    </div>
                                    <c:if test="${not empty reservation.specialRequests}">
                                        <div class="mb-3">
                                            <strong>특별 요청:</strong>
                                            <p class="mb-0">${reservation.specialRequests}</p>
                                        </div>
                                    </c:if>
                                    
                                    <!-- 예약 상태 업데이트 폼 -->
                                    <c:if test="${reservation.status != 'CANCELLED' && reservation.status != 'COMPLETED'}">
                                        <form action="/reservation/update-status/${reservation.reservationId}" method="post" class="mt-3">
                                            <div class="input-group">
                                                <select class="form-select" name="status">
                                                    <option value="PENDING" ${reservation.status == 'PENDING' ? 'selected' : ''}>대기 중</option>
                                                    <option value="CONFIRMED" ${reservation.status == 'CONFIRMED' ? 'selected' : ''}>확정</option>
                                                    <option value="CANCELLED" ${reservation.status == 'CANCELLED' ? 'selected' : ''}>취소</option>
                                                    <option value="COMPLETED" ${reservation.status == 'COMPLETED' ? 'selected' : ''}>완료</option>
                                                </select>
                                                <button class="btn btn-outline-primary" type="submit">상태 변경</button>
                                            </div>
                                        </form>
                                    </c:if>
                                </div>
                                <div class="card-footer text-end">
                                    <a href="/reservation/detail/${reservation.reservationId}" class="btn btn-sm btn-info">
                                        <i class="bi bi-eye"></i> 상세 보기
                                    </a>
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
            const status = urlParams.get('status');
            const accommodationId = urlParams.get('accommodationId');
            
            if (status) {
                document.getElementById('status').value = status;
            }
            
            if (accommodationId) {
                document.getElementById('accommodationId').value = accommodationId;
            }
        });
    </script>
</body>
</html>