<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>사용자 관리</title>
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
        <h1 class="mb-4">사용자 관리</h1>
        
        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">관리 메뉴</h5>
                        <div class="d-flex flex-wrap">
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
                        <h5 class="card-title">사용자 목록</h5>
                        <div class="table-responsive">
                            <table class="table table-striped">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>이름</th>
                                        <th>이메일</th>
                                        <th>역할</th>
                                        <th>상태</th>
                                        <th>가입일</th>
                                        <th>관리</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="user" items="${users}">
                                        <tr>
                                            <td>${user.userId}</td>
                                            <td>${user.name}</td>
                                            <td>${user.email}</td>
                                            <td>${user.role}</td>
                                            <td>
                                                <span class="badge ${user.status == 'ACTIVE' ? 'bg-success' : 'bg-danger'}">${user.status}</span>
                                            </td>
                                            <td>${user.createdAt}</td>
                                            <td>
                                                <div class="btn-group">
                                                    <button class="btn btn-sm btn-outline-primary status-toggle" data-user-id="${user.userId}" data-current-status="${user.status}" data-new-status="${user.status == 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}">
                                                        ${user.status == 'ACTIVE' ? '비활성화' : '활성화'}
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty users}">
                                        <tr>
                                            <td colspan="7" class="text-center">등록된 사용자가 없습니다.</td>
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
    
    <script>
        $(document).ready(function() {
            // 사용자 상태 변경 버튼 클릭 이벤트
            $(".status-toggle").click(function() {
                var userId = $(this).data("user-id");
                var currentStatus = $(this).data("current-status");
                var newStatus = $(this).data("new-status");
                var button = $(this);
                
                if (confirm("사용자 상태를 " + (currentStatus == "ACTIVE" ? "비활성화" : "활성화") + "하시겠습니까?")) {
                    $.ajax({
                        url: "/admin/users/" + userId + "/status",
                        type: "POST",
                        data: { status: newStatus },
                        dataType: "json",
                        beforeSend: function() {
                            button.prop("disabled", true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>');
                        },
                        success: function(response) {
                            if (response.success) {
                                // 상태 변경 성공 시 UI 업데이트
                                var row = button.closest("tr");
                                var badge = row.find(".badge");
                                
                                if (newStatus == "ACTIVE") {
                                    badge.removeClass("bg-danger").addClass("bg-success").text("ACTIVE");
                                    button.text("비활성화");
                                } else {
                                    badge.removeClass("bg-success").addClass("bg-danger").text("INACTIVE");
                                    button.text("활성화");
                                }
                                
                                button.data("current-status", newStatus);
                                button.data("new-status", newStatus == "ACTIVE" ? "INACTIVE" : "ACTIVE");
                                
                                $("#resultMessage").text(response.message).show();
                                $("#errorMessage").hide();
                                
                                setTimeout(function() {
                                    $("#resultMessage").hide();
                                }, 3000);
                            } else {
                                $("#errorMessage").text(response.message).show();
                                $("#resultMessage").hide();
                                
                                setTimeout(function() {
                                    $("#errorMessage").hide();
                                }, 3000);
                            }
                            
                            button.prop("disabled", false);
                        },
                        error: function(xhr, status, error) {
                            $("#errorMessage").text("오류가 발생했습니다: " + error).show();
                            $("#resultMessage").hide();
                            
                            setTimeout(function() {
                                $("#errorMessage").hide();
                            }, 3000);
                            
                            button.prop("disabled", false);
                        }
                    });
                }
            });
        });
    </script>
</body>
</html>