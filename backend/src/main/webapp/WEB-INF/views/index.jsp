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
			<h4 class="text-center mb-3">내 계정</h4>

			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">사용자 정보</h5>
						<p class="card-text">사용자 정보를 확인하고 수정할 수 있습니다.</p>
      <a href="${pageContext.request.contextPath}/user/detail" class="btn btn-primary w-100">사용자 정보 페이지</a>
					</div>
				</div>
			</div>

			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">사용자 정보 수정</h5>
						<p class="card-text">개인 정보를 업데이트하세요.</p>
      <a href="${pageContext.request.contextPath}/user/update-form" class="btn btn-warning w-100">사용자 정보 수정</a>
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

		<!-- 일반 사용자 기능 -->
		<div class="row justify-content-center mt-4">
			<h4 class="text-center mb-3">내 여행</h4>

			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">내 예약</h5>
						<p class="card-text">예약한 숙소를 확인합니다.</p>
						<a href="${pageContext.request.contextPath}/reservation/my-reservations" class="btn btn-primary w-100">예약 확인</a>
					</div>
				</div>
			</div>

			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">찜한 숙소</h5>
						<p class="card-text">관심있는 숙소 목록을 확인합니다.</p>
						<a href="${pageContext.request.contextPath}/accommodation/favorites" class="btn btn-danger w-100">찜 목록</a>
					</div>
				</div>
			</div>

			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">여행 후기</h5>
						<p class="card-text">내가 작성한 리뷰를 관리합니다.</p>
						<a href="${pageContext.request.contextPath}/review/my-reviews" class="btn btn-success w-100">내 리뷰</a>
					</div>
				</div>
			</div>
		</div>
	</c:if>

	<!-- 여행 계획 섹션 -->
	<div class="row justify-content-center mt-4">
		<h4 class="text-center mb-3">여행 계획</h4>

		<div class="col-md-4">
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

		<div class="col-md-4">
			<div class="card shadow-sm">
				<div class="card-body text-center">
					<h5 class="card-title">인기 여행지</h5>
					<p class="card-text">인기 있는 여행지를 확인하세요.</p>
					<a href="${pageContext.request.contextPath}/tourapi/popular" class="btn btn-primary w-100">인기 여행지</a>
				</div>
			</div>
		</div>

		<div class="col-md-4">
			<div class="card shadow-sm">
				<div class="card-body text-center">
					<h5 class="card-title">여행 가이드</h5>
					<p class="card-text">여행 계획에 도움이 되는 가이드를 확인하세요.</p>
					<a href="${pageContext.request.contextPath}/tourapi/guides" class="btn btn-info w-100">여행 가이드</a>
				</div>
			</div>
		</div>
	</div>

	<!-- 숙소 섹션 -->
	<div class="row justify-content-center mt-4">
		<h4 class="text-center mb-3">숙소</h4>

		<div class="col-md-4">
			<div class="card shadow-sm">
				<div class="card-body text-center">
					<h5 class="card-title">숙소 찾기</h5>
					<p class="card-text">다양한 숙소를 검색하고 예약하세요.</p>
					<a href="${pageContext.request.contextPath}/accommodation/list" class="btn btn-primary w-100">숙소 목록</a>
				</div>
			</div>
		</div>

		<div class="col-md-4">
			<div class="card shadow-sm">
				<div class="card-body text-center">
					<h5 class="card-title">특가 숙소</h5>
					<p class="card-text">할인 중인 특가 숙소를 확인하세요.</p>
					<a href="${pageContext.request.contextPath}/accommodation/special-offers" class="btn btn-danger w-100">특가 숙소</a>
				</div>
			</div>
		</div>

		<div class="col-md-4">
			<div class="card shadow-sm">
				<div class="card-body text-center">
					<h5 class="card-title">추천 숙소</h5>
					<p class="card-text">높은 평점의 추천 숙소를 확인하세요.</p>
					<a href="${pageContext.request.contextPath}/accommodation/recommended" class="btn btn-success w-100">추천 숙소</a>
				</div>
			</div>
		</div>

		<c:if test="${sessionScope.role eq 'HOST' or sessionScope.role eq 'ADMIN'}">
		<div class="col-md-4">
			<div class="card shadow-sm">
				<div class="card-body text-center">
					<h5 class="card-title">숙소 등록</h5>
					<p class="card-text">새로운 숙소를 등록합니다.</p>
					<a href="${pageContext.request.contextPath}/host/register-accommodation" class="btn btn-success w-100">숙소 등록</a>
				</div>
			</div>
		</div>
		</c:if>

		<div class="col-md-4">
			<div class="card shadow-sm">
				<div class="card-body text-center">
					<h5 class="card-title">호스트 사이트</h5>
					<p class="card-text">호스트 전용 사이트로 이동합니다.</p>
					<a href="${pageContext.request.contextPath}/host" class="btn btn-warning w-100">호스트 사이트</a>
				</div>
			</div>
		</div>

		<div class="col-md-4">
			<div class="card shadow-sm">
				<div class="card-body text-center">
					<h5 class="card-title">호스트 신청</h5>
					<p class="card-text">호스트가 되어 숙소를 등록해보세요.</p>
					<c:if test="${not empty username and sessionScope.role ne 'HOST' and sessionScope.role ne 'ADMIN'}">
						<a href="${pageContext.request.contextPath}/host/regist-form" class="btn btn-outline-warning w-100">호스트 신청하기</a>
					</c:if>
					<c:if test="${empty username}">
						<a href="${pageContext.request.contextPath}/host/regist-user-form" class="btn btn-outline-warning w-100">호스트로 가입하기</a>
					</c:if>
					<c:if test="${sessionScope.role eq 'HOST' or sessionScope.role eq 'ADMIN'}">
						<button class="btn btn-secondary w-100" disabled>이미 호스트입니다</button>
					</c:if>
				</div>
			</div>
		</div>

		<div class="col-md-4">
			<div class="card shadow-sm">
				<div class="card-body text-center">
					<h5 class="card-title">관리자 페이지</h5>
					<p class="card-text">관리자 전용 대시보드로 이동합니다.</p>
					<c:if test="${sessionScope.role eq 'ADMIN'}">
						<a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-danger w-100">관리자 페이지</a>
					</c:if>
					<c:if test="${sessionScope.role ne 'ADMIN'}">
						<button class="btn btn-secondary w-100" disabled>관리자 전용</button>
					</c:if>
				</div>
			</div>
		</div>
	</div>

	<!-- 관리자 전용 기능 -->
	<c:if test="${sessionScope.role eq 'ADMIN'}">
		<div class="row justify-content-center mt-4">
			<h4 class="text-center mb-3">관리자 전용 기능</h4>

			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">사용자 관리</h5>
						<p class="card-text">사용자 목록을 확인하고 관리합니다.</p>
						<a href="${pageContext.request.contextPath}/admin/users" class="btn btn-primary w-100">사용자 관리</a>
					</div>
				</div>
			</div>

			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">숙소 관리</h5>
						<p class="card-text">등록된 모든 숙소를 관리합니다.</p>
						<a href="${pageContext.request.contextPath}/admin/accommodations" class="btn btn-success w-100">숙소 관리</a>
					</div>
				</div>
			</div>

			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">지역 데이터 관리</h5>
						<p class="card-text">지역 데이터를 관리합니다.</p>
						<a href="${pageContext.request.contextPath}/admin/regions" class="btn btn-info w-100">지역 관리</a>
					</div>
				</div>
			</div>
		</div>
	</c:if>


	<!-- 호스트 전용 기능 -->
	<c:if test="${sessionScope.role eq 'HOST' or sessionScope.role eq 'ADMIN'}">
		<div class="row justify-content-center mt-4">
			<h4 class="text-center mb-3">호스트 전용 기능</h4>

			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">내 호스트 정보</h5>
						<p class="card-text">호스트 정보를 확인하고 수정합니다.</p>
						<a href="${pageContext.request.contextPath}/host/detail/${sessionScope.userId}" class="btn btn-primary w-100">호스트 정보 관리</a>
					</div>
				</div>
			</div>

			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">내 숙소 관리</h5>
						<p class="card-text">등록한 숙소를 관리합니다.</p>
						<a href="${pageContext.request.contextPath}/accommodation/my-accommodations" class="btn btn-success w-100">숙소 관리</a>
					</div>
				</div>
			</div>

			<div class="col-md-4">
				<div class="card shadow-sm">
					<div class="card-body text-center">
						<h5 class="card-title">예약 관리</h5>
						<p class="card-text">숙소 예약 현황을 확인합니다.</p>
						<a href="${pageContext.request.contextPath}/reservation/host-reservations" class="btn btn-info w-100">예약 관리</a>
					</div>
				</div>
			</div>
		</div>
	</c:if>
</div>
<%@ include file="/WEB-INF/views/fragments/footer.jsp" %>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
