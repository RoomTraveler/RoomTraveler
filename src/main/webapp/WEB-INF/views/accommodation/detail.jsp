<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${accommodation.title} - 숙소 상세</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        .carousel-item img {
            height: 400px;
            object-fit: cover;
        }
        .room-card {
            transition: transform 0.3s;
            margin-bottom: 20px;
            height: 100%;
        }
        .room-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        }
        .card-img-top {
            height: 200px;
            object-fit: cover;
        }
        .amenities-list {
            columns: 2;
        }
        .review-card {
            margin-bottom: 15px;
        }
        .star-rating {
            color: #ffc107;
        }
        .booking-card {
            position: sticky;
            top: 20px;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />

    <div class="container mt-5">
        <c:if test="${not empty message}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <!-- 숙소 정보 -->
        <div class="row mb-5">
            <!-- 이미지 캐러셀 -->
            <div class="col-md-8">
                <div id="accommodationCarousel" class="carousel slide" data-bs-ride="carousel">
                    <div class="carousel-inner">
                        <c:choose>
                            <c:when test="${not empty accommodation.mainImageUrl}">
                                <div class="carousel-item active">
                                    <img src="${accommodation.mainImageUrl}" class="d-block w-100" alt="${accommodation.title}">
                                </div>
                                <!-- 추가 이미지가 있다면 여기에 추가 -->
                            </c:when>
                            <c:otherwise>
                                <div class="carousel-item active">
                                    <img src="${pageContext.request.contextPath}/resources/images/no-image.jpg" class="d-block w-100" alt="이미지 없음">
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <button class="carousel-control-prev" type="button" data-bs-target="#accommodationCarousel" data-bs-slide="prev">
                        <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                        <span class="visually-hidden">Previous</span>
                    </button>
                    <button class="carousel-control-next" type="button" data-bs-target="#accommodationCarousel" data-bs-slide="next">
                        <span class="carousel-control-next-icon" aria-hidden="true"></span>
                        <span class="visually-hidden">Next</span>
                    </button>
                </div>
            </div>

            <!-- 숙소 기본 정보 -->
            <div class="col-md-4">
                <h2>${accommodation.title}</h2>
                <p class="text-muted">${accommodation.sidoName} ${accommodation.gugunName}</p>
                <p><i class="bi bi-geo-alt"></i> ${accommodation.address}</p>
                <p><i class="bi bi-telephone"></i> ${accommodation.phone}</p>
                <c:if test="${not empty accommodation.email}">
                    <p><i class="bi bi-envelope"></i> ${accommodation.email}</p>
                </c:if>
                <c:if test="${not empty accommodation.website}">
                    <p><i class="bi bi-globe"></i> <a href="${accommodation.website}" target="_blank">${accommodation.website}</a></p>
                </c:if>
                <p><i class="bi bi-clock"></i> 체크인: ${accommodation.checkInTime} / 체크아웃: ${accommodation.checkOutTime}</p>

                <!-- 호스트 정보 -->
                <div class="mt-4">
                    <h5>호스트 정보</h5>
                    <p><i class="bi bi-person"></i> ${accommodation.hostName}</p>
                </div>

                <!-- 호스트인 경우 수정/삭제 버튼 표시 -->
                <c:if test="${sessionScope.userId == accommodation.hostId}">
                    <div class="mt-4">
                        <a href="${pageContext.request.contextPath}/accommodation/update-form/${accommodation.accommodationId}" class="btn btn-outline-primary me-2">수정</a>
                        <a href="${pageContext.request.contextPath}/accommodation/delete/${accommodation.accommodationId}" class="btn btn-outline-danger" onclick="return confirm('정말 삭제하시겠습니까?')">삭제</a>
                        <a href="${pageContext.request.contextPath}/accommodation/register-room-form/${accommodation.accommodationId}" class="btn btn-success mt-2 w-100">객실 등록</a>
                    </div>
                </c:if>
            </div>
        </div>

        <!-- 숙소 상세 설명 -->
        <div class="row mb-5">
            <div class="col-12">
                <div class="card">
                    <div class="card-body">
                        <h4 class="card-title">숙소 설명</h4>
                        <p class="card-text">${accommodation.description}</p>

                        <h5 class="mt-4">편의시설</h5>
                        <c:if test="${not empty accommodation.amenities}">
                            <ul class="amenities-list">
                                <c:forEach var="amenity" items="${accommodation.amenities.split(',')}">
                                    <li><i class="bi bi-check-circle-fill text-success"></i> ${amenity.trim()}</li>
                                </c:forEach>
                            </ul>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>

        <!-- 객실 목록 -->
        <h3 class="mb-4">객실 목록</h3>
        <div class="row mb-5">
            <c:if test="${empty rooms}">
                <div class="col-12 text-center py-5">
                    <p class="lead">등록된 객실이 없습니다.</p>
                </div>
            </c:if>

            <c:forEach var="room" items="${rooms}">
                <div class="col-md-4 mb-4">
                    <div class="card room-card">
                        <c:choose>
                            <c:when test="${not empty room.mainImageUrl}">
                                <img src="${room.mainImageUrl}" class="card-img-top" alt="${room.name}">
                            </c:when>
                            <c:otherwise>
                                <img src="${pageContext.request.contextPath}/resources/images/no-image.jpg" class="card-img-top" alt="이미지 없음">
                            </c:otherwise>
                        </c:choose>
                        <div class="card-body">
                            <h5 class="card-title">${room.name}</h5>
                            <p class="card-text">
                                <i class="bi bi-people"></i> 최대 ${room.capacity}인<br>
                                <i class="bi bi-currency-dollar"></i> <fmt:formatNumber value="${room.price}" type="currency" currencySymbol="₩" maxFractionDigits="0"/>
                            </p>
                            <div class="d-grid gap-2">
                                <a href="${pageContext.request.contextPath}/accommodation/room/${room.roomId}" class="btn btn-outline-primary">상세 보기</a>
                                <button class="btn btn-primary" onclick="showReservationForm(${room.roomId})">예약하기</button>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>

        <!-- 리뷰 섹션 -->
        <h3 class="mb-4">리뷰</h3>
        <div class="row mb-5">
            <div class="col-12">
                <div id="reviewsContainer">
                    <!-- 리뷰는 JavaScript로 동적 로드 -->
                    <div class="text-center py-3">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Loading...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 위치 정보 -->
        <h3 class="mb-4">위치</h3>
        <div class="row mb-5">
            <div class="col-12">
                <div id="map" style="width:100%; height:400px;"></div>
            </div>
        </div>
    </div>

    <!-- 예약 모달 -->
    <div class="modal fade" id="reservationModal" tabindex="-1" aria-labelledby="reservationModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="reservationModalLabel">예약하기</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <form id="reservationForm" action="${pageContext.request.contextPath}/reservation/form/" method="get">
                        <input type="hidden" id="roomIdInput" name="roomId">
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label for="checkInDate" class="form-label">체크인 날짜</label>
                                <input type="date" class="form-control" id="checkInDate" name="checkInDate" required>
                            </div>
                            <div class="col-md-6">
                                <label for="checkOutDate" class="form-label">체크아웃 날짜</label>
                                <input type="date" class="form-control" id="checkOutDate" name="checkOutDate" required>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label for="guestCount" class="form-label">인원 수</label>
                            <input type="number" class="form-control" id="guestCount" name="guestCount" min="1" value="1" required>
                        </div>
                        <div class="d-grid">
                            <button type="submit" class="btn btn-primary">예약 계속하기</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="../fragments/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_KAKAO_MAP_API_KEY&libraries=services"></script>
    <script>
        // 예약 모달 표시
        function showReservationForm(roomId) {
            document.getElementById('roomIdInput').value = roomId;

            // 오늘 날짜 설정
            const today = new Date();
            const tomorrow = new Date(today);
            tomorrow.setDate(tomorrow.getDate() + 1);

            // 날짜 포맷 변환 (YYYY-MM-DD)
            const formatDate = (date) => {
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const day = String(date.getDate()).padStart(2, '0');
                return `${year}-${month}-${day}`;
            };

            document.getElementById('checkInDate').value = formatDate(today);
            document.getElementById('checkInDate').min = formatDate(today);
            document.getElementById('checkOutDate').value = formatDate(tomorrow);
            document.getElementById('checkOutDate').min = formatDate(tomorrow);

            const reservationModal = new bootstrap.Modal(document.getElementById('reservationModal'));
            reservationModal.show();
        }

        // 리뷰 로드
        function loadReviews() {
            fetch('${pageContext.request.contextPath}/accommodation/api/reviews?accommodationId=${accommodation.accommodationId}')
                .then(response => response.json())
                .then(data => {
                    const reviewsContainer = document.getElementById('reviewsContainer');
                    reviewsContainer.innerHTML = '';

                    if (data.length === 0) {
                        reviewsContainer.innerHTML = '<p class="text-center">아직 리뷰가 없습니다.</p>';
                        return;
                    }

                    // 평균 평점 계산
                    const totalRating = data.reduce((sum, review) => sum + review.rating, 0);
                    const averageRating = totalRating / data.length;

                    // 평균 평점 표시
                    const ratingDiv = document.createElement('div');
                    ratingDiv.className = 'mb-4';
                    ratingDiv.innerHTML = `
                        <h4>평균 평점: <span class="star-rating">${averageRating.toFixed(1)}</span> / 5.0</h4>
                        <p>총 ${data.length}개의 리뷰</p>
                    `;
                    reviewsContainer.appendChild(ratingDiv);

                    // 리뷰 목록 표시
                    data.forEach(review => {
                        const reviewCard = document.createElement('div');
                        reviewCard.className = 'card review-card';

                        // 별점 표시
                        let stars = '';
                        for (let i = 0; i < 5; i++) {
                            if (i < Math.floor(review.rating)) {
                                stars += '<i class="bi bi-star-fill star-rating"></i>';
                            } else if (i < review.rating) {
                                stars += '<i class="bi bi-star-half star-rating"></i>';
                            } else {
                                stars += '<i class="bi bi-star star-rating"></i>';
                            }
                        }

                        reviewCard.innerHTML = `
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <h5 class="card-title">${review.userName}</h5>
                                    <div>${stars} ${review.rating.toFixed(1)}</div>
                                </div>
                                <p class="card-text">${review.comment}</p>
                                <p class="card-text"><small class="text-muted">작성일: \${new Date(review.createdAt).toLocaleDateString()}</small></p>
                            </div>
                        `;

                        reviewsContainer.appendChild(reviewCard);
                    });
                })
                .catch(error => {
                    console.error('Error loading reviews:', error);
                    document.getElementById('reviewsContainer').innerHTML = '<p class="text-center text-danger">리뷰를 불러오는 중 오류가 발생했습니다.</p>';
                });
        }

        // 지도 초기화
        function initMap() {
            const latitude = ${accommodation.latitude != null ? accommodation.latitude : 37.5665};
            const longitude = ${accommodation.longitude != null ? accommodation.longitude : 126.9780};

            const mapContainer = document.getElementById('map');
            const mapOption = {
                center: new kakao.maps.LatLng(latitude, longitude),
                level: 3
            };

            const map = new kakao.maps.Map(mapContainer, mapOption);

            // 마커 생성
            const marker = new kakao.maps.Marker({
                position: new kakao.maps.LatLng(latitude, longitude)
            });

            // 마커 지도에 표시
            marker.setMap(map);

            // 인포윈도우 생성
            const infowindow = new kakao.maps.InfoWindow({
                content: '<div style="padding:5px;font-size:12px;">${accommodation.title}</div>'
            });

            // 인포윈도우 표시
            infowindow.open(map, marker);
        }

        // 페이지 로드 시 실행
        document.addEventListener('DOMContentLoaded', function() {
            // 리뷰 로드
            loadReviews();

            // 지도 초기화
            // initMap(); // Kakao Maps API 키가 필요합니다

            // 체크아웃 날짜 유효성 검사
            document.getElementById('checkInDate').addEventListener('change', function() {
                const checkInDate = new Date(this.value);
                const checkOutDate = document.getElementById('checkOutDate');

                // 체크아웃 날짜가 체크인 날짜보다 이전이면 체크인 다음날로 설정
                const minCheckOutDate = new Date(checkInDate);
                minCheckOutDate.setDate(minCheckOutDate.getDate() + 1);

                const formatDate = (date) => {
                    const year = date.getFullYear();
                    const month = String(date.getMonth() + 1).padStart(2, '0');
                    const day = String(date.getDate()).padStart(2, '0');
                    return `${year}-${month}-${day}`;
                };

                checkOutDate.min = formatDate(minCheckOutDate);

                if (new Date(checkOutDate.value) <= checkInDate) {
                    checkOutDate.value = formatDate(minCheckOutDate);
                }
            });
        });
    </script>
</body>
</html>
