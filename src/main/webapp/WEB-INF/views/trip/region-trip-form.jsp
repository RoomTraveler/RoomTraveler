<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>지역 관광지 검색</title>
<!-- Bootstrap CSS CDN -->
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
	rel="stylesheet">
</head>
<body>
	<%@ include file="/WEB-INF/views/fragments/header.jsp" %>
	<div class="container mt-5">
		<h3 class="mb-4">지역 관광지 검색</h3>

		<!-- 검색 폼 -->
		<form action="/map/regions" method="get">

<%--			<div class="row mb-3">--%>
<%--				<div class="col-md-3">--%>
<%--					<label for="sidoDropdown" class="form-label">시/도</label> <select--%>
<%--						class="form-select" id="sidoDropdown" name="sido">--%>
<%--						<option selected disabled>시/도를 선택하세요</option>--%>
<%--						<c:forEach var="sidoItem" items="${sidoList}">--%>
<%--							<option>${sidoItem.name}</option>--%>
<%--						</c:forEach>--%>
<%--					</select>--%>
<%--				</div>--%>

<%--				<div class="col-md-3">--%>
<%--					<label for="gugunDropdown" class="form-label">구/군</label> <select--%>
<%--						class="form-select" id="gugunDropdown" name="gugun">--%>
<%--						<option selected disabled>구/군을 선택하세요</option>--%>
<%--						<c:forEach var="gugunItem" items="${gugunList}">--%>
<%--							<option>${gugunItem.name}</option>--%>
<%--						</c:forEach>--%>
<%--					</select>--%>
<%--				</div>--%>

				<div class="col-md-3">
					<label for="contentDropdown" class="form-label">컨텐츠</label> <select
						class="form-select" id="contentDropdown" name="content">
						<option selected disabled>컨텐츠를 선택하세요</option>
						<c:forEach var="contentItem" items="${contentList}">
							<option>${contentItem.name}</option>
						</c:forEach>
					</select>
				</div>
			</div>

			<!-- 검색 버튼 -->
			<div class="row">
				<div class="col text-end">
					<button type="button" class="btn btn-primary">관광지 검색</button>
				</div>
			</div>
		</form>
		
<%--		<input type="text" id="address" placeholder="주소 검색">--%>
<%--		<button id="submit">검색</button>--%>
		
		
		<input type="text" placeholder="검색" />
		<button type="button" class="btn2">검색</button>

		<div id="map" style="width: 100%; height: 600px;"></div>

		<h5 class="mt-4">검색된 관광지 목록</h5>

		<form method="get" action="${root}/trip">
			<button type="submit" class="btn btn-success mt-3">선택한 장소 여행
				타임라인에 추가</button>
			<input type="hidden" name="action" value="save-plan"> <input
				type="hidden" name="email" value="${email}">

			<!-- 여기에 관광지 리스트 동적으로 추가 -->
			<ul id="spotList" class="list-group mt-2">
				<!-- JS로 li가 채워짐 -->
			</ul>
		</form>
	</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=18dcaec57ee4149f30695dab2b7cc784"></script>
<script>
// function loadSidoList() {
//     fetch("/map/sidos")
//         .then(response => response.json())
//         .then(data => {
//             const sidoDropdown = document.getElementById("sidoDropdown");
//             sidoDropdown.innerHTML = '<option disabled selected>시도 선택</option>';
//             data.forEach(sido => {
//                 const option = document.createElement("option");
//                 option.textContent = sido.name;
//                 option.value = sido.code;
//                 sidoDropdown.appendChild(option);
//             });
//         })
//         .catch(err => {
//             console.error("시도 목록 로딩 실패", err);
//         });
// }
// 페이지 로드시 자동 실행
//window.addEventListener("DOMContentLoaded", loadSidoList);

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

