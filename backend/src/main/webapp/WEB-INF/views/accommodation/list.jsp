<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>숙박 지역 선택 - 야놀자 스타일</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
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

        .modal {
            display: none;
            position: fixed;
            z-index: 1;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            overflow: auto;
            background-color: rgba(0,0,0,0.4);
        }

        .modal-content {
            background-color: #fefefe;
            margin: 5% auto;
            padding: 20px;
            border: 1px solid #888;
            width: 80%;
            max-width: 1000px;
            border-radius: 5px;
        }

        .close {
            color: #aaa;
            float: right;
            font-size: 28px;
            font-weight: bold;
        }

        .close:hover,
        .close:focus {
            color: black;
            text-decoration: none;
            cursor: pointer;
        }

        .room-list {
            margin-top: 20px;
        }

        .room-card {
            border: 1px solid #ddd;
            border-radius: 5px;
            padding: 15px;
            margin-bottom: 15px;
        }

        .room-card h4 {
            margin-top: 0;
            margin-bottom: 10px;
        }

        .room-card img {
            max-width: 300px;
            max-height: 200px;
            margin-right: 15px;
            float: left;
        }

        .room-details {
            overflow: hidden;
        }

        .room-details p {
            margin: 5px 0;
        }

        .room-facilities {
            margin-top: 10px;
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
        }

        .facility {
            background-color: #f0f0f0;
            padding: 3px 8px;
            border-radius: 3px;
            font-size: 0.9em;
        }

        .pagination {
            display: flex;
            justify-content: center;
            margin-top: 20px;
        }

        .pagination button {
            margin: 0 5px;
            padding: 5px 10px;
            background-color: #f5f5f5;
            border: 1px solid #ddd;
            cursor: pointer;
        }

        .pagination button.active {
            background-color: var(--yanolja-red);
            color: white;
            border-color: var(--yanolja-red);
        }

        .loading {
            text-align: center;
            padding: 20px;
            display: none;
        }

        .tab-container {
            margin-top: 20px;
        }

        .tab {
            overflow: hidden;
            border: 1px solid #ccc;
            background-color: #f1f1f1;
            border-radius: 10px 10px 0 0;
        }

        .tab button {
            background-color: inherit;
            float: left;
            border: none;
            outline: none;
            cursor: pointer;
            padding: 10px 16px;
            transition: 0.3s;
        }

        .tab button:hover {
            background-color: #ddd;
        }

        .tab button.active {
            background-color: var(--yanolja-red);
            color: white;
        }

        .tabcontent {
            display: none;
            padding: 20px;
            border: 1px solid #ccc;
            border-top: none;
            border-radius: 0 0 10px 10px;
        }

        .image-gallery {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
        }

        .image-gallery img {
            width: 200px;
            height: 150px;
            object-fit: cover;
            cursor: pointer;
            border-radius: 5px;
        }
    </style>
