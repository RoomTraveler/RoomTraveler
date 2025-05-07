<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>숙박 지역 선택</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            line-height: 1.6;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        .search-container {
            background-color: #f5f5f5;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        .search-container label {
            margin-right: 10px;
        }
        .search-container select {
            padding: 5px;
            margin-right: 15px;
        }
        .search-container button {
            padding: 5px 15px;
            background-color: #4CAF50;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        .search-container button:hover {
            background-color: #45a049;
        }
        .accommodation-list {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
        }
        .accommodation-card {
            border: 1px solid #ddd;
            border-radius: 5px;
            padding: 15px;
            width: calc(33.333% - 20px);
            box-sizing: border-box;
            cursor: pointer;
            transition: transform 0.3s;
        }
        .accommodation-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }
        .accommodation-card img {
            width: 100%;
            height: 200px;
            object-fit: cover;
            border-radius: 5px;
            margin-bottom: 10px;
        }
        .accommodation-card h3 {
            margin-top: 0;
            margin-bottom: 10px;
        }
        .accommodation-card p {
            margin: 5px 0;
            color: #666;
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
            background-color: #4CAF50;
            color: white;
            border-color: #4CAF50;
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
            background-color: #ccc;
        }
        .tabcontent {
            display: none;
            padding: 20px;
            border: 1px solid #ccc;
            border-top: none;
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
        }
    </style>
</head>
<body>
<div class="container">
    <h2>숙박 지역 선택</h2>

    <div class="search-container">
        <label>시도:</label>
        <select id="areaSelect"><option value="">전체</option></select>

        <label>시군구:</label>
        <select id="sigunguSelect"><option value="">전체</option></select>

        <button id="searchBtn">검색</button>
    </div>

    <div class="loading" id="loading">
        <p>로딩 중...</p>
    </div>

    <div class="accommodation-list" id="accommodationList"></div>

    <div class="pagination" id="pagination"></div>

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

