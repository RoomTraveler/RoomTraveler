<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>관리자 대시보드</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        .card {
            margin-bottom: 20px;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .btn-action {
            margin-right: 10px;
            margin-bottom: 10px;
        }
        .stats-card {
            background-color: #f8f9fa;
            border-left: 4px solid #0d6efd;
        }
    </style>
</head>
<body>
    <div class="container mt-4">
        <h1 class="mb-4">관리자 대시보드</h1>
        
        <div class="row">
            <!-- 사용자 통계 -->
            <div class="col-md-4">
                <div class="card stats-card">
                    <div class="card-body">
                        <h5 class="card-title">사용자 통계</h5>
                        <p>총 사용자 수: <strong>${userStats.totalUsers}</strong></p>
                        <p>활성 사용자 수: <strong>${userStats.activeUsers}</strong></p>
                        <p>호스트 수: <strong>${userStats.hostUsers}</strong></p>
                        <p>관리자 수: <strong>${userStats.adminUsers}</strong></p>
                    </div>
                </div>
            </div>
            
            <!-- 숙소 통계 -->
            <div class="col-md-4">
                <div class="card stats-card">
                    <div class="card-body">
                        <h5 class="card-title">숙소 통계</h5>
                        <p>총 숙소 수: <strong>${accommodationStats.totalAccommodations}</strong></p>
                        <p>활성 숙소 수: <strong>${accommodationStats.activeAccommodations}</strong></p>
                        <p>총 객실 수: <strong>${accommodationStats.totalRooms}</strong></p>
                        <p>예약 가능 객실 수: <strong>${accommodationStats.availableRooms}</strong></p>
                    </div>
                </div>
            </div>
            
            <!-- 예약 통계 -->
            <div class="col-md-4">
                <div class="card stats-card">
                    <div class="card-body">
                        <h5 class="card-title">예약 통계</h5>
                        <p>총 예약 수: <strong>${reservationStats.totalReservations}</strong></p>
                        <p>완료된 예약 수: <strong>${reservationStats.completedReservations}</strong></p>
                        <p>진행 중인 예약 수: <strong>${reservationStats.pendingReservations}</strong></p>
                        <p>취소된 예약 수: <strong>${reservationStats.cancelledReservations}</strong></p>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="row mt-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">데이터 관리</h5>
                        <div class="d-flex flex-wrap">
                            <a href="/admin/users" class="btn btn-primary btn-action">사용자 관리</a>
                            <a href="/admin/accommodations" class="btn btn-primary btn-action">숙소 관리</a>
                            <a href="/admin/regions" class="btn btn-primary btn-action">지역 데이터 관리</a>
                            <button id="createSampleDataBtn" class="btn btn-success btn-action">샘플 객실 및 지역 데이터 생성</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 결과 메시지 표시 영역 -->
        <div id="resultMessage" class="alert alert-success mt-3" style="display: none;"></div>
        <div id="errorMessage" class="alert alert-danger mt-3" style="display: none;"></div>
    </div>
    
    <script>
        $(document).ready(function() {
            // 샘플 데이터 생성 버튼 클릭 이벤트
            $("#createSampleDataBtn").click(function() {
                if (confirm("샘플 객실 및 지역 데이터를 생성하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
                    $.ajax({
                        url: "/admin/create-sample-data",
                        type: "POST",
                        dataType: "json",
                        beforeSend: function() {
                            $("#createSampleDataBtn").prop("disabled", true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 처리 중...');
                        },
                        success: function(response) {
                            if (response.success) {
                                $("#resultMessage").text(response.message).show();
                                $("#errorMessage").hide();
                                
                                // 3초 후 페이지 새로고침
                                setTimeout(function() {
                                    location.reload();
                                }, 3000);
                            } else {
                                $("#errorMessage").text(response.message).show();
                                $("#resultMessage").hide();
                                $("#createSampleDataBtn").prop("disabled", false).text("샘플 객실 및 지역 데이터 생성");
                            }
                        },
                        error: function(xhr, status, error) {
                            $("#errorMessage").text("오류가 발생했습니다: " + error).show();
                            $("#resultMessage").hide();
                            $("#createSampleDataBtn").prop("disabled", false).text("샘플 객실 및 지역 데이터 생성");
                        }
                    });
                }
            });
        });
    </script>
</body>
</html>