</head>
<body>
    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg navbar-light navbar-yanolja">
        <div class="container">
            <a class="navbar-brand" href="#">방구석 여행자</a>
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
            <form action="${pageContext.request.contextPath}/accommodation" method="get" class="row g-3">
                <div class="col-md-3">
                    <label for="sidoCode" class="form-label">지역</label>
                    <select class="form-select" id="sidoCode" name="sidoCode">
                        <option value="">전체</option>
                        <!-- 시도 목록은 JavaScript로 동적 로드 -->
                    </select>
                </div>
                <div class="col-md-3">
                    <label for="gugunCode" class="form-label">시군구</label>
                    <select class="form-select" id="gugunCode" name="gugunCode">
                        <option value="">전체</option>
                        <!-- 구군 목록은 시도 선택 시 JavaScript로 동적 로드 -->
                    </select>
                </div>
                <div class="col-md-3">
                    <label for="keyword" class="form-label">검색어</label>
                    <input type="text" class="form-control" id="keyword" name="keyword" placeholder="숙소명, 주소 등" value="${keyword}">
                </div>
                <div class="col-md-3">
                    <label for="sortBy" class="form-label">정렬</label>
                    <select class="form-select" id="sortBy" name="sortBy">
                        <option value="createdAt">최신순</option>
                        <option value="price">가격순</option>
                        <option value="rating">평점순</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label for="minPrice" class="form-label">최소 가격</label>
                    <input type="number" class="form-control" id="minPrice" name="minPrice" placeholder="최소 가격">
                </div>
                <div class="col-md-3">
                    <label for="maxPrice" class="form-label">최대 가격</label>
                    <input type="number" class="form-control" id="maxPrice" name="maxPrice" placeholder="최대 가격">
                </div>
                <div class="col-md-3 d-flex align-items-end">
                    <button type="submit" class="btn btn-yanolja w-100">검색</button>
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

        <!-- 숙소 목록 -->
        <div class="row">
            <c:if test="${empty accommodations}">
                <div class="col-12 text-center py-5">
                    <p class="lead">검색 결과가 없습니다.</p>
                </div>
            </c:if>

            <c:forEach var="accommodation" items="${accommodations}">
                <div class="col-md-4 mb-4">
                    <div class="card accommodation-card">
                        <c:choose>
                            <c:when test="${not empty accommodation.mainImageUrl}">
                                <img src="${accommodation.mainImageUrl}" class="card-img-top" alt="${accommodation.title}">
                            </c:when>
                            <c:otherwise>
                                <img src="${pageContext.request.contextPath}/resources/images/no-image.jpg" class="card-img-top" alt="이미지 없음">
                            </c:otherwise>
                        </c:choose>
                        <div class="card-body">
                            <h5 class="card-title">${accommodation.title}</h5>
                            <p class="card-text text-muted">${accommodation.sidoName} ${accommodation.gugunName}</p>
                            <p class="card-text">${accommodation.address}</p>
                            <div class="d-flex justify-content-between align-items-center">
                                <a href="${pageContext.request.contextPath}/accommodation/detail/${accommodation.accommodationId}" class="btn btn-outline-primary">상세 보기</a>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>

        <div id="accommodationModal" class="modal">
            <div class="modal-content">
                <span class="close">&times;</span>
                <div id="modalContent">
                    <h2 id="modalTitle"></h2>
                    <div id="modalBasicInfo"></div>

                    <div class="tab-container">
                        <div class="tab">
                            <button class="tablinks active" onclick="openTab(event, 'roomInfo')">객실 정보</button>
                            <button class="tablinks" onclick="openTab(event, 'facilityInfo')">시설 정보</button>
                            <button class="tablinks" onclick="openTab(event, 'imageInfo')">이미지</button>
                        </div>

                        <div id="roomInfo" class="tabcontent" style="display: block;">
                            <div class="room-list" id="roomList"></div>
                        </div>

                        <div id="facilityInfo" class="tabcontent">
                            <div id="facilityDetails"></div>
                        </div>

                        <div id="imageInfo" class="tabcontent">
                            <div class="image-gallery" id="imageGallery"></div>
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
                        <div class="bottom-nav-icon"><i class="bi bi-house"></i></div>
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

<script>
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
                    if (sido.code == '${sidoCode}') {
                        option.selected = true;
                    }
                    sidoSelect.appendChild(option);
                });

                // 시도가 선택되어 있으면 구군 목록 로드
                if ('${sidoCode}') {
                    loadGuguns('${sidoCode}');
                }
            })
            .catch(error => console.error('Error loading sidos:', error));
    }

    // 구군 목록 로드
    function loadGuguns(sidoCode) {
        fetch(`${pageContext.request.contextPath}/accommodation/api/guguns?sido=${sidoCode}`)
            .then(response => response.json())
            .then(data => {
                const gugunSelect = document.getElementById('gugunCode');
                gugunSelect.innerHTML = '<option value="">전체</option>';

                data.forEach(gugun => {
                    const option = document.createElement('option');
                    option.value = gugun.code;
                    option.textContent = gugun.name;
                    if (gugun.code == '${gugunCode}') {
                        option.selected = true;
                    }
                    gugunSelect.appendChild(option);
                });
            })
            .catch(error => console.error('Error loading guguns:', error));
    }

    // 시도 선택 시 구군 목록 로드
    document.getElementById('sidoCode').addEventListener('change', function() {
        const sidoCode = this.value;
        if (sidoCode) {
            loadGuguns(sidoCode);
        } else {
            document.getElementById('gugunCode').innerHTML = '<option value="">전체</option>';
        }
    });

    // 페이지 로드 시 시도 목록 로드
    document.addEventListener('DOMContentLoaded', function() {
        loadSidos();

        // 정렬 옵션 설정
        const sortBy = '${param.sortBy}';
        if (sortBy) {
            document.getElementById('sortBy').value = sortBy;
        }

        // 가격 범위 설정
        const minPrice = '${param.minPrice}';
        const maxPrice = '${param.maxPrice}';
        if (minPrice) {
            document.getElementById('minPrice').value = minPrice;
        }
        if (maxPrice) {
            document.getElementById('maxPrice').value = maxPrice;
        }
    });

    // 모달 관련 코드
    // 모달 닫기
    $('.close').click(function() {
        $('#accommodationModal').css('display', 'none');
    });

    // 모달 외부 클릭 시 닫기
    $(window).click(function(event) {
        if (event.target === document.getElementById('accommodationModal')) {
            $('#accommodationModal').css('display', 'none');
        }
    });

    // 탭 전환 함수
    function openTab(evt, tabName) {
        $('.tabcontent').css('display', 'none');
        $('.tablinks').removeClass('active');
        $('#' + tabName).css('display', 'block');
        $(evt.currentTarget).addClass('active');
    }
</script>
</body>
</html>
