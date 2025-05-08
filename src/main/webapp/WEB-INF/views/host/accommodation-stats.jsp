<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>숙소 상세 통계</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        .dashboard-card {
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            margin-bottom: 20px;
            transition: transform 0.3s;
        }
        .dashboard-card:hover {
            transform: translateY(-5px);
        }
        .stat-card {
            text-align: center;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 20px;
        }
        .stat-number {
            font-size: 2.5rem;
            font-weight: bold;
        }
        .stat-label {
            font-size: 1rem;
            color: #6c757d;
        }
        .bg-light-blue {
            background-color: #e3f2fd;
        }
        .bg-light-green {
            background-color: #e8f5e9;
        }
        .bg-light-yellow {
            background-color: #fffde7;
        }
        .bg-light-red {
            background-color: #ffebee;
        }
        .chart-container {
            position: relative;
            height: 300px;
            margin-bottom: 30px;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />

    <div class="container mt-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>숙소 상세 통계</h2>
            <a href="${pageContext.request.contextPath}/host/dashboard" class="btn btn-outline-primary">대시보드로 돌아가기</a>
        </div>
        
        <c:if test="${not empty accommodation}">
            <div class="row mb-4">
                <div class="col-md-12">
                    <div class="card dashboard-card">
                        <div class="card-body">
                            <h3 class="card-title">${accommodation.title}</h3>
                            <p class="card-text">${accommodation.description}</p>
                            <div class="row">
                                <div class="col-md-6">
                                    <p><strong>주소:</strong> ${accommodation.address}</p>
                                    <p><strong>연락처:</strong> ${accommodation.phone}</p>
                                    <p><strong>이메일:</strong> ${accommodation.email}</p>
                                </div>
                                <div class="col-md-6">
                                    <p>
                                        <strong>상태:</strong> 
                                        <c:choose>
                                            <c:when test="${accommodation.status eq 'ACTIVE'}">
                                                <span class="badge bg-success">활성</span>
                                            </c:when>
                                            <c:when test="${accommodation.status eq 'INACTIVE'}">
                                                <span class="badge bg-secondary">비활성</span>
                                            </c:when>
                                            <c:when test="${accommodation.status eq 'PENDING_REVIEW'}">
                                                <span class="badge bg-warning">검토중</span>
                                            </c:when>
                                        </c:choose>
                                    </p>
                                    <p><strong>체크인 시간:</strong> ${accommodation.checkInTime}</p>
                                    <p><strong>체크아웃 시간:</strong> ${accommodation.checkOutTime}</p>
                                </div>
                            </div>
                            <div class="mt-3">
                                <a href="${pageContext.request.contextPath}/accommodation/detail/${accommodation.accommodationId}" class="btn btn-primary">숙소 상세 보기</a>
                                <a href="${pageContext.request.contextPath}/accommodation/update-form/${accommodation.accommodationId}" class="btn btn-warning">숙소 정보 수정</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- 통계 요약 -->
        <div class="row">
            <div class="col-md-3">
                <div class="stat-card bg-light-blue dashboard-card">
                    <div class="stat-number">${totalReservations}</div>
                    <div class="stat-label">총 예약 수</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-card bg-light-green dashboard-card">
                    <div class="stat-number">${confirmedReservations}</div>
                    <div class="stat-label">확정된 예약</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-card bg-light-yellow dashboard-card">
                    <div class="stat-number">${completedReservations}</div>
                    <div class="stat-label">완료된 예약</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-card bg-light-red dashboard-card">
                    <div class="stat-number"><fmt:formatNumber value="${totalRevenue}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></div>
                    <div class="stat-label">총 수익</div>
                </div>
            </div>
        </div>

        <!-- 차트 -->
        <div class="row mt-4">
            <div class="col-md-6">
                <div class="card dashboard-card">
                    <div class="card-body">
                        <h5 class="card-title">예약 상태 분포</h5>
                        <div class="chart-container">
                            <canvas id="reservationStatusChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card dashboard-card">
                    <div class="card-body">
                        <h5 class="card-title">예약 추이</h5>
                        <div class="chart-container">
                            <canvas id="reservationTrendChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 예약 목록 -->
        <div class="row mt-4">
            <div class="col-md-12">
                <div class="card dashboard-card">
                    <div class="card-body">
                        <h5 class="card-title">예약 목록</h5>
                        <div class="table-responsive">
                            <table class="table table-striped">
                                <thead>
                                    <tr>
                                        <th>예약 ID</th>
                                        <th>객실명</th>
                                        <th>고객명</th>
                                        <th>체크인</th>
                                        <th>체크아웃</th>
                                        <th>인원</th>
                                        <th>금액</th>
                                        <th>상태</th>
                                        <th>결제 상태</th>
                                        <th>예약일</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="reservation" items="${reservations}">
                                        <tr>
                                            <td>${reservation.reservationId}</td>
                                            <td>${reservation.roomName}</td>
                                            <td>${reservation.userName}</td>
                                            <td>${reservation.checkInDate}</td>
                                            <td>${reservation.checkOutDate}</td>
                                            <td>${reservation.guestCount}명</td>
                                            <td><fmt:formatNumber value="${reservation.totalPrice}" type="currency" currencySymbol="₩" maxFractionDigits="0"/></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${reservation.status eq 'CONFIRMED'}">
                                                        <span class="badge bg-success">확정</span>
                                                    </c:when>
                                                    <c:when test="${reservation.status eq 'PENDING'}">
                                                        <span class="badge bg-warning">대기중</span>
                                                    </c:when>
                                                    <c:when test="${reservation.status eq 'CANCELLED'}">
                                                        <span class="badge bg-danger">취소</span>
                                                    </c:when>
                                                    <c:when test="${reservation.status eq 'COMPLETED'}">
                                                        <span class="badge bg-info">완료</span>
                                                    </c:when>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${reservation.paymentStatus eq 'PAID'}">
                                                        <span class="badge bg-success">결제완료</span>
                                                    </c:when>
                                                    <c:when test="${reservation.paymentStatus eq 'UNPAID'}">
                                                        <span class="badge bg-warning">미결제</span>
                                                    </c:when>
                                                    <c:when test="${reservation.paymentStatus eq 'REFUNDED'}">
                                                        <span class="badge bg-secondary">환불</span>
                                                    </c:when>
                                                </c:choose>
                                            </td>
                                            <td><fmt:formatDate value="${reservation.createdAt}" pattern="yyyy-MM-dd" /></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="../fragments/footer.jsp" />

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <!-- Chart.js 초기화 -->
    <script>
        // 예약 상태 분포 차트
        const statusCtx = document.getElementById('reservationStatusChart').getContext('2d');
        const statusChart = new Chart(statusCtx, {
            type: 'pie',
            data: {
                labels: ['확정', '대기중', '취소', '완료'],
                datasets: [{
                    data: [${confirmedReservations}, ${pendingReservations}, ${cancelledReservations}, ${completedReservations}],
                    backgroundColor: [
                        'rgba(54, 162, 235, 0.5)',
                        'rgba(255, 206, 86, 0.5)',
                        'rgba(255, 99, 132, 0.5)',
                        'rgba(75, 192, 192, 0.5)'
                    ],
                    borderColor: [
                        'rgba(54, 162, 235, 1)',
                        'rgba(255, 206, 86, 1)',
                        'rgba(255, 99, 132, 1)',
                        'rgba(75, 192, 192, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false
            }
        });

        // 예약 추이 차트 (더미 데이터 사용)
        const trendCtx = document.getElementById('reservationTrendChart').getContext('2d');
        const trendChart = new Chart(trendCtx, {
            type: 'line',
            data: {
                labels: ['1월', '2월', '3월', '4월', '5월', '6월'],
                datasets: [{
                    label: '예약 수',
                    data: [
                        Math.floor(Math.random() * 10),
                        Math.floor(Math.random() * 10),
                        Math.floor(Math.random() * 10),
                        Math.floor(Math.random() * 10),
                        Math.floor(Math.random() * 10),
                        Math.floor(Math.random() * 10)
                    ],
                    borderColor: 'rgba(75, 192, 192, 1)',
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    tension: 0.1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        }
                    }
                }
            }
        });
    </script>
</body>
</html>