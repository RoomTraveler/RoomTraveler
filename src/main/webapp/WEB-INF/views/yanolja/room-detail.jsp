<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${room.name} - 야놀자 클론</title>
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

        .room-info {
            background-color: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }

        .room-title {
            font-size: 1.5rem;
            font-weight: bold;
            margin-bottom: 5px;
        }

        .room-type {
            font-size: 0.9rem;
            color: var(--yanolja-dark-gray);
            margin-bottom: 10px;
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

        .calendar {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
        }

        .calendar th, .calendar td {
            border: 1px solid #dee2e6;
            padding: 10px;
            text-align: center;
        }

        .calendar th {
            background-color: var(--yanolja-light-gray);
            font-weight: bold;
        }

        .calendar .available {
            background-color: #d4edda;
            cursor: pointer;
        }

        .calendar .unavailable {
            background-color: #f8d7da;
            color: #721c24;
            text-decoration: line-through;
        }

        .calendar .selected {
            background-color: var(--yanolja-red);
            color: white;
            font-weight: bold;
        }

        .calendar .today {
            font-weight: bold;
            border: 2px solid #0d6efd;
        }

        .reservation-form {
            background-color: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }

        .price-breakdown {
            background-color: var(--yanolja-light-gray);
            border-radius: 10px;
            padding: 15px;
            margin-top: 20px;
        }

        .price-item {
            display: flex;
            justify-content: space-between;
            margin-bottom: 10px;
        }

        .price-total {
            display: flex;
            justify-content: space-between;
            font-weight: bold;
            margin-top: 10px;
            padding-top: 10px;
            border-top: 1px solid #dee2e6;
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
        <div id="roomCarousel" class="carousel slide mb-4" data-bs-ride="carousel">
            <div class="carousel-inner">
                <c:choose>
                    <c:when test="${not empty room.mainImageUrl}">
                        <div class="carousel-item active">
                            <img src="${room.mainImageUrl}" class="d-block w-100" alt="${room.name}">
                        </div>
                        <c:forEach var="imageUrl" items="${room.imageUrls}" varStatus="status">
                            <c:if test="${status.index > 0}">
                                <div class="carousel-item">
                                    <img src="${imageUrl}" class="d-block w-100" alt="${room.name}">
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
            <button class="carousel-control-prev" type="button" data-bs-target="#roomCarousel" data-bs-slide="prev">
                <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                <span class="visually-hidden">Previous</span>
            </button>
            <button class="carousel-control-next" type="button" data-bs-target="#roomCarousel" data-bs-slide="next">
                <span class="carousel-control-next-icon" aria-hidden="true"></span>
                <span class="visually-hidden">Next</span>
            </button>
        </div>

        <!-- Room Info -->
        <div class="room-info">
            <div class="room-type">
                <c:choose>
                    <c:when test="${not empty room.roomType}">
                        ${room.roomType}
                    </c:when>
                    <c:otherwise>
                        스탠다드 룸
                    </c:otherwise>
                </c:choose>
            </div>
            <h1 class="room-title">${room.name}</h1>

            <hr>

            <h3 class="section-title">객실 정보</h3>
            <div class="info-item">
                <div class="info-icon"><i class="bi bi-people"></i></div>
                <div class="info-text">
                    최대 ${room.capacity}인
                </div>
            </div>
            <div class="info-item">
                <div class="info-icon"><i class="bi bi-rulers"></i></div>
                <div class="info-text">
                    <c:choose>
                        <c:when test="${not empty room.roomSize}">
                            객실 크기: ${room.roomSize}㎡
                        </c:when>
                        <c:otherwise>
                            객실 크기: 24㎡
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="info-item">
                <div class="info-icon"><i class="bi bi-door-closed"></i></div>
                <div class="info-text">
                    <c:choose>
                        <c:when test="${not empty room.bedType}">
                            침대 유형: ${room.bedType}
                        </c:when>
                        <c:otherwise>
                            침대 유형: 퀸 사이즈 1개
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="info-item">
                <div class="info-icon"><i class="bi bi-currency-dollar"></i></div>
                <div class="info-text">
                    가격: 
                    <c:choose>
                        <c:when test="${not empty room.price}">
                            <fmt:formatNumber value="${room.price}" pattern="#,###" />원
                        </c:when>
                        <c:otherwise>
                            <fmt:formatNumber value="${100000 + room.roomId * 5000}" pattern="#,###" />원
                        </c:otherwise>
                    </c:choose>
                    / 1박
                </div>
            </div>
        </div>

        <!-- Tab Bar -->
        <div class="tab-bar">
            <div class="tab-item active" onclick="openTab('reservation')">예약하기</div>
            <div class="tab-item" onclick="openTab('details')">상세 정보</div>
            <div class="tab-item" onclick="openTab('amenities')">편의시설</div>
        </div>

        <!-- Tab Contents -->
        <div id="reservation" class="tab-content active">
            <div class="reservation-form">
                <h3 class="section-title">날짜 선택</h3>

                <div class="d-flex justify-content-between mb-3">
                    <button class="btn btn-outline-secondary" id="prevMonth"><i class="bi bi-chevron-left"></i> 이전 달</button>
                    <h5 id="currentMonth" class="mb-0 align-self-center"></h5>
                    <button class="btn btn-outline-secondary" id="nextMonth">다음 달 <i class="bi bi-chevron-right"></i></button>
                </div>

                <div id="calendar"></div>

                <form action="${pageContext.request.contextPath}/reservation/form" method="get" class="mt-4">
                    <input type="hidden" name="roomId" value="${room.roomId}">

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
                        <select class="form-select" id="guestCount" name="guestCount">
                            <c:forEach begin="1" end="${room.capacity}" var="i">
                                <option value="${i}">${i}명</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="price-breakdown">
                        <div class="price-item">
                            <div>객실 요금</div>
                            <div id="roomPrice">0원</div>
                        </div>
                        <div class="price-item">
                            <div>세금 및 봉사료</div>
                            <div id="taxFee">0원</div>
                        </div>
                        <div class="price-total">
                            <div>총 결제 금액</div>
                            <div id="totalPrice">0원</div>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-yanolja w-100 mt-3">예약하기</button>
                </form>
            </div>
        </div>

        <div id="details" class="tab-content">
            <div class="room-info">
                <h3 class="section-title">객실 설명</h3>
                <p>${room.description}</p>

                <hr>

                <h3 class="section-title">체크인/체크아웃 정보</h3>
                <div class="info-item">
                    <div class="info-icon"><i class="bi bi-clock"></i></div>
                    <div class="info-text">
                        체크인: 15:00 / 체크아웃: 11:00
                    </div>
                </div>

                <hr>

                <h3 class="section-title">취소 및 환불 규정</h3>
                <ul>
                    <li>체크인 7일 전: 100% 환불</li>
                    <li>체크인 5일 전: 70% 환불</li>
                    <li>체크인 3일 전: 50% 환불</li>
                    <li>체크인 1일 전: 환불 불가</li>
                </ul>
            </div>
        </div>

        <div id="amenities" class="tab-content">
            <div class="room-info">
                <h3 class="section-title">객실 내 시설</h3>
                <div class="amenities-list">
                    <c:if test="${not empty room.amenities}">
                        <c:forEach var="amenity" items="${room.amenities.split(',')}">
                            <div class="amenity-item">
                                <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                                <div>${amenity.trim()}</div>
                            </div>
                        </c:forEach>
                    </c:if>
                    <c:if test="${empty room.amenities}">
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
                            <div>미니바</div>
                        </div>
                        <div class="amenity-item">
                            <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                            <div>욕실용품</div>
                        </div>
                        <div class="amenity-item">
                            <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                            <div>헤어드라이어</div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <!-- Booking Bar -->
    <div class="booking-bar">
        <div class="booking-price">
            <c:choose>
                <c:when test="${not empty room.price}">
                    <fmt:formatNumber value="${room.price}" pattern="#,###" />원
                </c:when>
                <c:otherwise>
                    <fmt:formatNumber value="${100000 + room.roomId * 5000}" pattern="#,###" />원
                </c:otherwise>
            </c:choose>
            <span class="booking-price-unit">/ 1박</span>
        </div>
        <a href="#reservation" class="btn btn-yanolja">예약하기</a>
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
        // 현재 날짜 설정
        const today = new Date();
        let currentMonth = today.getMonth();
        let currentYear = today.getFullYear();

        // 페이지 로드 시 실행
        document.addEventListener('DOMContentLoaded', function() {
            // 체크인/체크아웃 날짜 초기화
            initDatePickers();

            // 캘린더 초기화
            renderCalendar(currentMonth, currentYear);

            // 이전/다음 달 버튼 이벤트
            document.getElementById('prevMonth').addEventListener('click', function() {
                currentMonth--;
                if (currentMonth < 0) {
                    currentMonth = 11;
                    currentYear--;
                }
                renderCalendar(currentMonth, currentYear);
            });

            document.getElementById('nextMonth').addEventListener('click', function() {
                currentMonth++;
                if (currentMonth > 11) {
                    currentMonth = 0;
                    currentYear++;
                }
                renderCalendar(currentMonth, currentYear);
            });

            // 체크아웃 날짜 유효성 검사
            document.getElementById('checkInDate').addEventListener('change', function() {
                validateDates();
                updatePriceBreakdown();
            });

            document.getElementById('checkOutDate').addEventListener('change', function() {
                validateDates();
                updatePriceBreakdown();
            });

            document.getElementById('guestCount').addEventListener('change', function() {
                updatePriceBreakdown();
            });

            // 초기 가격 계산
            updatePriceBreakdown();
        });

        // 체크인/체크아웃 날짜 초기화
        function initDatePickers() {
            const tomorrow = new Date(today);
            tomorrow.setDate(tomorrow.getDate() + 1);

            const dayAfterTomorrow = new Date(today);
            dayAfterTomorrow.setDate(dayAfterTomorrow.getDate() + 2);

            const formatDate = (date) => {
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const day = String(date.getDate()).padStart(2, '0');
                return `${year}-${month}-${day}`;
            };

            document.getElementById('checkInDate').value = formatDate(tomorrow);
            document.getElementById('checkInDate').min = formatDate(today);
            document.getElementById('checkOutDate').value = formatDate(dayAfterTomorrow);
            document.getElementById('checkOutDate').min = formatDate(tomorrow);
        }

        // 날짜 유효성 검사
        function validateDates() {
            const checkInDate = new Date(document.getElementById('checkInDate').value);
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
        }

        // 캘린더 렌더링
        function renderCalendar(month, year) {
            const monthNames = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];
            const daysInMonth = new Date(year, month + 1, 0).getDate();
            const firstDay = new Date(year, month, 1).getDay();

            document.getElementById('currentMonth').textContent = `${year}년 ${monthNames[month]}`;

            let calendarHTML = '<table class="calendar">';
            calendarHTML += '<thead><tr>';
            calendarHTML += '<th>일</th><th>월</th><th>화</th><th>수</th><th>목</th><th>금</th><th>토</th>';
            calendarHTML += '</tr></thead><tbody>';

            // 날짜 채우기
            let date = 1;
            for (let i = 0; i < 6; i++) {
                calendarHTML += '<tr>';

                for (let j = 0; j < 7; j++) {
                    if (i === 0 && j < firstDay) {
                        calendarHTML += '<td></td>';
                    } else if (date > daysInMonth) {
                        calendarHTML += '<td></td>';
                    } else {
                        const currentDate = new Date(year, month, date);
                        const isToday = currentDate.toDateString() === today.toDateString();
                        const isPast = currentDate < today;

                        // 가용성 확인 (실제로는 API 호출 필요)
                        const isAvailable = !isPast && Math.random() > 0.3; // 임시로 랜덤하게 가용성 설정

                        let cellClass = '';
                        if (isToday) cellClass += ' today';
                        if (isPast || !isAvailable) {
                            cellClass += ' unavailable';
                            calendarHTML += `<td class="${cellClass}">${date}</td>`;
                        } else {
                            cellClass += ' available';
                            calendarHTML += `<td class="${cellClass}" onclick="selectDate(${year}, ${month}, ${date})">${date}</td>`;
                        }

                        date++;
                    }
                }

                calendarHTML += '</tr>';
                if (date > daysInMonth) break;
            }

            calendarHTML += '</tbody></table>';
            document.getElementById('calendar').innerHTML = calendarHTML;
        }

        // 날짜 선택 함수
        function selectDate(year, month, day) {
            const selectedDate = new Date(year, month, day);
            const formatDate = (date) => {
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const day = String(date.getDate()).padStart(2, '0');
                return `${year}-${month}-${day}`;
            };

            // 체크인 날짜 설정
            document.getElementById('checkInDate').value = formatDate(selectedDate);

            // 체크아웃 날짜 설정 (체크인 다음날)
            const checkOutDate = new Date(selectedDate);
            checkOutDate.setDate(checkOutDate.getDate() + 1);
            document.getElementById('checkOutDate').value = formatDate(checkOutDate);

            // 가격 업데이트
            updatePriceBreakdown();

            // 선택된 날짜 표시
            renderCalendar(currentMonth, currentYear);
        }

        // 가격 계산 함수
        function updatePriceBreakdown() {
            const checkInDate = new Date(document.getElementById('checkInDate').value);
            const checkOutDate = new Date(document.getElementById('checkOutDate').value);

            // 숙박 일수 계산
            const nights = Math.round((checkOutDate - checkInDate) / (1000 * 60 * 60 * 24));

            // 객실 가격 (실제 구현에서는 API에서 가져온 가격 사용)
            const pricePerNight = ${not empty room.price ? room.price : 100000 + room.roomId * 5000};

            // 객실 요금 계산
            const roomPrice = nights * pricePerNight;

            // 세금 및 봉사료 계산 (10%)
            const taxFee = Math.round(roomPrice * 0.1);

            // 총 금액
            const totalPrice = roomPrice + taxFee;

            // 화면에 표시
            document.getElementById('roomPrice').textContent = roomPrice.toLocaleString() + '원';
            document.getElementById('taxFee').textContent = taxFee.toLocaleString() + '원';
            document.getElementById('totalPrice').textContent = totalPrice.toLocaleString() + '원';
        }

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

        // 예약하기 버튼 클릭 시 예약 탭으로 이동
        document.querySelector('.booking-bar a').addEventListener('click', function(e) {
            e.preventDefault();
            openTab('reservation');
            window.scrollTo({
                top: document.querySelector('.tab-bar').offsetTop - 20,
                behavior: 'smooth'
            });
        });
    </script>
</body>
</html>