// document.getElementById("sidoDropdown").addEventListener("change", function () {
//     const sidoCode = this.value;
//
//     fetch("/map/guguns?sido=" + sidoCode)
//         .then(response => response.json())
//         .then(data => {
//             const gugunDropdown = document.getElementById("gugunDropdown");
//             gugunDropdown.innerHTML = '<option disabled selected>구군 선택</option>';
//
//             data.forEach(gugun => {
//                 const option = document.createElement("option");
//                 option.textContent = gugun.name;
// 				option.value = gugun.code;
//                 gugunDropdown.appendChild(option);
//             });
//         })
//         .catch(err => {
//             console.error("구군 불러오기 실패", err);
//         });
// });

// const key_vworld = "EB8D8B97-EF71-3B8F-963D-695A17D4C65A";
// const key_sgis_service_id = "ee94f4b8a26d441eb2ad";
// const key_sgis_security = "a1e4df5956b646e3b774"; // 보안 key
// const key_data = "ouJC1TF%2FHBMbMumKwdt1AUeSiuNZvWWr%2B0yFi0JFjSu%2FNXjYHGH0mtNLdkUOE9EIZnr5eJ0H0Al09BCeEThRFQ%3D%3D";

<%--	<script src="https://sgisapi.kostat.go.kr/OpenAPI3/auth/javascriptAuth?consumer_key=ee94f4b8a26d441eb2ad"></script>--%>

document.addEventListener("DOMContentLoaded", initMap);
let map;
let mapMarkers = [];
let infoWindow;
let currentOpenInfoWindow = null;

    function initMap() {
		let currentLatitude = 33.450701;
		let currentLongitude = 126.570667;

		// HTTPS 환경에서만 작동(예외적으로 http://localhost에서는 작동)
		navigator.geolocation.getCurrentPosition(position => {
			currentLatitude = position.coords.latitude;
			currentLongitude = position.coords.longitude;

			let container = document.getElementById('map');
			let options = {
				center: new kakao.maps.LatLng(currentLatitude, currentLongitude),
				level: 6
			}
			map = new kakao.maps.Map(container, options);
		});
	}

const panTo = (x, y) => {
	// 이동할 위도 경도 위치를 생성합니다
	var moveLatLon = new kakao.maps.LatLng(x, y);

	// 지도 중심을 부드럽게 이동시킵니다
	// 만약 이동할 거리가 지도 화면보다 크면 부드러운 효과 없이 이동합니다
	map.panTo(moveLatLon);
}

