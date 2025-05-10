<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${accommodation.title} - 야놀자 클론</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        :root {
            --yanolja-red: #f0213b;
            --yanolja-pink: #ff3478;
            --yanolja-light-gray: #f5f5f5;
            --yanolja-dark-gray: #666;
        }
        
        body {
            font-family: 'Noto Sans KR', sans-serif;
            color: #333;
            background-color: #f9f9f9;
        }
        
        .navbar-yanolja {
            background-color: white;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .navbar-brand {
            font-weight: bold;
            color: var(--yanolja-red) !important;
            font-size: 1.5rem;
        }
        
        .btn-yanolja {
            background-color: var(--yanolja-red);
            color: white;
            border: none;
        }
        
        .btn-yanolja:hover {
            background-color: #d01c33;
            color: white;
        }
        
        .carousel-item img {
            height: 300px;
            object-fit: cover;
        }
        
        .accommodation-info {
            background-color: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        
        .accommodation-title {
            font-size: 1.5rem;
            font-weight: bold;
            margin-bottom: 5px;
        }
        
        .accommodation-type {
            font-size: 0.9rem;
            color: var(--yanolja-dark-gray);
            margin-bottom: 10px;
        }
        
        .accommodation-location {
            font-size: 0.9rem;
            color: var(--yanolja-dark-gray);
            margin-bottom: 15px;
        }
        
        .rating-box {
            display: flex;
            align-items: center;
            margin-bottom: 15px;
        }
        
        .rating {
            color: #ffb700;
            font-weight: bold;
            font-size: 1.2rem;
            margin-right: 5px;
        }
        
        .review-count {
            font-size: 0.9rem;
            color: var(--yanolja-dark-gray);
        }
        
        .section-title {
            font-weight: bold;
            margin-bottom: 15px;
            font-size: 1.2rem;
        }
        
        .info-item {
            margin-bottom: 10px;
            display: flex;
            align-items: flex-start;
        }
        
        .info-icon {
            color: var(--yanolja-dark-gray);
            margin-right: 10px;
            font-size: 1.1rem;
            width: 20px;
            text-align: center;
        }
        
        .info-text {
            flex: 1;
        }
        
        .room-card {
            background-color: white;
            border-radius: 10px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 20px;
            transition: transform 0.3s;
        }
        
        .room-card:hover {
            transform: translateY(-5px);
        }
        
        .room-img {
            height: 180px;
            object-fit: cover;
            width: 100%;
        }
        
        .room-body {
            padding: 15px;
        }
        
        .room-title {
            font-weight: bold;
            margin-bottom: 5px;
            font-size: 1.1rem;
        }
        
        .room-info {
            font-size: 0.9rem;
            color: var(--yanolja-dark-gray);
            margin-bottom: 10px;
        }
        
        .room-price {
            font-weight: bold;
            color: var(--yanolja-red);
            font-size: 1.2rem;
            margin-bottom: 10px;
        }
        
        .price-unit {
            font-size: 0.8rem;
            font-weight: normal;
            color: var(--yanolja-dark-gray);
        }
        
        .promotion-badge {
            position: absolute;
            top: 10px;
            left: 10px;
            background-color: var(--yanolja-red);
            color: white;
            padding: 3px 8px;
            border-radius: 5px;
            font-size: 0.8rem;
            font-weight: bold;
        }
        
        .tab-bar {
            display: flex;
            background-color: white;
            border-radius: 10px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        
        .tab-item {
            flex: 1;
            text-align: center;
            padding: 15px 0;
            font-weight: bold;
            cursor: pointer;
            border-bottom: 3px solid transparent;
        }
        
        .tab-item.active {
            color: var(--yanolja-red);
            border-bottom-color: var(--yanolja-red);
        }
        
        .tab-content {
            display: none;
        }
        
        .tab-content.active {
            display: block;
        }
        
        .review-item {
            background-color: white;
            border-radius: 10px;
            padding: 15px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 15px;
        }
        
        .review-header {
            display: flex;
            justify-content: space-between;
            margin-bottom: 10px;
        }
        
        .reviewer-name {
            font-weight: bold;
        }
        
        .review-date {
            font-size: 0.8rem;
            color: var(--yanolja-dark-gray);
        }
        
        .review-rating {
            color: #ffb700;
            font-weight: bold;
            margin-bottom: 10px;
        }
        
        .review-text {
            font-size: 0.95rem;
            line-height: 1.5;
        }
        
        .map-container {
            height: 300px;
            background-color: #eee;
            border-radius: 10px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        
        .bottom-nav {
            position: fixed;
            bottom: 0;
            left: 0;
            right: 0;
            background-color: white;
            box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
            padding: 10px 0;
            z-index: 1000;
        }
        
        .bottom-nav-item {
            text-align: center;
            font-size: 0.8rem;
        }
        
        .bottom-nav-icon {
            font-size: 1.5rem;
            margin-bottom: 5px;
            color: var(--yanolja-dark-gray);
        }
        
        .bottom-nav-item.active .bottom-nav-icon,
        .bottom-nav-item.active .bottom-nav-text {
            color: var(--yanolja-red);
        }
        
        .main-content {
            margin-bottom: 70px; /* Space for bottom nav */
        }
        
        .booking-bar {
            position: fixed;
            bottom: 70px;
            left: 0;
            right: 0;
            background-color: white;
            box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
            padding: 15px;
            z-index: 999;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .booking-price {
            font-weight: bold;
            font-size: 1.2rem;
        }
        
        .booking-price-unit {
            font-size: 0.8rem;
            font-weight: normal;
            color: var(--yanolja-dark-gray);
        }
        
        .amenities-list {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 10px;
        }
        
        .amenity-item {
            display: flex;
            align-items: center;
        }
        
        .amenity-icon {
            color: var(--yanolja-dark-gray);
            margin-right: 10px;
            font-size: 1.1rem;
        }
    </style>
</head>
<body>
    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg navbar-light navbar-yanolja">
        <div class="container">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/yanolja">야놀자</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto">
                    <c:if test="${empty username}">
                        <li class="nav-item">
                            <a class="nav-link" href="/user/login-form">로그인</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="/user/regist-user-form">회원가입</a>
                        </li>
                    </c:if>
                    <c:if test="${not empty username}">
                        <li class="nav-item">
                            <a class="nav-link" href="/user/user-detail">${username}님</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="/user/logout">로그아웃</a>
                        </li>
                    </c:if>
                </ul>
            </div>
        </div>
    </nav>

    <div class="container main-content mt-4">
        <!-- Image Carousel -->
        <div id="accommodationCarousel" class="carousel slide mb-4" data-bs-ride="carousel">
            <div class="carousel-inner">
                <c:choose>
                    <c:when test="${not empty accommodation.mainImageUrl}">
                        <div class="carousel-item active">
                            <img src="${accommodation.mainImageUrl}" class="d-block w-100" alt="${accommodation.title}">
                        </div>
                        <c:forEach var="imageUrl" items="${accommodation.imageUrls}" varStatus="status">
                            <c:if test="${status.index > 0}">
                                <div class="carousel-item">
                                    <img src="${imageUrl}" class="d-block w-100" alt="${accommodation.title}">
                                </div>
                            </c:if>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="carousel-item active">
                            <img src="https://via.placeholder.com/800x300" class="d-block w-100" alt="이미지 없음">
                        </div>
                        <div class="carousel-item">
                            <img src="https://via.placeholder.com/800x300" class="d-block w-100" alt="이미지 없음">
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

        <!-- Accommodation Info -->
        <div class="accommodation-info">
            <div class="accommodation-type">
                <c:choose>
                    <c:when test="${not empty accommodation.accommodationType}">
                        ${accommodation.accommodationType}
                    </c:when>
                    <c:otherwise>
                        호텔
                    </c:otherwise>
                </c:choose>
            </div>
            <h1 class="accommodation-title">${accommodation.title}</h1>
            <div class="accommodation-location">
                <i class="bi bi-geo-alt"></i> 
                <c:choose>
                    <c:when test="${not empty accommodation.sidoName and not empty accommodation.gugunName}">
                        ${accommodation.sidoName} ${accommodation.gugunName}
                    </c:when>
                    <c:otherwise>
                        ${accommodation.address}
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="rating-box">
                <span class="rating">4.8</span>
                <span class="review-count">(302)</span>
            </div>
            
            <hr>
            
            <h3 class="section-title">기본 정보</h3>
            <div class="info-item">
                <div class="info-icon"><i class="bi bi-clock"></i></div>
                <div class="info-text">
                    체크인: ${accommodation.checkInTime} / 체크아웃: ${accommodation.checkOutTime}
                </div>
            </div>
            <div class="info-item">
                <div class="info-icon"><i class="bi bi-telephone"></i></div>
                <div class="info-text">
                    ${accommodation.phone}
                </div>
            </div>
            <div class="info-item">
                <div class="info-icon"><i class="bi bi-envelope"></i></div>
                <div class="info-text">
                    ${accommodation.email}
                </div>
            </div>
            <div class="info-item">
                <div class="info-icon"><i class="bi bi-globe"></i></div>
                <div class="info-text">
                    ${accommodation.website}
                </div>
            </div>
        </div>

        <!-- Tab Bar -->
        <div class="tab-bar">
            <div class="tab-item active" onclick="openTab('rooms')">객실 선택</div>
            <div class="tab-item" onclick="openTab('details')">상세 정보</div>
            <div class="tab-item" onclick="openTab('reviews')">리뷰</div>
            <div class="tab-item" onclick="openTab('location')">위치</div>
        </div>

        <!-- Tab Contents -->
        <div id="rooms" class="tab-content active">
            <c:if test="${empty rooms}">
                <div class="text-center py-5">
                    <p class="lead">등록된 객실이 없습니다.</p>
                </div>
            </c:if>
            
            <c:forEach var="room" items="${rooms}">
                <div class="room-card">
                    <div class="position-relative">
                        <c:choose>
                            <c:when test="${not empty room.mainImageUrl}">
                                <img src="${room.mainImageUrl}" class="room-img" alt="${room.name}">
                            </c:when>
                            <c:otherwise>
                                <img src="https://via.placeholder.com/400x180" class="room-img" alt="이미지 없음">
                            </c:otherwise>
                        </c:choose>
                        
                        <!-- 특가 표시 (실제 구현에서는 특가 정보가 있는 경우에만 표시) -->
                        <c:if test="${room.roomId % 3 == 0}">
                            <span class="promotion-badge">특가 20%</span>
                        </c:if>
                    </div>
                    <div class="room-body">
                        <h5 class="room-title">${room.name}</h5>
                        <div class="room-info">
                            <i class="bi bi-people"></i> 최대 ${room.capacity}인
                            <c:if test="${not empty room.bedType}">
                                &nbsp;&nbsp;|&nbsp;&nbsp;<i class="bi bi-door-closed"></i> ${room.bedType}
                            </c:if>
                        </div>
                        <div class="room-price">
                            <c:choose>
                                <c:when test="${not empty room.price}">
                                    <fmt:formatNumber value="${room.price}" pattern="#,###" />원
                                </c:when>
                                <c:otherwise>
                                    <fmt:formatNumber value="${100000 + room.roomId * 5000}" pattern="#,###" />원
                                </c:otherwise>
                            </c:choose>
                            <span class="price-unit">/ 1박</span>
                        </div>
                        <a href="${pageContext.request.contextPath}/yanolja/room/${room.roomId}" class="btn btn-yanolja w-100">객실 예약하기</a>
                    </div>
                </div>
            </c:forEach>
        </div>

        <div id="details" class="tab-content">
            <div class="accommodation-info">
                <h3 class="section-title">숙소 설명</h3>
                <p>${accommodation.description}</p>
                
                <hr>
                
                <h3 class="section-title">편의 시설</h3>
                <div class="amenities-list">
                    <c:if test="${not empty accommodation.amenities}">
                        <c:forEach var="amenity" items="${accommodation.amenities.split(',')}">
                            <div class="amenity-item">
                                <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                                <div>${amenity.trim()}</div>
                            </div>
                        </c:forEach>
                    </c:if>
                    <c:if test="${empty accommodation.amenities}">
                        <!-- 샘플 편의시설 -->
                        <div class="amenity-item">
                            <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                            <div>무료 Wi-Fi</div>
                        </div>
                        <div class="amenity-item">
                            <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                            <div>에어컨</div>
                        </div>
                        <div class="amenity-item">
                            <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                            <div>TV</div>
                        </div>
                        <div class="amenity-item">
                            <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                            <div>주차장</div>
                        </div>
                        <div class="amenity-item">
                            <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                            <div>수영장</div>
                        </div>
                        <div class="amenity-item">
                            <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                            <div>레스토랑</div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>

        <div id="reviews" class="tab-content">
            <!-- 샘플 리뷰 -->
            <div class="review-item">
                <div class="review-header">
                    <div class="reviewer-name">김철수</div>
                    <div class="review-date">2023.05.15</div>
                </div>
                <div class="review-rating">★★★★★ 5.0</div>
                <div class="review-text">
                    정말 좋은 숙소였습니다. 깨끗하고 직원들도 친절했어요. 다음에 또 방문하고 싶습니다.
                </div>
            </div>
            
            <div class="review-item">
                <div class="review-header">
                    <div class="reviewer-name">이영희</div>
                    <div class="review-date">2023.04.22</div>
                </div>
                <div class="review-rating">★★★★☆ 4.0</div>
                <div class="review-text">
                    전체적으로 만족스러웠습니다. 위치도 좋고 시설도 괜찮았어요. 다만 소음이 조금 있었던 점이 아쉬웠습니다.
                </div>
            </div>
            
            <div class="review-item">
                <div class="review-header">
                    <div class="reviewer-name">박지민</div>
                    <div class="review-date">2023.03.10</div>
                </div>
                <div class="review-rating">★★★★★ 5.0</div>
                <div class="review-text">
                    가족 여행으로 방문했는데 아이들도 너무 좋아했어요. 특히 조식이 맛있었습니다. 다음에 또 이용할 예정입니다.
                </div>
            </div>
        </div>

        <div id="location" class="tab-content">
            <div class="map-container">
                <p class="text-center">지도가 여기에 표시됩니다.</p>
            </div>
            
            <div class="accommodation-info">
                <h3 class="section-title">주소</h3>
                <p>${accommodation.address}</p>
                
                <h3 class="section-title">주변 정보</h3>
                <div class="info-item">
                    <div class="info-icon"><i class="bi bi-train-front"></i></div>
                    <div class="info-text">
                        가장 가까운 지하철역: 강남역 (2호선) - 도보 5분
                    </div>
                </div>
                <div class="info-item">
                    <div class="info-icon"><i class="bi bi-airplane"></i></div>
                    <div class="info-text">
                        인천국제공항 - 차량으로 60분
                    </div>
                </div>
                <div class="info-item">
                    <div class="info-icon"><i class="bi bi-shop"></i></div>
                    <div class="info-text">
                        주변 쇼핑몰: 코엑스몰 - 도보 15분
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Booking Bar -->
    <div class="booking-bar">
        <div class="booking-price">
            <c:choose>
                <c:when test="${not empty rooms and not empty rooms[0].price}">
                    <fmt:formatNumber value="${rooms[0].price}" pattern="#,###" />원
                </c:when>
                <c:otherwise>
                    100,000원
                </c:otherwise>
            </c:choose>
            <span class="booking-price-unit">/ 1박</span>
        </div>
        <a href="#rooms" class="btn btn-yanolja">객실 선택하기</a>
    </div>

    <!-- Bottom Navigation -->
    <div class="bottom-nav">
        <div class="container">
            <div class="row">
                <div class="col-3">
                    <div class="bottom-nav-item">
                        <div class="bottom-nav-icon"><i class="bi bi-house"></i></div>
                        <div class="bottom-nav-text">홈</div>
                    </div>
                </div>
                <div class="col-3">
                    <div class="bottom-nav-item active">
                        <div class="bottom-nav-icon"><i class="bi bi-search"></i></div>
                        <div class="bottom-nav-text">검색</div>
                    </div>
                </div>
                <div class="col-3">
                    <div class="bottom-nav-item">
                        <div class="bottom-nav-icon"><i class="bi bi-heart"></i></div>
                        <div class="bottom-nav-text">찜</div>
                    </div>
                </div>
                <div class="col-3">
                    <div class="bottom-nav-item">
                        <div class="bottom-nav-icon"><i class="bi bi-person"></i></div>
                        <div class="bottom-nav-text">마이</div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 탭 전환 함수
        function openTab(tabName) {
            // 모든 탭 콘텐츠 숨기기
            const tabContents = document.getElementsByClassName('tab-content');
            for (let i = 0; i < tabContents.length; i++) {
                tabContents[i].classList.remove('active');
            }
            
            // 모든 탭 아이템 비활성화
            const tabItems = document.getElementsByClassName('tab-item');
            for (let i = 0; i < tabItems.length; i++) {
                tabItems[i].classList.remove('active');
            }
            
            // 선택한 탭 콘텐츠 표시
            document.getElementById(tabName).classList.add('active');
            
            // 선택한 탭 아이템 활성화
            const activeTab = document.querySelector(`.tab-item[onclick="openTab('${tabName}')"]`);
            activeTab.classList.add('active');
        }
        
        // 바텀 네비게이션 아이템 클릭 이벤트
        document.querySelectorAll('.bottom-nav-item').forEach(item => {
            item.addEventListener('click', function() {
                document.querySelectorAll('.bottom-nav-item').forEach(i => {
                    i.classList.remove('active');
                });
                this.classList.add('active');
            });
        });
        
        // 객실 선택하기 버튼 클릭 시 객실 탭으로 이동
        document.querySelector('.booking-bar a').addEventListener('click', function(e) {
            e.preventDefault();
            openTab('rooms');
            window.scrollTo({
                top: document.querySelector('.tab-bar').offsetTop - 20,
                behavior: 'smooth'
            });
        });
    </script>
</body>
</html>