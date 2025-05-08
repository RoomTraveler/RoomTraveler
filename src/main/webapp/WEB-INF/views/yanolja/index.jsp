<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>야놀자 클론 - 국내 숙소 예약</title>
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

        .search-bar {
            background-color: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 20px;
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

        .category-icon {
            width: 60px;
            height: 60px;
            background-color: white;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            font-size: 1.5rem;
            color: var(--yanolja-red);
        }

        .category-item {
            text-align: center;
            margin-bottom: 15px;
        }

        .category-name {
            font-size: 0.9rem;
            color: #333;
        }

        .section-title {
            font-weight: bold;
            margin-bottom: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .section-title a {
            font-size: 0.9rem;
            color: var(--yanolja-dark-gray);
            text-decoration: none;
        }

        .accommodation-card {
            border-radius: 10px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 20px;
            background-color: white;
            transition: transform 0.3s;
        }

        .accommodation-card:hover {
            transform: translateY(-5px);
        }

        .card-img-top {
            height: 180px;
            object-fit: cover;
        }

        .card-body {
            padding: 15px;
        }

        .accommodation-type {
            font-size: 0.8rem;
            color: var(--yanolja-dark-gray);
            margin-bottom: 5px;
        }

        .accommodation-title {
            font-weight: bold;
            margin-bottom: 5px;
            font-size: 1.1rem;
        }

        .accommodation-location {
            font-size: 0.9rem;
            color: var(--yanolja-dark-gray);
            margin-bottom: 10px;
        }

        .accommodation-price {
            font-weight: bold;
            color: var(--yanolja-red);
            font-size: 1.2rem;
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

        .rating {
            color: #ffb700;
            font-weight: bold;
            margin-right: 5px;
        }

        .review-count {
            font-size: 0.8rem;
            color: var(--yanolja-dark-gray);
        }

        .banner {
            border-radius: 10px;
            overflow: hidden;
            margin-bottom: 20px;
        }

        .banner img {
            width: 100%;
            height: auto;
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
    </style>
</head>
<body>
    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg navbar-light navbar-yanolja">
        <div class="container">
            <a class="navbar-brand" href="#">야놀자</a>
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
        <!-- Search Bar -->
        <div class="search-bar">
            <form action="${pageContext.request.contextPath}/accommodation/filter" method="get">
                <div class="row g-3">
                    <div class="col-md-4">
                        <label for="sidoCode" class="form-label">지역</label>
                        <select class="form-select" id="sidoCode" name="sidoCode">
                            <option value="">지역을 선택하세요</option>
                            <!-- 시도 목록은 JavaScript로 동적 로드 -->
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label for="checkIn" class="form-label">체크인</label>
                        <input type="date" class="form-control" id="checkIn" name="checkIn">
                    </div>
                    <div class="col-md-3">
                        <label for="checkOut" class="form-label">체크아웃</label>
                        <input type="date" class="form-control" id="checkOut" name="checkOut">
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button type="submit" class="btn btn-yanolja w-100">검색</button>
                    </div>
                </div>
            </form>
        </div>

        <!-- Categories -->
        <div class="row mb-4">
            <div class="col-12">
                <h5 class="section-title">카테고리</h5>
            </div>
            <div class="col-3">
                <div class="category-item">
                    <div class="category-icon">
                        <i class="bi bi-building"></i>
                    </div>
                    <div class="category-name">호텔</div>
                </div>
            </div>
            <div class="col-3">
                <div class="category-item">
                    <div class="category-icon">
                        <i class="bi bi-house"></i>
                    </div>
                    <div class="category-name">펜션</div>
                </div>
            </div>
            <div class="col-3">
                <div class="category-item">
                    <div class="category-icon">
                        <i class="bi bi-water"></i>
                    </div>
                    <div class="category-name">풀빌라</div>
                </div>
            </div>
            <div class="col-3">
                <div class="category-item">
                    <div class="category-icon">
                        <i class="bi bi-shop"></i>
                    </div>
                    <div class="category-name">모텔</div>
                </div>
            </div>
        </div>

        <!-- Banner -->
        <div class="banner">
            <img src="https://via.placeholder.com/800x200/f0213b/ffffff?text=특가+프로모션" alt="프로모션 배너">
        </div>

        <!-- Today's Deals -->
        <div class="row mb-4">
            <div class="col-12">
                <h5 class="section-title">
                    오늘의 특가
                    <a href="${pageContext.request.contextPath}/accommodation/special-offers">더보기 <i class="bi bi-chevron-right"></i></a>
                </h5>
            </div>

            <!-- Sample accommodations - in real implementation, these would be populated from the database -->
            <div class="col-md-6 col-lg-4">
                <div class="accommodation-card">
                    <div class="position-relative">
                        <img src="https://via.placeholder.com/300x180" class="card-img-top" alt="숙소 이미지">
                        <span class="promotion-badge">특가 20%</span>
                    </div>
                    <div class="card-body">
                        <div class="accommodation-type">호텔</div>
                        <h5 class="accommodation-title">그랜드 호텔 서울</h5>
                        <div class="accommodation-location"><i class="bi bi-geo-alt"></i> 서울 강남구</div>
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <span class="rating">4.8</span>
                                <span class="review-count">(302)</span>
                            </div>
                            <div class="accommodation-price">
                                120,000원 <span class="price-unit">/ 1박</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6 col-lg-4">
                <div class="accommodation-card">
                    <div class="position-relative">
                        <img src="https://via.placeholder.com/300x180" class="card-img-top" alt="숙소 이미지">
                        <span class="promotion-badge">특가 15%</span>
                    </div>
                    <div class="card-body">
                        <div class="accommodation-type">펜션</div>
                        <h5 class="accommodation-title">바다뷰 펜션</h5>
                        <div class="accommodation-location"><i class="bi bi-geo-alt"></i> 부산 해운대구</div>
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <span class="rating">4.6</span>
                                <span class="review-count">(158)</span>
                            </div>
                            <div class="accommodation-price">
                                95,000원 <span class="price-unit">/ 1박</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6 col-lg-4">
                <div class="accommodation-card">
                    <div class="position-relative">
                        <img src="https://via.placeholder.com/300x180" class="card-img-top" alt="숙소 이미지">
                        <span class="promotion-badge">특가 30%</span>
                    </div>
                    <div class="card-body">
                        <div class="accommodation-type">풀빌라</div>
                        <h5 class="accommodation-title">프라이빗 풀빌라</h5>
                        <div class="accommodation-location"><i class="bi bi-geo-alt"></i> 제주 서귀포시</div>
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <span class="rating">4.9</span>
                                <span class="review-count">(87)</span>
                            </div>
                            <div class="accommodation-price">
                                250,000원 <span class="price-unit">/ 1박</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Recommended Accommodations -->
        <div class="row mb-4">
            <div class="col-12">
                <h5 class="section-title">
                    추천 숙소
                    <a href="${pageContext.request.contextPath}/accommodation/recommended">더보기 <i class="bi bi-chevron-right"></i></a>
                </h5>
            </div>

            <!-- Sample accommodations - in real implementation, these would be populated from the database -->
            <div class="col-md-6 col-lg-4">
                <div class="accommodation-card">
                    <img src="https://via.placeholder.com/300x180" class="card-img-top" alt="숙소 이미지">
                    <div class="card-body">
                        <div class="accommodation-type">호텔</div>
                        <h5 class="accommodation-title">시그니엘 서울</h5>
                        <div class="accommodation-location"><i class="bi bi-geo-alt"></i> 서울 송파구</div>
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <span class="rating">4.9</span>
                                <span class="review-count">(521)</span>
                            </div>
                            <div class="accommodation-price">
                                350,000원 <span class="price-unit">/ 1박</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6 col-lg-4">
                <div class="accommodation-card">
                    <img src="https://via.placeholder.com/300x180" class="card-img-top" alt="숙소 이미지">
                    <div class="card-body">
                        <div class="accommodation-type">리조트</div>
                        <h5 class="accommodation-title">알펜시아 리조트</h5>
                        <div class="accommodation-location"><i class="bi bi-geo-alt"></i> 강원 평창군</div>
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <span class="rating">4.7</span>
                                <span class="review-count">(342)</span>
                            </div>
                            <div class="accommodation-price">
                                180,000원 <span class="price-unit">/ 1박</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6 col-lg-4">
                <div class="accommodation-card">
                    <img src="https://via.placeholder.com/300x180" class="card-img-top" alt="숙소 이미지">
                    <div class="card-body">
                        <div class="accommodation-type">게스트하우스</div>
                        <h5 class="accommodation-title">서울 스테이</h5>
                        <div class="accommodation-location"><i class="bi bi-geo-alt"></i> 서울 마포구</div>
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <span class="rating">4.5</span>
                                <span class="review-count">(198)</span>
                            </div>
                            <div class="accommodation-price">
                                45,000원 <span class="price-unit">/ 1박</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Bottom Navigation -->
    <div class="bottom-nav">
        <div class="container">
            <div class="row">
                <div class="col-3">
                    <div class="bottom-nav-item active">
                        <div class="bottom-nav-icon"><i class="bi bi-house-fill"></i></div>
                        <div class="bottom-nav-text">홈</div>
                    </div>
                </div>
                <div class="col-3">
                    <div class="bottom-nav-item">
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
        // 현재 날짜 설정
        document.addEventListener('DOMContentLoaded', function() {
            const today = new Date();
            const tomorrow = new Date(today);
            tomorrow.setDate(tomorrow.getDate() + 1);

            const formatDate = (date) => {
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const day = String(date.getDate()).padStart(2, '0');
                return `${year}-${month}-${day}`;
            };

            document.getElementById('checkIn').value = formatDate(today);
            document.getElementById('checkOut').value = formatDate(tomorrow);

            // 시도 목록 로드
            loadSidos();
        });

        // 시도 목록 로드
        function loadSidos() {
            fetch('${pageContext.request.contextPath}/accommodation/api/sidos')
                .then(response => response.json())
                .then(data => {
                    const sidoSelect = document.getElementById('sidoCode');
                    data.forEach(sido => {
                        const option = document.createElement('option');
                        option.value = sido.code;
                        option.textContent = sido.name;
                        sidoSelect.appendChild(option);
                    });
                })
                .catch(error => console.error('Error loading sidos:', error));
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
    </script>
</body>
</html>