const updateMap = (map, infos) => {
	const bounds = [];
	try {
		forCenterOfLat = 0.0;
		forCenterOfLon = 0.0;
		mapMarkers = [];
		infoWindows = [];
		for (let i = 0; i < infos.length; i++) {
			const info = infos[i];
			forCenterOfLat += info.x;
			forCenterOfLon += info.y;
			const markerPosition = new kakao.maps.LatLng(info.x, info.y);
			const marker = new kakao.maps.Marker({
				position: markerPosition,
				clickable: true
			});
			marker.setMap(map);

			const iwContent = `<div class="custom-infowindow">
				  <h4>\${info.title}</h4>
				  <img src="\${info.image}" alt="\${info.title}" style="max-width: 100px; height: 100px;">
				  <p><strong>주소:</strong>\${info.addr}</p>
				  <p><strong>전화번호:</strong>\${info.tel=='' ? '없음': info.tel}</p>
			  </div>`;

			const iwPosition = new kakao.maps.LatLng(info.x, info.y);
			const infowindow = new kakao.maps.InfoWindow({
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
		console.log(forCenterOfLat);
		console.log(forCenterOfLon);
		console.log(forCenterOfLat / infos.length);
		console.log(forCenterOfLon / infos.length);
		panTo(forCenterOfLat / infos.length, forCenterOfLon / infos.length);
	} catch (e) {
		console.log(e);
	}
};

const getFetch = async (url, param, isXml) => {
	try {
		const queryString = new URLSearchParams(param).toString();
		const response = await fetch(url + "?" + queryString);
		let result = "";
		if (isXml) {
			result = await response.text();
		} else {
			result = await response.json();
		}
		console.log("요청 URL: " + url, param, result);
		return result;
	} catch (e) {
		console.log(e);
		throw e;
	}
};

const renderSpots = (spots) => {
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

const searchButton = document.querySelector(".btn-primary");
searchButton.addEventListener("click", async (e) => {
	e.preventDefault();
	const sido = document.getElementById("sidoDropdown").value;
	const gugun = document.getElementById("gugunDropdown").value;
	const content = document.getElementById("contentDropdown").value;

	if (sido === "시도 선택" || gugun === "구군 선택") {
		alert("필수 필드를 선택해주세요.");
		return;
	}

	try {
		let response;
		if (content === "컨텐츠 선택") {
			response = await getFetch("/map/regions", {
				sido: sido,
				gugun: gugun
			}, false);
		} else {
			response = await getFetch("/map/regions-content", {
				sido: sido,
				gugun: gugun,
				content: content
			}, false);
		}
		renderSpots(response);
		addrInfos = [];
		response.forEach(item => {
			addrInfos.push({
				title: item.title,
				image: item.image,
				x: item.latitude,
				y: item.longitude,
				addr: item.addr1 + item.addr2,
				tel: item.tel
			});
		});

		updateMap(map, addrInfos);
	} catch (error) {
		console.error("데이터를 불러오는 중 오류 발생:", error);
	}
});
// KMP 실패함수(lps) 생성
function buildLPS(pattern) {
	const lps = Array(pattern.length).fill(0);
	let len = 0;
	let i = 1;

	while (i < pattern.length) {
		if (pattern[i] === pattern[len]) {
			len++;
			lps[i] = len;
			i++;
		} else {
			if (len !== 0) {
				len = lps[len - 1];
			} else {
				lps[i] = 0;
				i++;
			}
		}
	}
	return lps;
}

// KMP 문자열 포함 여부
function kmpSearch(text, pattern) {
	if (pattern === "") return true;  // 빈 문자열은 항상 포함

	const lps = buildLPS(pattern);
	let i = 0, j = 0;

	while (i < text.length) {
		if (pattern[j] === text[i]) {
			i++;
			j++;
		}

		if (j === pattern.length) {
			return true;  // 포함됨
		} else if (i < text.length && pattern[j] !== text[i]) {
			if (j !== 0) {
				j = lps[j - 1];
			} else {
				i++;
			}
		}
	}

	return false;  // 포함 안됨
}

let allAttractionsInMap = [];

window.addEventListener("DOMContentLoaded", () => {
	const searchButton = document.querySelector(".btn2");
	if (!searchButton) {
		console.error("btn2 못찾음");
		return;
	}

	searchButton.addEventListener("click", async (e) => {
		e.preventDefault();

		console.log("btn2 클릭됨");  // 작동확인 로그

		// 기존 로직 유지
		const sido = document.getElementById("sidoDropdown").value;
		const gugun = document.getElementById("gugunDropdown").value;
		const content = document.getElementById("contentDropdown").value;
		const keyword = document.querySelector('input[placeholder="검색"]').value.toLowerCase();

		if (sido === "시도 선택" || gugun === "구군 선택") {
			alert("필수 필드를 선택해주세요.");
			return;
		}

		try {
			const response = await getFetch("/SSAFYTrip/trip", {
				action: "searchTouristSpots",
				sido: sido,
				gugun: gugun,
				content: content
			}, false);

			const filtered = response.filter(item => kmpSearch(item.title.toLowerCase(), keyword));

			renderSpots(filtered);

			addrInfos = [];
			filtered.forEach(item => {
				const utmkXY = new sop.LatLng(item.latitude, item.longitude);
				addrInfos.push({
					title: item.title,
					image: item.image,
					x: utmkXY.x,
					y: utmkXY.y,
					addr: item.addr1 + item.addr2,
					tel: item.tel
				});
			});

			updateMap(map, addrInfos);

		} catch (error) {
			console.error("데이터를 불러오는 중 오류 발생:", error);
		}
	});
});
</script>
</body>
</html>
