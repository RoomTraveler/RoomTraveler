<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
	<meta charset="UTF-8">
	<title>메인 페이지</title>
	<!-- Bootstrap CSS -->
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>

<%@ include file="/WEB-INF/views/fragments/header.jsp" %>

<div class="container mt-5">
	<h2 class="text-center mb-4">환영합니다!</h2>

	<c:if test="${not empty username}">
		<div class="row justify-content-center">
			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">사용자 정보</h5>
						<p class="card-text">사용자 정보를 확인하고 수정할 수 있습니다.</p>
						<a href="/user/user-detail" class="btn btn-primary w-100">사용자 정보 페이지</a>
					</div>
				</div>
			</div>

			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">사용자 정보 수정</h5>
						<p class="card-text">개인 정보를 업데이트하세요.</p>
						<a href="/user/user-update-form" class="btn btn-warning w-100">사용자 정보 수정</a>
					</div>
				</div>
			</div>

			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">회원 탈퇴</h5>
						<p class="card-text">계정을 삭제하려면 클릭하세요.</p>
						<a href="/user/delete" class="btn btn-danger w-100">회원 탈퇴</a>
					</div>
				</div>
			</div>
		</div>
	</c:if>

	<div class="row justify-content-center mt-4">
		<div class="col-md-6">
			<div class="card shadow-sm">
				<div class="card-body text-center">
					<h5 class="card-title">관광지 검색</h5>
					<p class="card-text">지역별 관광지를 검색하고 여행을 계획하세요.</p>
					<form action="/map/region-trip-form" method="get">
						<button type="submit" class="btn btn-success w-100">관광지 검색</button>
					</form>
				</div>
			</div>
		</div>
	</div>
</div>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
