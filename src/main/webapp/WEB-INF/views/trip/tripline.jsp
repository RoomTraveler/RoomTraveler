<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Plans</title>
</head>
<body>
    <%@ include file="/WEB-INF/views/fragments/header.jsp" %>
    
    <h3>여행 시작 지점 설정</h3>
	<div style="margin-bottom: 20px;">
	  <input type="text" id="startPointInput" placeholder="출발지를 입력하세요" style="width: 300px;" />
	  <button onclick="getPlaceByKeywordByKakao()">검색</button>
	  
	  <select id="startPointSelect" onchange="setStartPoint()" style="display:none; margin-left: 10px;">
	    <option disabled selected>장소 선택</option>
	  </select>
	
	  <div id="selectedStartPoint" style="margin-top: 10px;"></div>
	  
	  <!-- 선택된 출발지 위도/경도 hidden input으로 저장 -->
	  <input type="hidden" id="startLat" name="startLat" />
	  <input type="hidden" id="startLng" name="startLng" />
	</div>
    
    <ol>
        <c:forEach items="${planList}" var="plan" varStatus="status">
            <li>
                <strong>Plan </strong><br>
                <ul>
                    <c:forEach items="${plan.attractions}" var="attraction">
                        <li class="attraction-item">
                       	  관광지명 : <span class="title">${attraction.title}</span><br>
	                      이미지: <img src="${attraction.image}" style="max-width: 100px; height: 100px;" /><br>
	                      주소: <span class="addr1">${attraction.addr1}</span> <span class="addr2">${attraction.addr2}</span><br>
	                      전화번호: <span class="tel">${attraction.tel}</span>
	                      <input type="hidden" class="latitude" value="${attraction.lat}" />
	                      <input type="hidden" class="longitude" value="${attraction.lon}" />
                        </li>
                    </c:forEach>
                </ul>
                <button onclick='fetchShortestPlan(this)'>최적 경로 보기</button>
                <div class="optimal-route" style="margin-top: 10px;"></div>
            </li>
        </c:forEach>
    </ol>
    <%@ include file="/fragments/footer.jsp"%>
    <script>
    
		function fetchShortestPlan(buttonEl) {
			const startLat = document.getElementById("startLat").value;
			const startLng = document.getElementById("startLng").value;
			
		    if (!startLat || !startLng) {
		      alert("출발지를 먼저 검색하고 선택해주세요!");
		      return;
		    }
			
			const li = buttonEl.closest('li');
		    const attractionItems = li.querySelectorAll('.attraction-item');
		    
		    const locations = Array.from(attractionItems).map(item => ({
		        title: item.querySelector('.title')?.innerText || '',
		        latitude: parseFloat(item.querySelector('.latitude')?.value),
		        longitude: parseFloat(item.querySelector('.longitude')?.value)
		    }));
		    
		    if (locations.length > 12) {
		    	  alert("최대 12개의 관광지를 가지는 계획만을 지원합니다.");
		    	  return;
		    }
		    
		    const start = {"latitude": startLat, "longitude": startLng};
		    const requestBody = {
		            start,
		            locations
	        };
		    
		    fetch("trip?action=find-shortestPlan", {
		        method: "POST",
		        headers: {
		            "Content-Type": "application/json"
		        },
		        body: JSON.stringify(requestBody)
		    })
		    .then(res => res.json())
		    .then(result => renderRoute(result, li.querySelector('.optimal-route')))
		    .catch(err => console.error("에러 발생:", err));
		}
		
		function renderRoute(routeList, container) {
			container.innerHTML = '';
		    const ol = document.createElement('ol');

		    routeList.forEach(loc => {
		        const li = document.createElement('li');
		        li.innerHTML = `
		            <strong>\${loc.title}</strong><br>
		        `;
		        ol.appendChild(li);
		    });

		    container.appendChild(ol);
		}
		
		async function getPlaceByKeywordByKakao() {
		      const address = document.getElementById("startPointInput").value.trim();
		      const select = document.getElementById("startPointSelect");
		      const resultDiv = document.getElementById("result");
		      if (!address) {
		        alert("주소를 입력하세요.");
		        return;
		      }

		      try {
		    	  const url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + encodeURIComponent(address);
		          const response = await fetch(url, {
		          method: "GET",
		          headers: {
		            "Authorization": "KakaoAK 9e05c935f1bf036d218c3f7ade0d979f"
		          }
		        });

		        const data = await response.json();

		        if (data.documents.length === 0) {
		          alert("검색 결과가 없습니다.");
		          select.style.display = "none";
		          return;
		        }

		        select.innerHTML = `<option disabled selected>장소 선택</option>`;
		        data.documents.forEach(place => {
		          const option = document.createElement("option");
		          option.value = `\${place.y},\${place.x},\${place.place_name}`;
		          option.textContent = `\${place.place_name}`;
		          select.appendChild(option);
		        });

		        select.style.display = "inline-block";
		      } catch (error) {
		        console.error("주소 검색 실패:", error);
		        alert("검색 중 오류가 발생했습니다.");
		      }
		    }
		
		function setStartPoint() {
			  const selected = document.getElementById("startPointSelect").value;
			  const [lat, lng, name] = selected.split(",");

			  document.getElementById("startLat").value = lat;
			  document.getElementById("startLng").value = lng;

			  document.getElementById("selectedStartPoint").innerHTML =
			    `<strong>선택된 출발지:</strong> \${name}`;
			}
	</script>
</body>
</html>