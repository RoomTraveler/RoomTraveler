<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>숙소 등록</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .form-container {
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f8f9fa;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        .required-field::after {
            content: " *";
            color: red;
        }
        .image-preview {
            width: 100%;
            height: 200px;
            border: 1px solid #ddd;
            margin-top: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #f8f9fa;
            overflow: hidden;
        }
        .image-preview img {
            max-width: 100%;
            max-height: 100%;
        }
    </style>
</head>
<body>
    <jsp:include page="../fragments/header.jsp" />

    <div class="container mt-5 mb-5">
        <h2 class="text-center mb-4">숙소 등록</h2>

        <div class="form-container">
            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <form action="${pageContext.request.contextPath}/accommodation/register" method="post" enctype="multipart/form-data">
                <div class="mb-3">
                    <label for="title" class="form-label required-field">숙소 이름</label>
                    <input type="text" class="form-control" id="title" name="title" value="${accommodation.title}" required>
                </div>

                <div class="mb-3">
                    <label for="description" class="form-label required-field">숙소 설명</label>
                    <textarea class="form-control" id="description" name="description" rows="5" required>${accommodation.description}</textarea>
                </div>

                <div class="mb-3">
                    <label for="address" class="form-label required-field">주소</label>
                    <input type="text" class="form-control" id="address" name="address" value="${accommodation.address}" required>
                </div>

                <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="sidoCode" class="form-label required-field">시/도</label>
                        <select class="form-select" id="sidoCode" name="sidoCode" required>
                            <option value="">시/도 선택</option>
                            <!-- 시도 목록은 JavaScript로 동적 로드 -->
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label for="gugunCode" class="form-label required-field">구/군</label>
                        <select class="form-select" id="gugunCode" name="gugunCode" required>
                            <option value="">구/군 선택</option>
                            <!-- 구군 목록은 시도 선택 시 JavaScript로 동적 로드 -->
                        </select>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="latitude" class="form-label">위도</label>
                        <input type="text" class="form-control" id="latitude" name="latitude" value="${accommodation.latitude}">
                        <div class="form-text">지도에서 위치를 선택하면 자동으로 입력됩니다.</div>
                    </div>
                    <div class="col-md-6">
                        <label for="longitude" class="form-label">경도</label>
                        <input type="text" class="form-control" id="longitude" name="longitude" value="${accommodation.longitude}">
                        <div class="form-text">지도에서 위치를 선택하면 자동으로 입력됩니다.</div>
                    </div>
                </div>

                <div class="mb-3">
                    <label for="map" class="form-label">지도에서 위치 선택</label>
                    <div id="map" style="width:100%;height:400px;"></div>
                </div>

                <div class="mb-3">
                    <label for="accommodationType" class="form-label required-field">숙소 유형</label>
                    <select class="form-select" id="accommodationType" name="accommodationType" required>
                        <option value="">숙소 유형 선택</option>
                        <option value="HOTEL" ${accommodation.accommodationType eq 'HOTEL' ? 'selected' : ''}>호텔</option>
                        <option value="MOTEL" ${accommodation.accommodationType eq 'MOTEL' ? 'selected' : ''}>모텔</option>
                        <option value="PENSION" ${accommodation.accommodationType eq 'PENSION' ? 'selected' : ''}>펜션</option>
                        <option value="GUEST_HOUSE" ${accommodation.accommodationType eq 'GUEST_HOUSE' ? 'selected' : ''}>게스트하우스</option>
                        <option value="RESORT" ${accommodation.accommodationType eq 'RESORT' ? 'selected' : ''}>리조트</option>
                        <option value="CONDO" ${accommodation.accommodationType eq 'CONDO' ? 'selected' : ''}>콘도</option>
                        <option value="HANOK" ${accommodation.accommodationType eq 'HANOK' ? 'selected' : ''}>한옥</option>
                        <option value="CAMPING" ${accommodation.accommodationType eq 'CAMPING' ? 'selected' : ''}>캠핑/글램핑</option>
                        <option value="OTHER" ${accommodation.accommodationType eq 'OTHER' ? 'selected' : ''}>기타</option>
                    </select>
                </div>

                <div class="mb-3">
                    <label for="phone" class="form-label">전화번호</label>
                    <input type="tel" class="form-control" id="phone" name="phone" value="${accommodation.phone}" 
                           placeholder="예: 02-1234-5678 또는 010-1234-5678">
                </div>

                <div class="mb-3">
                    <label for="email" class="form-label">이메일</label>
                    <input type="email" class="form-control" id="email" name="email" value="${accommodation.email}">
                </div>

                <div class="mb-3">
                    <label for="website" class="form-label">웹사이트</label>
                    <input type="url" class="form-control" id="website" name="website" value="${accommodation.website}" 
                           placeholder="예: https://www.example.com">
                </div>

                <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="checkInTime" class="form-label">체크인 시간</label>
                        <input type="time" class="form-control" id="checkInTime" name="checkInTime" value="${accommodation.checkInTime}">
                    </div>
                    <div class="col-md-6">
                        <label for="checkOutTime" class="form-label">체크아웃 시간</label>
                        <input type="time" class="form-control" id="checkOutTime" name="checkOutTime" value="${accommodation.checkOutTime}">
                    </div>
                </div>

                <div class="mb-3">
                    <label for="amenities" class="form-label">편의시설</label>
                    <textarea class="form-control" id="amenities" name="amenities" rows="3" 
                              placeholder="예: 와이파이, 주차장, 수영장, 조식 등">${accommodation.amenities}</textarea>
                </div>

                <div class="mb-3">
                    <label for="imageFiles" class="form-label required-field">숙소 이미지</label>
                    <input type="file" class="form-control" id="imageFiles" name="imageFiles" multiple accept="image/*" required>
                    <div class="form-text">최소 1개 이상의 이미지를 등록해주세요. 첫 번째 이미지가 대표 이미지로 설정됩니다.</div>
                    <div id="imagePreviewContainer" class="d-flex flex-wrap mt-2"></div>
                </div>

                <div class="alert alert-info">
                    <p><strong>숙소 등록 안내:</strong></p>
                    <ul>
                        <li>숙소 등록 후 관리자 승인이 필요합니다.</li>
                        <li>정확한 정보를 입력해주세요.</li>
                        <li>숙소 등록 후 객실을 추가로 등록할 수 있습니다.</li>
                    </ul>
                </div>

                <div class="d-grid gap-2">
                    <button type="submit" class="btn btn-primary">숙소 등록</button>
                    <a href="${pageContext.request.contextPath}/accommodation/host/accommodations" class="btn btn-secondary">취소</a>
                </div>
            </form>
        </div>
    </div>

    <jsp:include page="../fragments/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
    <script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_KAKAO_MAP_API_KEY&libraries=services"></script>
    <script>
        // 이미지 미리보기
        document.getElementById('imageFiles').addEventListener('change', function(e) {
            const previewContainer = document.getElementById('imagePreviewContainer');
            previewContainer.innerHTML = '';
            
            for (const file of this.files) {
                if (file) {
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        const previewDiv = document.createElement('div');
                        previewDiv.className = 'image-preview me-2 mb-2';
                        
                        const img = document.createElement('img');
                        img.src = e.target.result;
                        
                        previewDiv.appendChild(img);
                        previewContainer.appendChild(previewDiv);
                    }
                    reader.readAsDataURL(file);
                }
            }
        });

        // 시도 및 구군 데이터 로드
        window.addEventListener('DOMContentLoaded', function() {
            // 시도 데이터 로드
            fetch('${pageContext.request.contextPath}/api/regions/sido')
                .then(response => response.json())
                .then(data => {
                    const sidoSelect = document.getElementById('sidoCode');
                    data.forEach(sido => {
                        const option = document.createElement('option');
                        option.value = sido.sidoCode;
                        option.textContent = sido.sidoName;
                        sidoSelect.appendChild(option);
                    });
                })
                .catch(error => console.error('시도 데이터 로드 실패:', error));
        });

        // 시도 선택 시 구군 데이터 로드
        document.getElementById('sidoCode').addEventListener('change', function() {
            const sidoCode = this.value;
            const gugunSelect = document.getElementById('gugunCode');
            
            // 기존 옵션 제거
            gugunSelect.innerHTML = '<option value="">구/군 선택</option>';
            
            if (sidoCode) {
                fetch(`${pageContext.request.contextPath}/api/regions/gugun?sidoCode=${sidoCode}`)
                    .then(response => response.json())
                    .then(data => {
                        data.forEach(gugun => {
                            const option = document.createElement('option');
                            option.value = gugun.gugunCode;
                            option.textContent = gugun.gugunName;
                            gugunSelect.appendChild(option);
                        });
                    })
                    .catch(error => console.error('구군 데이터 로드 실패:', error));
            }
        });

        // 카카오맵 초기화
        let map, marker;
        
        window.addEventListener('DOMContentLoaded', function() {
            const mapContainer = document.getElementById('map');
            const mapOption = {
                center: new kakao.maps.LatLng(37.566826, 126.9786567), // 서울 시청
                level: 3
            };
            
            map = new kakao.maps.Map(mapContainer, mapOption);
            
            // 주소-좌표 변환 객체
            const geocoder = new kakao.maps.services.Geocoder();
            
            // 지도 클릭 이벤트
            kakao.maps.event.addListener(map, 'click', function(mouseEvent) {
                const latlng = mouseEvent.latLng;
                
                // 마커 생성 또는 이동
                if (!marker) {
                    marker = new kakao.maps.Marker({
                        position: latlng,
                        map: map
                    });
                } else {
                    marker.setPosition(latlng);
                }
                
                // 위도, 경도 입력
                document.getElementById('latitude').value = latlng.getLat();
                document.getElementById('longitude').value = latlng.getLng();
            });
            
            // 주소 입력 시 지도에 표시
            document.getElementById('address').addEventListener('blur', function() {
                const address = this.value;
                if (address) {
                    geocoder.addressSearch(address, function(result, status) {
                        if (status === kakao.maps.services.Status.OK) {
                            const coords = new kakao.maps.LatLng(result[0].y, result[0].x);
                            
                            // 마커 생성 또는 이동
                            if (!marker) {
                                marker = new kakao.maps.Marker({
                                    position: coords,
                                    map: map
                                });
                            } else {
                                marker.setPosition(coords);
                            }
                            
                            // 지도 중심 이동
                            map.setCenter(coords);
                            
                            // 위도, 경도 입력
                            document.getElementById('latitude').value = result[0].y;
                            document.getElementById('longitude').value = result[0].x;
                        }
                    });
                }
            });
        });
    </script>
</body>
</html>