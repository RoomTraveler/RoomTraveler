<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>지역 관광지 검색 및 여행 계획</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
<div class="container-fluid mt-4">
    <div class="row">
        <!-- LEFT: Filters, Search Results & Map -->
        <div class="col-lg-8">
            <!-- 필터 및 검색 -->
            <div class="card mb-3">
                <div class="card-body">
                    <form id="searchForm" class="row g-2">
                        <div class="col-md-4">
                            <label for="contentDropdown" class="form-label">컨텐츠</label>
                            <select class="form-select" id="contentDropdown" name="content">
                                <option disabled selected>선택하세요</option>
                                <c:forEach var="contentItem" items="${contentList}">
                                    <option value="${contentItem.code}">${contentItem.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label for="keywordInput" class="form-label">키워드 검색</label>
                            <div class="input-group">
                                <input type="text" id="keywordInput" class="form-control" placeholder="검색어 입력">
                                <button type="button" id="keywordSearchBtn" class="btn btn-outline-secondary">검색</button>
                            </div>
                        </div>
                        <div class="col-md-4 align-self-end text-end">
                            <button type="button" id="attractionBtn" class="btn btn-primary">관광지 검색</button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- 검색 결과 및 페이지네이션 -->
            <div class="row" style="height: 70vh;">
                <div class="col-md-4">
                    <h5>검색된 관광지 목록</h5>
                    <ul id="spotList" class="list-group mb-2" style="max-height:500px; overflow-y:auto;">
                        <!-- JS로 li 채움 -->
                    </ul>
                    <!-- 페이지네이션 (동적 처리 필요) -->
                    <nav aria-label="Page navigation">
                        <ul id="pagination" class="pagination">
                            <!-- JS로 페이지 번호 생성 -->
                        </ul>
                    </nav>
                </div>
                <div class="col-md-8">
                    <div id="map" style="width:100%; height:100%;" class="border"></div>
                </div>
            </div>
        </div>

        <!-- RIGHT: 선택된 관광지 (여행 일정) -->
        <div class="col-lg-4">
            <div class="card">
                <div class="card-header">
                    <h5>여행 일정 선택</h5>
                </div>
                <div class="card-body">
                    <ul id="selectedList" class="list-group mb-3" style="max-height:600px; overflow-y:auto;">
                        <!-- JS로 선택된 장소 채움 -->
                    </ul>
                    <button id="planStoreBtn" type="submit" class="btn btn-success w-100">선택한 장소 저장</button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Bootstrap JS & Kakao Maps SDK -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=18dcaec57ee4149f30695dab2b7cc784"></script>
<script>
    // 초기 맵 세팅
    let map, mapMarkers = [], infoWindows = [];
    let currentOpenInfoWindow = null;
    let mapBound = null;
    const id = 1; // 아이디 일단 번호 나중엔 JWT에서 가져오는 식이 되려나...
    function initMap() {
        const defaultLat = 35.205432, defaultLng = 126.811591;
        const container = document.getElementById('map');
        map = new kakao.maps.Map(container, { center: new kakao.maps.LatLng(defaultLat, defaultLng), level: 6 });
        navigator.geolocation.getCurrentPosition(pos => {
            const { latitude, longitude } = pos.coords;
            map.setCenter(new kakao.maps.LatLng(latitude, longitude));
        });
    }
    window.addEventListener('DOMContentLoaded', initMap);

    function loadContentList() { // 컨텐츠
        fetch("/map/content-types")
            .then(response => response.json())
            .then(data => {
                const contentDropdown = document.getElementById("contentDropdown");
                contentDropdown.innerHTML = '<option disabled selected>컨텐츠 선택</option>';
                data.forEach(content => {
                    const option = document.createElement("option");
                    option.textContent = content.name;
                    option.value = content.code;
                    contentDropdown.appendChild(option);
                });
            })
            .catch(err => {
                console.error("컨텐츠 목록 로딩 실패", err);
            });
    }
    // 페이지 로드시 자동 실행
    window.addEventListener("DOMContentLoaded", loadContentList);

    async function fetchTourSpots(page) {

        const params = new URLSearchParams({
            mapBound: JSON.stringify(mapBound),
            page: page,
            size: 10
        });

        const response = await fetch(`/map/regions-content?\${params.toString()}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });
        const data = await response.json();
        renderSpots(data);
        updateMap(data);
    }

    // Spot 리스트 렌더링 (페이지네이션 고려)
    function renderSpots(spots) {
        const ul = document.getElementById("spotList");
        ul.innerHTML = ""; // 초기화

        spots.forEach(spot => {
            const li = document.createElement("li");
            li.className = "list-group-item";
            li.innerHTML = `
            <label>
                <input type="checkbox" name="attractionId" value="\${spot.no}">
                <strong>\${spot.title}</strong>
            </label><br/>
            <img src="\${spot.image}" class="img-thumbnail" style="max-width: 100px; height: 100px;"><br/>
            \${spot.addr1 || ""} \${spot.addr2 || ""}<br/>
            \${spot.tel || ""}`;
            ul.appendChild(li);
        });
    }

    function setPagination(totalPages, currentPage) {
        const pagination = document.getElementById("pagination");
        pagination.innerHTML = "";

        const blockSize = 5; // 한번에 보여줄 페이지 수
        const currentBlock = Math.floor(currentPage / blockSize);
        const startPage = currentBlock * blockSize;
        const endPage = Math.min(startPage + blockSize, totalPages);

        // < 버튼 (이전 블럭)
        if (startPage > 0) {
            const prevLi = document.createElement("li");
            prevLi.className = "page-item";
            const prevA = document.createElement("a");
            prevA.className = "page-link";
            prevA.href = "#";
            prevA.innerHTML = "&laquo;"; // <<
            prevA.onclick = () => {
                fetchTourSpots(startPage - 1);
                return false;
            };
            prevLi.appendChild(prevA);
            pagination.appendChild(prevLi);
        }

        // 페이지 번호 버튼
        for (let i = startPage; i < endPage; i++) {
            const li = document.createElement("li");
            li.className = `page-item \${i === currentPage ? 'active' : ''}`;

            const a = document.createElement("a");
            a.className = "page-link";
            a.href = "/map/regions-content?page=${i}";
            a.textContent = i + 1;
            a.onclick = () => {
                fetchTourSpots(i);
                return false;
            };

            li.appendChild(a);
            pagination.appendChild(li);
        }

        // > 버튼 (다음 블럭)
        if (endPage < totalPages) {
            const nextLi = document.createElement("li");
            nextLi.className = "page-item";
            const nextA = document.createElement("a");
            nextA.className = "page-link";
            nextA.href = "#";
            nextA.innerHTML = "&raquo;"; // >>
            nextA.onclick = () => {
                fetchTourSpots(endPage);
                return false;
            };
            nextLi.appendChild(nextA);
            pagination.appendChild(nextLi);
        }
    }

    function removeMarkers() {
        for (let i = 0; i < mapMarkers.length; i++) {
            mapMarkers[i].setMap(null);
        }
        mapMarkers = [];
        infoWindows = [];
    }

    const updateMap = (infos) => {
        try {
            removeMarkers();

            for (let i = 0; i < infos.length; i++) {
                const info = infos[i];
                const markerPosition = new kakao.maps.LatLng(info.latitude, info.longitude);
                const marker = new kakao.maps.Marker({
                    position: markerPosition,
                    clickable: true
                });
                marker.setMap(map);

                const iwContent = `<div class="custom-infowindow">
				  <h4>\${info.title}</h4>
				  <img src="\${info.image}" alt="\${info.title}" style="max-width: 100px; height: 100px;">
				  <p><strong>주소:</strong>\${info.addr1 + " " + info.addr2}</p>
				  <p><strong>전화번호:</strong>\${info.tel=='' ? '없음': info.tel}</p>
			  </div>`;

                const iwPosition = new kakao.maps.LatLng(info.latitude, info.longitude);
                const infoWindow = new kakao.maps.InfoWindow({
                    position : iwPosition,
                    content : iwContent
                });

                const content = `<div class="custom-infowindow">
				  <h4>\${info.title}</h4>
				  <img src="\${info.image}" alt="\${info.title}" style="max-width: 100px; height: 100px;">
				  <p><strong>주소:</strong>\${info.addr}</p>
				  <p><strong>전화번호:</strong>\${info.tel=='' ? '없음': info.tel}</p>
			  </div>`;

                kakao.maps.event.addListener(marker, 'click', function () {
                    if (currentOpenInfoWindow === infoWindow) {
                        infoWindow.close();
                        currentOpenInfoWindow = null;
                    } else {
                        if (currentOpenInfoWindow) {
                            currentOpenInfoWindow.close();
                        }
                        infoWindow.open(map, marker);
                        currentOpenInfoWindow = infoWindow;
                    }
                });

                mapMarkers.push(marker);
                infoWindows.push(infoWindow);
            }
        } catch (e) {
            console.log(e);
        }
    };

    const panTo = (x, y) => {
        var moveLatLon = new kakao.maps.LatLng(x, y);

        map.panTo(moveLatLon);
    }

    // 선택된 여행 일정 업데이트
    function updateSelected(id, title) {
        const ul = document.getElementById('selectedList');
        const li = document.createElement('li');
        li.className = 'list-group-item';
        li.draggable = true;
        li.textContent = title;
        li.value = id;
        ul.appendChild(li);
    }

    // 필터 버튼 이벤트
    document.getElementById('attractionBtn').addEventListener('click', async () => {
        const bounds = map.getBounds();
        mapBound = {
            southWest: {
                latitude: bounds.getSouthWest().getLat(),
                longitude: bounds.getSouthWest().getLng()
            },
            northEast: {
                latitude: bounds.getNorthEast().getLat(),
                longitude: bounds.getNorthEast().getLng()
            }
        };

        const params = new URLSearchParams({
            mapBound: JSON.stringify(mapBound),
            page: 0,
            size: 10
        });

        const response = await fetch(`/map/regions-content?\${params.toString()}`, {
            method: "GET"
        });
        const data = await response.json();
        renderSpots(data);
        updateMap(data);

        const param = new URLSearchParams({
            mapBound: JSON.stringify(mapBound)
        });

        const responseForPage = await fetch(`/map/regions-content-total-page?\${param.toString()}`, {
            method: "GET"
        });
        const dataForPage = await responseForPage.json();
        setPagination(dataForPage.totalPages, 0);
    });

    document.getElementById('spotList').addEventListener('change', (e) => {
        if (e.target.matches('input[type="checkbox"]')) {
            const checkedCheckbox = e.target;
            const id = checkedCheckbox.value;
            const title = checkedCheckbox.parentElement.querySelector("strong").textContent;
            if (e.target.checked) updateSelected(id, title);
            else {
                const items = document.querySelectorAll('#selectedList li');
                items.forEach(li => { if (li.textContent === title) li.remove(); });
            }
        }
    });

    const container = document.getElementById('selectedList');
    let draggedItem = null;

    container.addEventListener('dragstart', (e) => {
        if (e.target.classList.contains('list-group-item')) {
            draggedItem = e.target;
            e.dataTransfer.effectAllowed = 'move';
        }
    });

    container.addEventListener('dragover', (e) => {
        e.preventDefault();
        const target = e.target;
        if (target !== draggedItem && target.classList.contains('list-group-item')) {
            const bounding = target.getBoundingClientRect();
            const offset = bounding.y + bounding.height / 2;
            if (e.clientY - offset > 0) {
                target.after(draggedItem);
            } else {
                target.before(draggedItem);
            }
        }
    });

    container.addEventListener('drop', (e) => {
        e.preventDefault();
    });

    // private Long userId;
    // private Long planId;
    // private List<Long> attractionIds;
    document.getElementById('planStoreBtn').addEventListener('click', async () => {
        const selectedList = document.querySelector("#selectedList");
        const attractionIds = Array.from(selectedList.children).map(li => li.getAttribute('value'));
        try {
            const response = await fetch('/map/plan', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    userId: id,
                    attractionIds: attractionIds
                })
            });

            if (!response.ok) throw new Error('서버 응답 실패');

            const result = await response.text();
            alert(result);
            location.reload();
        } catch (error) {
            console.error('저장 실패:', error);
            alert('저장 중 오류가 발생했습니다.');
        }
    });
</script>
</body>
</html>
