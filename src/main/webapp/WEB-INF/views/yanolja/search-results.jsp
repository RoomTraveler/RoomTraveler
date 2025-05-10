<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>검색 결과 - 야놀자 클론</title>
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
        
        .filter-bar {
            background-color: white;
            border-radius: 10px;
            padding: 15px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 20px;
            display: flex;
            overflow-x: auto;
            white-space: nowrap;
            -webkit-overflow-scrolling: touch;
        }
        
        .filter-item {
            display: inline-block;
            padding: 8px 15px;
            margin-right: 10px;
            border-radius: 20px;
            background-color: var(--yanolja-light-gray);
            color: var(--yanolja-dark-gray);
            font-size: 0.9rem;
            cursor: pointer;
        }
        
        .filter-item.active {
            background-color: var(--yanolja-red);
            color: white;
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
        
        .search-header {
            background-color: white;
            padding: 15px;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        
        .search-term {
            font-weight: bold;
            color: var(--yanolja-red);
        }
        
        .sort-options {
            display: flex;
            justify-content: space-between;
            margin-bottom: 15px;
        }
        
        .sort-option {
            font-size: 0.9rem;
            color: var(--yanolja-dark-gray);
            cursor: pointer;
        }
        
        .sort-option.active {
            color: var(--yanolja-red);
            font-weight: bold;
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
        <!-- Search Header -->
        <div class="search-header">
            <h4 class="mb-0">
                <span class="search-term">"${keyword}"</span> 검색 결과
                <c:if test="${not empty accommodations}">
                    <small class="text-muted">(${accommodations.size()}개)</small>
                </c:if>
            </h4>
        </div>
        
        <!-- Search Bar -->
        <div class="search-bar">
            <form action="${pageContext.request.contextPath}/yanolja/search" method="get">
                <div class="input-group">
                    <input type="text" class="form-control" name="keyword" placeholder="지역, 숙소명, 주소 검색" value="${keyword}">
                    <button class="btn btn-yanolja" type="submit">
                        <i class="bi bi-search"></i> 검색
                    </button>
                </div>
            </form>
        </div>

        <!-- Filter Bar -->
        <div class="filter-bar">
            <div class="filter-item active">전체</div>
            <div class="filter-item">호텔</div>
            <div class="filter-item">펜션</div>
            <div class="filter-item">풀빌라</div>
            <div class="filter-item">모텔</div>
            <div class="filter-item">게스트하우스</div>
            <div class="filter-item">리조트</div>
            <div class="filter-item">한옥</div>
        </div>

        <!-- Sort Options -->
        <div class="sort-options">
            <div class="sort-option active">추천순</div>
            <div class="sort-option">낮은 가격순</div>
            <div class="sort-option">높은 가격순</div>
            <div class="sort-option">평점 높은순</div>
            <div class="sort-option">리뷰 많은순</div>
        </div>

        <!-- Accommodations List -->
        <div class="row">
            <c:if test="${empty accommodations}">
                <div class="col-12 text-center py-5">
                    <p class="lead">검색 결과가 없습니다.</p>
                    <p>다른 키워드로 검색해보세요.</p>
                </div>
            </c:if>
            
            <c:forEach var="accommodation" items="${accommodations}">
                <div class="col-md-6 col-lg-4">
                    <div class="accommodation-card">
                        <div class="position-relative">
                            <c:choose>
                                <c:when test="${not empty accommodation.mainImageUrl}">
                                    <img src="${accommodation.mainImageUrl}" class="card-img-top" alt="${accommodation.title}">
                                </c:when>
                                <c:otherwise>
                                    <img src="https://via.placeholder.com/300x180" class="card-img-top" alt="이미지 없음">
                                </c:otherwise>
                            </c:choose>
                            
                            <!-- 특가 표시 (실제 구현에서는 특가 정보가 있는 경우에만 표시) -->
                            <c:if test="${accommodation.accommodationId % 3 == 0}">
                                <span class="promotion-badge">특가 20%</span>
                            </c:if>
                        </div>
                        <div class="card-body">
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
                            <h5 class="accommodation-title">${accommodation.title}</h5>
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
                            <div class="d-flex justify-content-between align-items-center">
                                <div>
                                    <!-- 평점 표시 (실제 구현에서는 실제 평점 사용) -->
                                    <span class="rating">4.${accommodation.accommodationId % 10}</span>
                                    <span class="review-count">(${accommodation.accommodationId * 3 + 50})</span>
                                </div>
                                <div class="accommodation-price">
                                    <!-- 가격 표시 (실제 구현에서는 실제 가격 사용) -->
                                    <fmt:formatNumber value="${80000 + accommodation.accommodationId * 5000}" pattern="#,###" />원
                                    <span class="price-unit">/ 1박</span>
                                </div>
                            </div>
                        </div>
                        <a href="${pageContext.request.contextPath}/yanolja/accommodation/${accommodation.accommodationId}" class="stretched-link"></a>
                    </div>
                </div>
            </c:forEach>
        </div>
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
        // 필터 아이템 클릭 이벤트
        document.querySelectorAll('.filter-item').forEach(item => {
            item.addEventListener('click', function() {
                document.querySelectorAll('.filter-item').forEach(i => {
                    i.classList.remove('active');
                });
                this.classList.add('active');
            });
        });
        
        // 정렬 옵션 클릭 이벤트
        document.querySelectorAll('.sort-option').forEach(item => {
            item.addEventListener('click', function() {
                document.querySelectorAll('.sort-option').forEach(i => {
                    i.classList.remove('active');
                });
                this.classList.add('active');
            });
        });
        
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