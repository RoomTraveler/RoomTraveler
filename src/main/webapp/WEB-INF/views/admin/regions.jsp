<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>지역 데이터 관리</title>
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
    </style>
</head>
<body>
    <div class="container mt-4">
        <h1 class="mb-4">지역 데이터 관리</h1>
        
        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">데이터 가져오기</h5>
                        <div class="d-flex flex-wrap">
                            <button id="importSidosBtn" class="btn btn-primary btn-action">시도 데이터 가져오기</button>
                            <button id="importGugunsBtn" class="btn btn-primary btn-action">구군 데이터 가져오기</button>
                            <a href="/admin" class="btn btn-secondary btn-action">대시보드로 돌아가기</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 결과 메시지 표시 영역 -->
        <div id="resultMessage" class="alert alert-success mt-3" style="display: none;"></div>
        <div id="errorMessage" class="alert alert-danger mt-3" style="display: none;"></div>
        
        <div class="row">
            <div class="col-12">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">시도 목록</h5>
                        <div class="table-responsive">
                            <table class="table table-striped">
                                <thead>
                                    <tr>
                                        <th>코드</th>
                                        <th>이름</th>
                                        <th>구군 보기</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="sido" items="${sidos}">
                                        <tr>
                                            <td>${sido.code}</td>
                                            <td>${sido.name}</td>
                                            <td>
                                                <button class="btn btn-sm btn-info view-guguns" data-sido-code="${sido.code}">구군 보기</button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty sidos}">
                                        <tr>
                                            <td colspan="3" class="text-center">시도 데이터가 없습니다. '시도 데이터 가져오기' 버튼을 클릭하여 데이터를 가져오세요.</td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <!-- 구군 목록 모달 -->
    <div class="modal fade" id="gugunsModal" tabindex="-1" aria-labelledby="gugunsModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="gugunsModalLabel">구군 목록</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="table-responsive">
                        <table class="table table-striped" id="gugunsTable">
                            <thead>
                                <tr>
                                    <th>코드</th>
                                    <th>이름</th>
                                </tr>
                            </thead>
                            <tbody>
                                <!-- 구군 데이터가 여기에 동적으로 로드됩니다 -->
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        $(document).ready(function() {
            // 시도 데이터 가져오기 버튼 클릭 이벤트
            $("#importSidosBtn").click(function() {
                if (confirm("TourAPI에서 시도 데이터를 가져오시겠습니까?")) {
                    $.ajax({
                        url: "/admin/regions/import-sidos",
                        type: "POST",
                        dataType: "json",
                        beforeSend: function() {
                            $("#importSidosBtn").prop("disabled", true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 처리 중...');
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
                                $("#importSidosBtn").prop("disabled", false).text("시도 데이터 가져오기");
                            }
                        },
                        error: function(xhr, status, error) {
                            $("#errorMessage").text("오류가 발생했습니다: " + error).show();
                            $("#resultMessage").hide();
                            $("#importSidosBtn").prop("disabled", false).text("시도 데이터 가져오기");
                        }
                    });
                }
            });
            
            // 구군 데이터 가져오기 버튼 클릭 이벤트
            $("#importGugunsBtn").click(function() {
                if (confirm("TourAPI에서 구군 데이터를 가져오시겠습니까? 이 작업은 시간이 오래 걸릴 수 있습니다.")) {
                    $.ajax({
                        url: "/admin/regions/import-guguns",
                        type: "POST",
                        dataType: "json",
                        beforeSend: function() {
                            $("#importGugunsBtn").prop("disabled", true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 처리 중...');
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
                                $("#importGugunsBtn").prop("disabled", false).text("구군 데이터 가져오기");
                            }
                        },
                        error: function(xhr, status, error) {
                            $("#errorMessage").text("오류가 발생했습니다: " + error).show();
                            $("#resultMessage").hide();
                            $("#importGugunsBtn").prop("disabled", false).text("구군 데이터 가져오기");
                        }
                    });
                }
            });
            
            // 구군 보기 버튼 클릭 이벤트
            $(".view-guguns").click(function() {
                var sidoCode = $(this).data("sido-code");
                var sidoName = $(this).closest("tr").find("td:nth-child(2)").text();
                
                // 모달 제목 설정
                $("#gugunsModalLabel").text(sidoName + " 구군 목록");
                
                // 구군 데이터 로드
                $.ajax({
                    url: "/admin/regions/guguns",
                    type: "GET",
                    data: { sidoCode: sidoCode },
                    dataType: "json",
                    beforeSend: function() {
                        $("#gugunsTable tbody").html('<tr><td colspan="2" class="text-center"><span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 로딩 중...</td></tr>');
                    },
                    success: function(response) {
                        var html = '';
                        
                        if (response.length > 0) {
                            $.each(response, function(index, gugun) {
                                html += '<tr>';
                                html += '<td>' + gugun.code + '</td>';
                                html += '<td>' + gugun.name + '</td>';
                                html += '</tr>';
                            });
                        } else {
                            html = '<tr><td colspan="2" class="text-center">구군 데이터가 없습니다.</td></tr>';
                        }
                        
                        $("#gugunsTable tbody").html(html);
                    },
                    error: function(xhr, status, error) {
                        $("#gugunsTable tbody").html('<tr><td colspan="2" class="text-center text-danger">데이터를 불러오는 중 오류가 발생했습니다: ' + error + '</td></tr>');
                    }
                });
                
                // 모달 표시
                var gugunsModal = new bootstrap.Modal(document.getElementById('gugunsModal'));
                gugunsModal.show();
            });
        });
    </script>
</body>
</html>