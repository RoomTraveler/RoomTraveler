<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>숙소 관리</title>
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
        .accommodation-card {
            height: 100%;
        }
        .accommodation-image {
            height: 200px;
            object-fit: cover;
        }
    </style>
</head>
<body>
    <div class="container mt-4">
        <h1 class="mb-4">숙소 관리</h1>

        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">데이터 관리</h5>
                        <div class="d-flex flex-wrap">
                            <button id="importAccommodationsBtn" class="btn btn-primary btn-action" data-bs-toggle="modal" data-bs-target="#importModal">TourAPI에서 숙소 가져오기</button>
                            <button id="createSampleDataBtn" class="btn btn-success btn-action">샘플 객실 및 지역 데이터 생성</button>
                            <button id="deleteAllAccommodationsBtn" class="btn btn-danger btn-action">모든 숙소 삭제</button>
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
            <c:forEach var="accommodation" items="${accommodations}">
                <div class="col-md-4 mb-4">
                    <div class="card accommodation-card">
                        <img src="${accommodation.mainImageUrl != null ? accommodation.mainImageUrl : 'https://via.placeholder.com/300x200?text=No+Image'}" class="card-img-top accommodation-image" alt="${accommodation.title}">
                        <div class="card-body">
                            <h5 class="card-title">${accommodation.title}</h5>
                            <p class="card-text">
                                <small class="text-muted">${accommodation.address}</small>
                            </p>
                            <p class="card-text">
                                <span class="badge ${accommodation.status == 'ACTIVE' ? 'bg-success' : 'bg-danger'}">${accommodation.status}</span>
                            </p>
                            <div class="d-flex justify-content-between">
                                <button class="btn btn-sm btn-info view-rooms" data-accommodation-id="${accommodation.accommodationId}">객실 보기</button>
                                <div class="btn-group">
                                    <button class="btn btn-sm btn-outline-primary status-toggle" data-accommodation-id="${accommodation.accommodationId}" data-current-status="${accommodation.status}" data-new-status="${accommodation.status == 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}">
                                        ${accommodation.status == 'ACTIVE' ? '비활성화' : '활성화'}
                                    </button>
                                    <button class="btn btn-sm btn-outline-danger delete-accommodation" data-accommodation-id="${accommodation.accommodationId}">삭제</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty accommodations}">
                <div class="col-12">
                    <div class="alert alert-info">
                        등록된 숙소가 없습니다. 'TourAPI에서 숙소 가져오기' 또는 '샘플 객실 및 지역 데이터 생성' 버튼을 클릭하여 데이터를 가져오세요.
                    </div>
                </div>
            </c:if>
        </div>
    </div>

    <!-- 숙소 가져오기 모달 -->
    <div class="modal fade" id="importModal" tabindex="-1" aria-labelledby="importModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="importModalLabel">TourAPI에서 숙소 가져오기</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <form id="importForm">
                        <div class="mb-3">
                            <label for="sidoCode" class="form-label">시도 선택</label>
                            <select class="form-select" id="sidoCode" name="sidoCode">
                                <option value="">전체</option>
                                <c:forEach var="sido" items="${sidos}">
                                    <option value="${sido.code}">${sido.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="gugunCode" class="form-label">구군 선택</label>
                            <select class="form-select" id="gugunCode" name="gugunCode" disabled>
                                <option value="">전체</option>
                            </select>
                        </div>
                        <div class="mb-3 form-check">
                            <input type="checkbox" class="form-check-input" id="clearExisting" name="clearExisting">
                            <label class="form-check-label" for="clearExisting">기존 데이터 삭제 후 가져오기</label>
                            <div class="form-text">체크하면 기존 숙소 데이터를 모두 삭제한 후 새로운 데이터를 가져옵니다.</div>
                        </div>
                        <div class="alert alert-warning">
                            <small>모든 데이터를 가져오는 작업은 시간이 오래 걸릴 수 있습니다. 잠시만 기다려주세요.</small>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
                    <button type="button" class="btn btn-primary" id="startImport">가져오기</button>
                </div>
            </div>
        </div>
    </div>

    <script>
        $(document).ready(function() {
            // 시도 선택 시 구군 목록 로드
            $("#sidoCode").change(function() {
                var sidoCode = $(this).val();

                if (sidoCode) {
                    $.ajax({
                        url: "/admin/regions/guguns",
                        type: "GET",
                        data: { sidoCode: sidoCode },
                        dataType: "json",
                        success: function(response) {
                            var html = '<option value="">전체</option>';

                            if (response.length > 0) {
                                $.each(response, function(index, gugun) {
                                    html += '<option value="' + gugun.code + '">' + gugun.name + '</option>';
                                });
                            }

                            $("#gugunCode").html(html).prop("disabled", false);
                        },
                        error: function(xhr, status, error) {
                            console.error("구군 데이터 로드 중 오류 발생:", error);
                        }
                    });
                } else {
                    $("#gugunCode").html('<option value="">전체</option>').prop("disabled", true);
                }
            });

            // 숙소 가져오기 시작
            $("#startImport").click(function() {
                var sidoCode = $("#sidoCode").val();
                var gugunCode = $("#gugunCode").val();
                var clearExisting = $("#clearExisting").is(":checked");

                if (confirm("선택한 조건으로 숙소 데이터를 가져오시겠습니까? 이 작업은 시간이 오래 걸릴 수 있습니다." + (clearExisting ? " 기존 데이터가 모두 삭제됩니다!" : ""))) {
                    $.ajax({
                        url: "/admin/accommodations/import",
                        type: "POST",
                        data: {
                            sidoCode: sidoCode,
                            gugunCode: gugunCode,
                            clearExisting: clearExisting
                        },
                        dataType: "json",
                        beforeSend: function() {
                            $("#startImport").prop("disabled", true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 처리 중...');
                        },
                        success: function(response) {
                            if (response.success) {
                                $("#importModal").modal("hide");
                                $("#resultMessage").text(response.message).show();
                                $("#errorMessage").hide();

                                // 3초 후 페이지 새로고침
                                setTimeout(function() {
                                    location.reload();
                                }, 3000);
                            } else {
                                $("#errorMessage").text(response.message).show();
                                $("#resultMessage").hide();
                                $("#startImport").prop("disabled", false).text("가져오기");
                            }
                        },
                        error: function(xhr, status, error) {
                            $("#errorMessage").text("오류가 발생했습니다: " + error).show();
                            $("#resultMessage").hide();
                            $("#startImport").prop("disabled", false).text("가져오기");
                        }
                    });
                }
            });

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

            // 모든 숙소 삭제 버튼 클릭 이벤트
            $("#deleteAllAccommodationsBtn").click(function() {
                if (confirm("정말로 모든 숙소를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
                    $.ajax({
                        url: "/admin/accommodations/all",
                        type: "DELETE",
                        dataType: "json",
                        beforeSend: function() {
                            $("#deleteAllAccommodationsBtn").prop("disabled", true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 처리 중...');
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
                                $("#deleteAllAccommodationsBtn").prop("disabled", false).text("모든 숙소 삭제");
                            }
                        },
                        error: function(xhr, status, error) {
                            $("#errorMessage").text("오류가 발생했습니다: " + error).show();
                            $("#resultMessage").hide();
                            $("#deleteAllAccommodationsBtn").prop("disabled", false).text("모든 숙소 삭제");
                        }
                    });
                }
            });

            // 숙소 상태 변경 버튼 클릭 이벤트
            $(".status-toggle").click(function() {
                var accommodationId = $(this).data("accommodation-id");
                var currentStatus = $(this).data("current-status");
                var newStatus = $(this).data("new-status");
                var button = $(this);

                if (confirm("숙소 상태를 " + (currentStatus == "ACTIVE" ? "비활성화" : "활성화") + "하시겠습니까?")) {
                    $.ajax({
                        url: "/admin/accommodations/" + accommodationId + "/status",
                        type: "POST",
                        data: { status: newStatus },
                        dataType: "json",
                        beforeSend: function() {
                            button.prop("disabled", true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>');
                        },
                        success: function(response) {
                            if (response.success) {
                                // 상태 변경 성공 시 UI 업데이트
                                var card = button.closest(".card");
                                var badge = card.find(".badge");

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

            // 숙소 삭제 버튼 클릭 이벤트
            $(".delete-accommodation").click(function() {
                var accommodationId = $(this).data("accommodation-id");
                var button = $(this);

                if (confirm("이 숙소를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
                    $.ajax({
                        url: "/admin/accommodations/" + accommodationId,
                        type: "DELETE",
                        dataType: "json",
                        beforeSend: function() {
                            button.prop("disabled", true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>');
                        },
                        success: function(response) {
                            if (response.success) {
                                // 삭제 성공 시 카드 제거
                                button.closest(".col-md-4").fadeOut(function() {
                                    $(this).remove();

                                    // 남은 숙소가 없으면 안내 메시지 표시
                                    if ($(".accommodation-card").length == 0) {
                                        $(".row").append('<div class="col-12"><div class="alert alert-info">등록된 숙소가 없습니다. \'TourAPI에서 숙소 가져오기\' 또는 \'샘플 객실 및 지역 데이터 생성\' 버튼을 클릭하여 데이터를 가져오세요.</div></div>');
                                    }
                                });

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

                                button.prop("disabled", false).html("삭제");
                            }
                        },
                        error: function(xhr, status, error) {
                            $("#errorMessage").text("오류가 발생했습니다: " + error).show();
                            $("#resultMessage").hide();

                            setTimeout(function() {
                                $("#errorMessage").hide();
                            }, 3000);

                            button.prop("disabled", false).html("삭제");
                        }
                    });
                }
            });
        });
    </script>
</body>
</html>
