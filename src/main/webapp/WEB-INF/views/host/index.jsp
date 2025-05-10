<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>호스트 페이지</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .hero-section {
            background-color: #f8f9fa;
            padding: 60px 0;
            margin-bottom: 30px;
            border-radius: 10px;
        }
        .card {
            transition: transform 0.3s;
            margin-bottom: 20px;
        }
        .card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 20px rgba(0,0,0,0.1);
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />

    <div class="container">
        <!-- 호스트 메인 배너 -->
        <div class="hero-section text-center">
            <h1 class="display-4">호스트 포털에 오신 것을 환영합니다</h1>
            <p class="lead">숙소를 등록하고 관리하세요. 새로운 호스트가 되어보세요!</p>

            <c:if test="${empty username}">
                <div class="mt-4">
                    <a href="${pageContext.request.contextPath}/host/login-form" class="btn btn-primary me-2">로그인</a>
                    <a href="${pageContext.request.contextPath}/host/regist-user-form" class="btn btn-success">호스트 가입하기</a>
                </div>
            </c:if>
        </div>

        <c:if test="${not empty username}">
            <!-- 로그인한 호스트를 위한 대시보드 -->
            <div class="row mb-4">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-body">
                            <h3 class="card-title">안녕하세요, ${username}님!</h3>
                            <p class="card-text">오늘도 좋은 하루 되세요. 아래에서 호스트 관련 기능을 이용하실 수 있습니다.</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 호스트 기능 카드 -->
            <div class="row">
                <div class="col-md-4">
                    <div class="card h-100">
                        <div class="card-body text-center">
                            <h5 class="card-title">내 호스트 정보</h5>
                            <p class="card-text">호스트 정보를 확인하고 수정합니다.</p>
                            <a href="${pageContext.request.contextPath}/host/detail/${sessionScope.userId}" class="btn btn-primary w-100">호스트 정보 관리</a>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="card h-100">
                        <div class="card-body text-center">
                            <h5 class="card-title">내 숙소 관리</h5>
                            <p class="card-text">등록한 숙소를 관리합니다.</p>
                            <a href="${pageContext.request.contextPath}/accommodation/host/accommodations" class="btn btn-success w-100">숙소 관리</a>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="card h-100">
                        <div class="card-body text-center">
                            <h5 class="card-title">예약 관리</h5>
                            <p class="card-text">숙소 예약 현황을 확인합니다.</p>
                            <a href="${pageContext.request.contextPath}/reservation/host-reservations" class="btn btn-info w-100">예약 관리</a>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row mt-4">
                <div class="col-md-6">
                    <div class="card h-100">
                        <div class="card-body text-center">
                            <h5 class="card-title">새 숙소 등록</h5>
                            <p class="card-text">새로운 숙소를 등록합니다.</p>
                            <a href="${pageContext.request.contextPath}/host/register-accommodation" class="btn btn-warning w-100">숙소 등록</a>
                        </div>
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="card h-100">
                        <div class="card-body text-center">
                            <h5 class="card-title">호스트 대시보드</h5>
                            <p class="card-text">숙소 예약 및 수익 통계를 확인합니다.</p>
                            <a href="${pageContext.request.contextPath}/host/dashboard" class="btn btn-success w-100">대시보드 보기</a>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- 호스트 혜택 소개 -->
        <div class="row mt-5">
            <div class="col-md-12">
                <h3 class="text-center mb-4">호스트가 되면 누릴 수 있는 혜택</h3>
            </div>

            <div class="col-md-4">
                <div class="card h-100">
                    <div class="card-body text-center">
                        <h5 class="card-title">추가 수입</h5>
                        <p class="card-text">여분의 공간을 활용하여 추가 수입을 올릴 수 있습니다.</p>
                    </div>
                </div>
            </div>

            <div class="col-md-4">
                <div class="card h-100">
                    <div class="card-body text-center">
                        <h5 class="card-title">유연한 일정</h5>
                        <p class="card-text">원하는 날짜와 시간에 맞춰 숙소를 운영할 수 있습니다.</p>
                    </div>
                </div>
            </div>

            <div class="col-md-4">
                <div class="card h-100">
                    <div class="card-body text-center">
                        <h5 class="card-title">새로운 만남</h5>
                        <p class="card-text">다양한 게스트를 만나고 새로운 경험을 쌓을 수 있습니다.</p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="../fragments/footer.jsp" />

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
