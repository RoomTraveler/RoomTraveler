<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>호스트 대시보드</title>
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
        <h2 class="mb-4">호스트 대시보드</h2>
        
        <c:if test="${not empty host}">
            <div class="row mb-4">
                <div class="col-md-12">
                    <div class="card dashboard-card">
                        <div class="card-body">
                            <h3 class="card-title">안녕하세요, ${host.businessName}님!</h3>
                            <p class="card-text">호스트 대시보드에서 숙소 및 예약 현황을 확인하세요.</p>
                            <p class="card-text">
                                <strong>사업자명:</strong> ${host.businessName}<br>
                                <strong>사업자 등록번호:</strong> ${host.businessRegNo}<br>
                                <strong>호스트 상태:</strong> ${host.hostStatus}
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- 통계 요약 -->
        <div class="row">
            <div class="col-md-3">
                <div class="stat-card bg-light-blue dashboard-card">
                    <div class="stat-number">${accommodationCount}</div>
                    <div class="stat-label">등록된 숙소</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-card bg-light-green dashboard-card">
                    <div class="stat-number">${totalReservations}</div>
                    <div class="stat-label">총 예약 수</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-card bg-light-yellow dashboard-card">
                    <div class="stat-number">${confirmedReservations + completedReservations}</div>
                    <div class="stat-label">확정/완료된 예약</div>
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
                        <h5 class="card-title">월별 예약 현황</h5>
                        <div class="chart-container">
                            <canvas id="reservationsChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card dashboard-card">
                    <div class="card-body">
                        <h5 class="card-title">월별 수익 현황</h5>
                        <div class="chart-container">
                            <canvas id="revenueChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 예약 상태 분포 -->
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
                        <h5 class="card-title">숙소별 예약 현황</h5>
                        <div class="chart-container">
                            <canvas id="accommodationReservationsChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 숙소 목록 -->
        <div class="row mt-4">
            <div class="col-md-12">
                <div class="card dashboard-card">
                    <div class="card-body">
                        <h5 class="card-title">내 숙소 목록</h5>
                        <div class="table-responsive">
                            <table class="table table-striped">
                                <thead>
                                    <tr>
                                        <th>숙소명</th>
                                        <th>주소</th>
                                        <th>상태</th>
                                        <th>예약 수</th>
                                        <th>상세 통계</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="accommodation" items="${accommodations}">
                                        <tr>
                                            <td>${accommodation.title}</td>
                                            <td>${accommodation.address}</td>
                                            <td>
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
                                            </td>
                                            <td>
                                                <c:set var="reservationCount" value="0" />
                                                <c:forEach var="reservation" items="${reservations}">
                                                    <c:if test="${reservation.accommodationId eq accommodation.accommodationId}">
                                                        <c:set var="reservationCount" value="${reservationCount + 1}" />
                                                    </c:if>
                                                </c:forEach>
                                                ${reservationCount}
                                            </td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/host/dashboard/accommodation?accommodationId=${accommodation.accommodationId}" class="btn btn-sm btn-primary">상세 통계</a>
                                            </td>
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
        // 월별 예약 차트
        const reservationsCtx = document.getElementById('reservationsChart').getContext('2d');
        const reservationsChart = new Chart(reservationsCtx, {
            type: 'line',
            data: {
                labels: [
                    <c:forEach var="entry" items="${monthlyReservations}" varStatus="status">
                        '${entry.key}'<c:if test="${!status.last}">, </c:if>
                    </c:forEach>
                ],
                datasets: [{
                    label: '월별 예약 수',
                    data: [
                        <c:forEach var="entry" items="${monthlyReservations}" varStatus="status">
                            ${entry.value}<c:if test="${!status.last}">, </c:if>
                        </c:forEach>
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

        // 월별 수익 차트
        const revenueCtx = document.getElementById('revenueChart').getContext('2d');
        const revenueChart = new Chart(revenueCtx, {
            type: 'bar',
            data: {
                labels: [
                    <c:forEach var="entry" items="${monthlyRevenue}" varStatus="status">
                        '${entry.key}'<c:if test="${!status.last}">, </c:if>
                    </c:forEach>
                ],
                datasets: [{
                    label: '월별 수익 (원)',
                    data: [
                        <c:forEach var="entry" items="${monthlyRevenue}" varStatus="status">
                            ${entry.value}<c:if test="${!status.last}">, </c:if>
                        </c:forEach>
                    ],
                    backgroundColor: 'rgba(54, 162, 235, 0.5)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });

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

        // 숙소별 예약 현황 차트
        const accommodationReservationsCtx = document.getElementById('accommodationReservationsChart').getContext('2d');
        const accommodationReservationsChart = new Chart(accommodationReservationsCtx, {
            type: 'bar',
            data: {
                labels: [
                    <c:forEach var="accommodation" items="${accommodations}" varStatus="status">
                        '${accommodation.title}'<c:if test="${!status.last}">, </c:if>
                    </c:forEach>
                ],
                datasets: [{
                    label: '예약 수',
                    data: [
                        <c:forEach var="accommodation" items="${accommodations}" varStatus="status">
                            <c:set var="reservationCount" value="0" />
                            <c:forEach var="reservation" items="${reservations}">
                                <c:if test="${reservation.accommodationId eq accommodation.accommodationId}">
                                    <c:set var="reservationCount" value="${reservationCount + 1}" />
                                </c:if>
                            </c:forEach>
                            ${reservationCount}<c:if test="${!status.last}">, </c:if>
                        </c:forEach>
                    ],
                    backgroundColor: 'rgba(153, 102, 255, 0.5)',
                    borderColor: 'rgba(153, 102, 255, 1)',
                    borderWidth: 1
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