<script>
    $(function(){
        const apiBase = '${pageContext.request.contextPath}/accommodation/api';
        let currentPage = 1;
        let totalPages = 1;
        let itemsPerPage = 10;
        let totalItems = 0;
        let currentAreaCode = '';
        let currentSigunguCode = '';

        // 시도 목록 로드
        $.ajax({ url: apiBase + '/sidos', dataType: 'json' })
            .done(data => data.forEach(sido =>
                $('#areaSelect').append(
                    $('<option>').val(sido.code).text(sido.name)
                )
            ))
            .fail(xhr => console.error('시도 로드 실패:', xhr.responseJSON || xhr.responseText));

        // 시도 선택 시 구군 로드
        $('#areaSelect').change(function(){
            const code = $(this).val();
            $('#sigunguSelect').empty().append('<option value="">전체</option>');
            if (!code) return;

            $.ajax({ url: apiBase + '/guguns', data: { sido: code }, dataType: 'json' })
                .done(data => data.forEach(gugun =>
                    $('#sigunguSelect').append(
                        $('<option>').val(gugun.code).text(gugun.name)
                    )
                ))
                .fail(xhr => console.error('구군 로드 실패:', xhr.responseJSON || xhr.responseText));
        });

        // 검색 버튼 클릭 시 숙박 정보 로드
        $('#searchBtn').click(function() {
            currentPage = 1;
            loadAccommodations();
        });

        // 숙박 정보 로드 함수
        function loadAccommodations() {
            $('#loading').show();
            $('#accommodationList').empty();
            $('#pagination').empty();

            currentAreaCode = $('#areaSelect').val();
            currentSigunguCode = $('#sigunguSelect').val();

            const params = {
                pageNo: currentPage,
                numOfRows: itemsPerPage
            };

            if (currentAreaCode) {
                params.areaCode = currentAreaCode;
            }

            if (currentSigunguCode) {
                params.sigunguCode = currentSigunguCode;
            }

            $.ajax({
                url: apiBase + '/accommodations',
                data: params,
                dataType: 'json'
            })
            .done(function(data) {
                $('#loading').hide();

                if (!data.items || data.items.length === 0) {
                    $('#accommodationList').html('<p>검색 결과가 없습니다.</p>');
                    return;
                }

                totalItems = data.totalCount;
                totalPages = Math.ceil(totalItems / itemsPerPage);

                // 숙박 정보 표시
                data.items.forEach(item => {
                    const card = $('<div>').addClass('accommodation-card').attr('data-id', item.contentid);

                    // 이미지 추가 (백엔드에서 이미지가 있는 것만 필터링했지만 안전하게 체크)
                    if (item.firstimage) {
                        card.append($('<img>').attr('src', item.firstimage).attr('alt', item.title));
                    }

                    // 정보 추가
                    card.append($('<h3>').text(item.title));
                    if (item.addr1) {
                        card.append($('<p>').text('주소: ' + item.addr1 + (item.addr2 ? ' ' + item.addr2 : '')));
                    }
                    if (item.tel) {
                        card.append($('<p>').text('전화: ' + item.tel));
                    }

                    // 클릭 이벤트 추가
                    card.click(function() {
                        const contentId = $(this).attr('data-id');
                        loadRoomInfo(contentId);
                    });

                    $('#accommodationList').append(card);
                });

                // 페이지네이션 생성
                createPagination();
            })
            .fail(function(xhr) {
                $('#loading').hide();
                console.error('숙박 정보 로드 실패:', xhr.responseJSON || xhr.responseText);
                $('#accommodationList').html('<p>숙박 정보를 불러오는 중 오류가 발생했습니다.</p>');
            });
        }

        // 페이지네이션 생성 함수
        function createPagination() {
            const pagination = $('#pagination');
            pagination.empty();

            // 처음 페이지로 이동
            if (currentPage > 1) {
                pagination.append($('<button>').text('처음').click(() => {
                    currentPage = 1;
                    loadAccommodations();
                }));
            }

            // 이전 페이지로 이동
            if (currentPage > 1) {
                pagination.append($('<button>').text('이전').click(() => {
                    currentPage--;
                    loadAccommodations();
                }));
            }

            // 페이지 번호
            const startPage = Math.max(1, currentPage - 2);
            const endPage = Math.min(totalPages, currentPage + 2);

            for (let i = startPage; i <= endPage; i++) {
                const pageBtn = $('<button>').text(i);
                if (i === currentPage) {
                    pageBtn.addClass('active');
                }
                pageBtn.click(() => {
                    currentPage = i;
                    loadAccommodations();
                });
                pagination.append(pageBtn);
            }

            // 다음 페이지로 이동
            if (currentPage < totalPages) {
                pagination.append($('<button>').text('다음').click(() => {
                    currentPage++;
                    loadAccommodations();
                }));
            }

            // 마지막 페이지로 이동
            if (currentPage < totalPages) {
                pagination.append($('<button>').text('마지막').click(() => {
                    currentPage = totalPages;
                    loadAccommodations();
                }));
            }
        }

        // 객실 정보 로드 함수
        function loadRoomInfo(contentId) {
            $('#loading').show();
            $('#modalTitle').empty();
            $('#modalBasicInfo').empty();
            $('#roomList').empty();
            $('#facilityDetails').empty();
            $('#imageGallery').empty();

            $.ajax({
                url: apiBase + '/rooms',
                data: { contentId: contentId },
                dataType: 'json'
            })
            .done(function(data) {
                $('#loading').hide();

                // 기본 정보 표시
                const basicInfo = data.basicInfo;
                $('#modalTitle').text(basicInfo.title);

                const basicInfoHtml = $('<div>');
                if (basicInfo.firstimage) {
                    basicInfoHtml.append($('<img>').attr('src', basicInfo.firstimage).attr('alt', basicInfo.title).css({
                        'max-width': '300px',
                        'max-height': '200px',
                        'margin-right': '15px',
                        'float': 'left'
                    }));
                }

                const infoDetails = $('<div>').css('overflow', 'hidden');
                if (basicInfo.addr1) {
                    infoDetails.append($('<p>').text('주소: ' + basicInfo.addr1 + (basicInfo.addr2 ? ' ' + basicInfo.addr2 : '')));
                }
                if (basicInfo.tel) {
                    infoDetails.append($('<p>').text('전화: ' + basicInfo.tel));
                }
                if (basicInfo.homepage) {
                    // HTML 태그 제거
                    const homepage = basicInfo.homepage.replace(/<[^>]*>/g, '');
                    infoDetails.append($('<p>').text('홈페이지: ' + homepage));
                }
                if (basicInfo.overview) {
                    infoDetails.append($('<p>').text('개요: ' + basicInfo.overview.substring(0, 200) + '...'));
                }

                basicInfoHtml.append(infoDetails);
                $('#modalBasicInfo').append(basicInfoHtml);

                // 객실 정보 표시
                if (data.roomInfo && data.roomInfo.length > 0) {
                    data.roomInfo.forEach(room => {
                        const roomCard = $('<div>').addClass('room-card');

                        // 객실 이미지
                        if (room.roomimg1) {
                            roomCard.append($('<img>').attr('src', room.roomimg1).attr('alt', room.roomtitle || '객실 이미지'));
                        }

                        const roomDetails = $('<div>').addClass('room-details');
                        roomDetails.append($('<h4>').text(room.roomtitle || '객실 정보'));

                        // 객실 기본 정보
                        if (room.roomsize1 || room.roomsize2) {
                            roomDetails.append($('<p>').text('객실 크기: ' + 
                                (room.roomsize1 ? room.roomsize1 + '평' : '') + 
                                (room.roomsize2 ? ' (' + room.roomsize2 + '㎡)' : '')));
                        }
                        if (room.roomcount) {
                            roomDetails.append($('<p>').text('객실 수: ' + room.roomcount));
                        }
                        if (room.roombasecount || room.roommaxcount) {
                            roomDetails.append($('<p>').text('수용 인원: ' + 
                                (room.roombasecount ? '기준 ' + room.roombasecount + '인' : '') + 
                                (room.roommaxcount ? ', 최대 ' + room.roommaxcount + '인' : '')));
                        }
                        if (room.roomintro) {
                            roomDetails.append($('<p>').text('객실 소개: ' + room.roomintro));
                        }

                        // 요금 정보
                        const feeInfo = [];
                        if (room.roomoffseasonminfee1 && room.roomoffseasonminfee1 !== '0') {
                            feeInfo.push('비수기 주중: ' + room.roomoffseasonminfee1 + '원');
                        }
                        if (room.roomoffseasonminfee2 && room.roomoffseasonminfee2 !== '0') {
                            feeInfo.push('비수기 주말: ' + room.roomoffseasonminfee2 + '원');
                        }
                        if (room.roompeakseasonminfee1 && room.roompeakseasonminfee1 !== '0') {
                            feeInfo.push('성수기 주중: ' + room.roompeakseasonminfee1 + '원');
                        }
                        if (room.roompeakseasonminfee2 && room.roompeakseasonminfee2 !== '0') {
                            feeInfo.push('성수기 주말: ' + room.roompeakseasonminfee2 + '원');
                        }

                        if (feeInfo.length > 0) {
                            roomDetails.append($('<p>').text('요금 정보: ' + feeInfo.join(', ')));
                        }

                        // 객실 시설 정보
                        const facilities = [];
                        if (room.roomaircondition === 'Y') facilities.push('에어컨');
                        if (room.roombath === 'Y') facilities.push('욕조');
                        if (room.roombathfacility === 'Y') facilities.push('욕실시설');
                        if (room.roomcable === 'Y') facilities.push('케이블TV');
                        if (room.roomcook === 'Y') facilities.push('취사시설');
                        if (room.roomhairdryer === 'Y') facilities.push('드라이기');
                        if (room.roomhometheater === 'Y') facilities.push('홈시어터');
                        if (room.roominternet === 'Y') facilities.push('인터넷');
                        if (room.roompc === 'Y') facilities.push('PC');
                        if (room.roomrefrigerator === 'Y') facilities.push('냉장고');
                        if (room.roomsofa === 'Y') facilities.push('소파');
                        if (room.roomtable === 'Y') facilities.push('테이블');
                        if (room.roomtoiletries === 'Y') facilities.push('세면도구');
                        if (room.roomtv === 'Y') facilities.push('TV');

                        if (facilities.length > 0) {
                            const facilityDiv = $('<div>').addClass('room-facilities');
                            facilities.forEach(facility => {
                                facilityDiv.append($('<span>').addClass('facility').text(facility));
                            });
                            roomDetails.append($('<p>').text('객실 시설:'));
                            roomDetails.append(facilityDiv);
                        }

                        roomCard.append(roomDetails);
                        $('#roomList').append(roomCard);
                    });
                } else {
                    $('#roomList').html('<p>객실 정보가 없습니다.</p>');
                }

                // 시설 정보 표시
                const introInfo = data.introInfo;
                if (Object.keys(introInfo).length > 0) {
                    const facilityHtml = $('<div>');

                    if (introInfo.checkintime || introInfo.checkouttime) {
                        facilityHtml.append($('<p>').html('<strong>체크인/체크아웃:</strong> ' + 
                            (introInfo.checkintime || '') + ' / ' + (introInfo.checkouttime || '')));
                    }

                    if (introInfo.parkinglodging) {
                        facilityHtml.append($('<p>').html('<strong>주차 시설:</strong> ' + introInfo.parkinglodging));
                    }

                    if (introInfo.reservationlodging) {
                        facilityHtml.append($('<p>').html('<strong>예약 안내:</strong> ' + introInfo.reservationlodging));
                    }

                    if (introInfo.scalelodging) {
                        facilityHtml.append($('<p>').html('<strong>규모:</strong> ' + introInfo.scalelodging));
                    }

                    if (introInfo.subfacility) {
                        facilityHtml.append($('<p>').html('<strong>부대 시설:</strong> ' + introInfo.subfacility));
                    }

                    // 기타 시설 정보
                    const otherFacilities = [];
                    if (introInfo.barbecue === 'Y') otherFacilities.push('바비큐');
                    if (introInfo.beauty === 'Y') otherFacilities.push('뷰티시설');
                    if (introInfo.beverage === 'Y') otherFacilities.push('식음료장');
                    if (introInfo.bicycle === 'Y') otherFacilities.push('자전거대여');
                    if (introInfo.campfire === 'Y') otherFacilities.push('캠프파이어');
                    if (introInfo.fitness === 'Y') otherFacilities.push('피트니스');
                    if (introInfo.karaoke === 'Y') otherFacilities.push('노래방');
                    if (introInfo.publicbath === 'Y') otherFacilities.push('공용샤워실');
                    if (introInfo.publicpc === 'Y') otherFacilities.push('공용PC실');
                    if (introInfo.sauna === 'Y') otherFacilities.push('사우나');
                    if (introInfo.seminar === 'Y') otherFacilities.push('세미나실');
                    if (introInfo.sports === 'Y') otherFacilities.push('스포츠시설');

                    if (otherFacilities.length > 0) {
                        facilityHtml.append($('<p>').html('<strong>기타 시설:</strong> ' + otherFacilities.join(', ')));
                    }

                    if (introInfo.refundregulation) {
                        facilityHtml.append($('<p>').html('<strong>환불 규정:</strong> ' + introInfo.refundregulation));
                    }

                    $('#facilityDetails').append(facilityHtml);
                } else {
                    $('#facilityDetails').html('<p>시설 정보가 없습니다.</p>');
                }

                // 이미지 정보 표시
                if (data.imageInfo && data.imageInfo.length > 0) {
                    data.imageInfo.forEach(image => {
                        if (image.originimgurl) {
                            const img = $('<img>')
                                .attr('src', image.originimgurl)
                                .attr('alt', image.imgname || '숙소 이미지')
                                .click(function() {
                                    window.open(image.originimgurl, '_blank');
                                });
                            $('#imageGallery').append(img);
                        }
                    });
                } else {
                    $('#imageGallery').html('<p>이미지가 없습니다.</p>');
                }

                // 모달 표시
                $('#accommodationModal').css('display', 'block');
            })
            .fail(function(xhr) {
                $('#loading').hide();
                console.error('객실 정보 로드 실패:', xhr.responseJSON || xhr.responseText);
                alert('객실 정보를 불러오는 중 오류가 발생했습니다.');
            });
        }

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
