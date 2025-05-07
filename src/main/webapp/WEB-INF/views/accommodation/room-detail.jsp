<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${room.name} - 객실 상세</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        .carousel-item img {
            height: 400px;
            object-fit: cover;
        }
        .amenities-list {
            columns: 2;
        }
        .booking-card {
            position: sticky;
            top: 20px;
        }
        .calendar {
            width: 100%;
            border-collapse: collapse;
        }
        .calendar th, .calendar td {
            border: 1px solid #dee2e6;
            padding: 8px;
            text-align: center;
        }
        .calendar th {
            background-color: #f8f9fa;
        }
        .calendar .available {
            background-color: #d4edda;
        }
        .calendar .unavailable {
            background-color: #f8d7da;
            color: #721c24;
        }
        .calendar .today {
            font-weight: bold;
            border: 2px solid #0d6efd;
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
        
        <!-- 객실 정보 -->
        <div class="row mb-5">
            <!-- 이미지 캐러셀 -->
            <div class="col-md-8">
                <div id="roomCarousel" class="carousel slide" data-bs-ride="carousel">
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
                                    <img src="${pageContext.request.contextPath}/resources/images/no-image.jpg" class="d-block w-100" alt="이미지 없음">
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
            </div>
            
            <!-- 객실 기본 정보 및 예약 카드 -->
            <div class="col-md-4">
                <div class="card booking-card">
                    <div class="card-body">
                        <h2 class="card-title">${room.name}</h2>
                        <p class="text-muted">${accommodation.title}</p>
                        
                        <div class="mb-3">
                            <h5>객실 정보</h5>
                            <p><i class="bi bi-people"></i> 최대 ${room.capacity}인</p>
                            <p><i class="bi bi-rulers"></i> 객실 크기: ${room.roomSize}㎡</p>
                            <p><i class="bi bi-door-closed"></i> 객실 수: ${room.roomCount}개</p>
                            <p><i class="bi bi-currency-dollar"></i> 가격: <fmt:formatNumber value="${room.price}" type="currency" currencySymbol="₩" maxFractionDigits="0"/>/박</p>
                        </div>
                        
                        <!-- 예약 폼 -->
                        <form action="${pageContext.request.contextPath}/reservation/form/${room.roomId}" method="get">
                            <input type="hidden" name="roomId" value="${room.roomId}">
                            <div class="mb-3">
                                <label for="checkInDate" class="form-label">체크인 날짜</label>
                                <input type="date" class="form-control" id="checkInDate" name="checkInDate" required>
                            </div>
                            <div class="mb-3">
                                <label for="checkOutDate" class="form-label">체크아웃 날짜</label>
                                <input type="date" class="form-control" id="checkOutDate" name="checkOutDate" required>
                            </div>
                            <div class="mb-3">
                                <label for="guestCount" class="form-label">인원 수</label>
                                <input type="number" class="form-control" id="guestCount" name="guestCount" min="1" max="${room.capacity}" value="1" required>
                            </div>
                            <div class="d-grid">
                                <button type="submit" class="btn btn-primary">예약하기</button>
                            </div>
                        </form>
                        
                        <!-- 호스트인 경우 수정/삭제 버튼 표시 -->
                        <c:if test="${sessionScope.userId == accommodation.hostId}">
                            <div class="mt-4">
                                <a href="${pageContext.request.contextPath}/accommodation/update-room-form/${room.roomId}" class="btn btn-outline-primary me-2">수정</a>
                                <a href="${pageContext.request.contextPath}/accommodation/delete-room/${room.roomId}" class="btn btn-outline-danger" onclick="return confirm('정말 삭제하시겠습니까?')">삭제</a>
                            </div>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 객실 상세 설명 -->
        <div class="row mb-5">
            <div class="col-12">
                <div class="card">
                    <div class="card-body">
                        <h4 class="card-title">객실 설명</h4>
                        <p class="card-text">${room.description}</p>
                        
                        <h5 class="mt-4">침대 유형</h5>
                        <p>${room.bedType}</p>
                        
                        <h5 class="mt-4">편의시설</h5>
                        <c:if test="${not empty room.amenities}">
                            <ul class="amenities-list">
                                <c:forEach var="amenity" items="${room.amenities.split(',')}">
                                    <li><i class="bi bi-check-circle-fill text-success"></i> ${amenity.trim()}</li>
                                </c:forEach>
                            </ul>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 가용성 캘린더 -->
        <div class="row mb-5">
            <div class="col-12">
                <div class="card">
                    <div class="card-body">
                        <h4 class="card-title">가용성 캘린더</h4>
                        <div class="d-flex justify-content-between mb-3">
                            <button class="btn btn-outline-secondary" id="prevMonth"><i class="bi bi-chevron-left"></i> 이전 달</button>
                            <h5 id="currentMonth" class="mb-0 align-self-center"></h5>
                            <button class="btn btn-outline-secondary" id="nextMonth">다음 달 <i class="bi bi-chevron-right"></i></button>
                        </div>
                        <div id="calendar"></div>
                        <div class="mt-3">
                            <span class="badge bg-success">가용</span>
                            <span class="badge bg-danger ms-2">불가</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 숙소 정보 링크 -->
        <div class="row mb-5">
            <div class="col-12">
                <div class="card">
                    <div class="card-body">
                        <h4 class="card-title">숙소 정보</h4>
                        <p><i class="bi bi-geo-alt"></i> ${accommodation.address}</p>
                        <p><i class="bi bi-telephone"></i> ${accommodation.phone}</p>
                        <p><i class="bi bi-clock"></i> 체크인: ${accommodation.checkInTime} / 체크아웃: ${accommodation.checkOutTime}</p>
                        <a href="${pageContext.request.contextPath}/accommodation/detail/${accommodation.accommodationId}" class="btn btn-outline-primary">숙소 상세 정보 보기</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="../fragments/footer.jsp" />
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 오늘 날짜 설정
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
            });
            
            document.getElementById('checkOutDate').addEventListener('change', function() {
                validateDates();
            });
        });
        
        // 체크인/체크아웃 날짜 초기화
        function initDatePickers() {
            const tomorrow = new Date(today);
            tomorrow.setDate(tomorrow.getDate() + 1);
            
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
                        if (isPast || !isAvailable) cellClass += ' unavailable';
                        else cellClass += ' available';
                        
                        calendarHTML += `<td class="${cellClass}">${date}</td>`;
                        date++;
                    }
                }
                
                calendarHTML += '</tr>';
                if (date > daysInMonth) break;
            }
            
            calendarHTML += '</tbody></table>';
            document.getElementById('calendar').innerHTML = calendarHTML;
            
            // 실제 구현에서는 여기서 API를 호출하여 가용성 정보를 가져와 캘린더에 표시
            loadAvailability(year, month);
        }
        
        // 가용성 정보 로드 (실제 구현에서는 API 호출)
        function loadAvailability(year, month) {
            // 예시: API 호출하여 가용성 정보 가져오기
            // fetch(`${pageContext.request.contextPath}/api/room/${room.roomId}/availability?year=${year}&month=${month}`)
            //     .then(response => response.json())
            //     .then(data => {
            //         // 가용성 정보를 캘린더에 표시
            //         updateCalendarAvailability(data);
            //     })
            //     .catch(error => console.error('Error loading availability:', error));
        }
        
        // 캘린더 가용성 업데이트 (실제 구현에서 사용)
        function updateCalendarAvailability(availabilityData) {
            const calendar = document.getElementById('calendar');
            const cells = calendar.querySelectorAll('td');
            
            cells.forEach(cell => {
                const day = parseInt(cell.textContent);
                if (!isNaN(day) && day > 0) {
                    const date = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
                    const availability = availabilityData.find(a => a.date === date);
                    
                    if (availability) {
                        if (availability.availableCount > 0) {
                            cell.classList.add('available');
                            cell.classList.remove('unavailable');
                        } else {
                            cell.classList.add('unavailable');
                            cell.classList.remove('available');
                        }
                    }
                }
            });
        }
    </script>
</body>
</html>