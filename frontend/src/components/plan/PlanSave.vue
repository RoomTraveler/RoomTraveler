<template>
  <Header />
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
                <select class="form-select" id="contentDropdown" name="content" v-model="selectedContentType">
                  <option disabled selected>선택하세요</option>
                  <option v-for="contentType in contentTypes" :value="contentType.code" :key="contentType">
                    {{ contentType.name }}
                  </option>
                </select>
              </div>
              <div class="col-md-4">
                <label for="keywordInput" class="form-label">키워드 검색</label>
                <div class="input-group">
                  <input type="text" id="keywordInput" class="form-control" placeholder="검색어 입력(2자 이상)" />
                </div>
              </div>
              <div class="col-md-4 align-self-end text-end">
                <button type="button" id="attractionBtn" class="btn btn-primary" @click="searchAttractions">
                  관광지 검색
                </button>
              </div>
            </form>
          </div>
        </div>

        <!-- 검색 결과 및 페이지네이션 -->
        <div class="row" style="height: 70vh">
          <div class="col-md-4">
            <h5>검색된 관광지 목록</h5>
            <ul class="list-group mb-2" style="max-height: 500px; overflow-y: auto">
              <li
                v-for="(spot, index) in spots"
                :key="spot.no"
                @click="triggerMarkerClick(index)"
                style="cursor: pointer"
                class="spot-item"
              >
                <label>
                  <input type="checkbox" :value="spot" v-model="selectedPlaces" />
                  <strong>{{ spot.title }}</strong>
                </label>

                <span @click.stop="toggleLike(spot.no)" style="color: red; cursor: pointer; margin-left: 10px">
                  {{ likedAttractions[spot.no] ? "❤️" : "🤍" }} {{ likeCounts[spot.no] }}
                </span>

                <br />
                <img
                  v-if="spot.image2 && spot.image2.trim().length !== 0"
                  :src="spot.image2"
                  class="img-thumbnail"
                  style="max-width: 100px; height: 100px"
                /><br />
                {{ spot.addr1 || "" }} {{ spot.addr2 || "" }}<br />
                {{ spot.tel || "" }}
                <input type="hidden" name="latitude" :value="spot.latitude" />
                <input type="hidden" name="longitude" :value="spot.longitude" />

                <button class="btn btn-sm btn-outline-primary mt-2" @click="openModal(spot.no)">자세히 보기</button>
              </li>
            </ul>
            <nav aria-label="Page navigation">
              <ul id="pagination" class="pagination">
                <li class="page-item" :class="{ disabled: currentPage === 1 }">
                  <a class="page-link" href="#" @click.prevent="goToPage(currentPage - 1)">&lt;</a>
                </li>

                <li class="page-item" v-for="page in pageNumbers" :key="page" :class="{ active: page === currentPage }">
                  <a class="page-link" href="#" @click.prevent="goToPage(page)">
                    {{ page }}
                  </a>
                </li>

                <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                  <a class="page-link" href="#" @click.prevent="goToPage(currentPage + 1)">&gt;</a>
                </li>
              </ul>
            </nav>
          </div>
          <div class="col-md-8">
            <div ref="mapContainer" style="width: 100%; height: 100%" class="border"></div>
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
            <draggable
              v-model="selectedPlaces"
              class="list-group"
              ghost-class="ghost"
              @start="dragging = true"
              @end="dragging = false"
              item-key="title"
            >
              <template #item="{ element, index }">
                <div class="list-group-item d-flex justify-between items-center">
                  <span>{{ element.title }}</span>
                  <button @click="removePlace(index)" class="btn btn-danger btn-sm">삭제</button>
                </div>
              </template>
            </draggable>
            <label>
              <input v-model="isShared" type="checkbox" name="is_shared" />
              모두와 공유하기
            </label>
            <br />
            <label for="datePicker">날짜 선택:</label>
            <input type="date" id="datePicker" v-model="selectedDate" :min="minDate" />
            <button type="button" class="btn btn-success w-100" @click="savePlan">선택한 장소 저장</button>
          </div>
        </div>

        <div>
          <button
            v-if="selectedPlaces.length > 2"
            @click="evaluatePlan"
            class="btn btn-success btn-lg w-100 mb-3 d-flex align-items-center justify-content-center"
            :disabled="isLoading"
          >
            <i v-if="isLoading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></i>
            <i v-else class="bi bi-check-circle-fill me-2"></i>
            {{ isLoading ? "평가 중..." : "여행 계획 평가받기" }}
          </button>

          <div
            v-if="evaluationResult.advantages || evaluationResult.disadvantages || evaluationResult.recommendations"
            class="mt-3 evaluation-container"
          >
            <div class="evaluation-header"><i class="bi bi-robot"></i> 계획 평가 및 개선 추천</div>

            <div v-if="evaluationResult.advantages" class="evaluation-section advantages-section">
              <div class="section-header"><i class="bi bi-plus-circle-fill"></i> 장점</div>
              <div class="section-content">{{ evaluationResult.advantages }}</div>
            </div>

            <div v-if="evaluationResult.disadvantages" class="evaluation-section disadvantages-section">
              <div class="section-header"><i class="bi bi-dash-circle-fill"></i> 단점</div>
              <div class="section-content">{{ evaluationResult.disadvantages }}</div>
            </div>

            <div v-if="evaluationResult.recommendations" class="evaluation-section recommendations-section">
              <div class="section-header"><i class="bi bi-lightbulb-fill"></i> 추천</div>
              <div class="section-content">{{ evaluationResult.recommendations }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <ModalWrapper v-if="showModal" :title="modalTitle" @close="showModal = false">
      <!-- AttractionDetail 컴포넌트를 슬롯에 넣고, id 전달 -->
      <AttractionDetail :id="modalSpotId" />
    </ModalWrapper>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import draggable from "vuedraggable";
import apiGroup from "@/api/index";
import ModalWrapper from "@/components/attraction/ModelWrapper.vue";
import AttractionDetail from "@/components/attraction/AttractionDetail.vue";
import Header from "@/components/layout/Header.vue";

// 상태 관리
const mapContainer = ref(null);
const mapBound = ref(null);
const selectedPlaces = ref([]); // { id, title, latitude, longitude }
const userId = ref(1); // 아이디 일단 번호로 설정, 나중엔 JWT에서 가져오는 식이 되면 좋을 것
const isShared = ref(false);
const minDate = ref("");
const selectedDate = ref("");
const contentTypes = ref([]);
const selectedContentType = ref("");
const keywordText = ref("");
const spots = ref([]);
const currentPage = ref(1);
const totalPages = ref(0);
const savePolyline = ref(null);
const imageSize = ref(null);
const imageOption = ref(null);
const likedAttractions = ref({});
const likeCounts = ref({});

// 상수 설정
let map = null;
let mapMarkers = [];
let infoWindows = [];
let currentOpenInfoWindow = null;
const blockSize = 5;
const markerIcons = {
  12: "/img/attractionIcon.png",
  14: "/img/cultureIcon.png",
  15: "/img/festivalIcon.png",
  25: "/img/tripCourseIcon.png",
  28: "/img/leisureSportIcon.png",
  32: "/img/lodgmentIcon.png",
  38: "/img/shoppingIcon.png",
  39: "/img/restaurantIcon.png",
};

const evaluationResult = ref({
  advantages: "",
  disadvantages: "",
  recommendations: "",
});

const isLoading = ref(false);

const evaluatePlan = async () => {
  isLoading.value = true;
  try {
    const response = await apiGroup.api({
      url: "/api/ai/evaluation",
      method: "POST",
      data: {
        selectedPlaces: selectedPlaces.value.map((item) => ({
          address: `${item.addr1}${item.addr2 ?? ""}`.trim(),
          name: item.title,
          contentType: contentTypeMap[item.contentTypeId] ?? "기타",
          latitude: item.latitude,
          longitude: item.longitude,
        })),
      },
    });
    isLoading.value = false;
    // Parse the response to separate advantages, disadvantages, and recommendations
    const fullText = response.data;

    // Initialize with empty strings
    const parsedResult = {
      advantages: "",
      disadvantages: "",
      recommendations: "",
    };

    // Find the sections in the text
    const advantagesMatch = fullText.match(/장점[:\s]+([\s\S]+?)(?=단점[:\s]+|$)/i);
    const disadvantagesMatch = fullText.match(/단점[:\s]+([\s\S]+?)(?=추천[:\s]+|$)/i);
    const recommendationsMatch = fullText.match(/추천[:\s]+([\s\S]+?)(?=$)/i);

    // Extract the content if matches are found
    if (advantagesMatch && advantagesMatch[1]) {
      parsedResult.advantages = advantagesMatch[1].trim();
    }

    if (disadvantagesMatch && disadvantagesMatch[1]) {
      parsedResult.disadvantages = disadvantagesMatch[1].trim();
    }

    if (recommendationsMatch && recommendationsMatch[1]) {
      parsedResult.recommendations = recommendationsMatch[1].trim();
    }

    // If no structured content was found, use the full text as recommendations
    if (!parsedResult.advantages && !parsedResult.disadvantages && !parsedResult.recommendations) {
      parsedResult.recommendations = fullText.trim();
    }

    evaluationResult.value = parsedResult;
  } catch (err) {
    console.error("평가 요청 실패", err);
  }
};

// 계산된 속성들
const startPage = computed(() => {
  const currentBlock = Math.floor((currentPage.value - 1) / blockSize);
  return currentBlock * blockSize + 1;
});

const endPage = computed(() => {
  return Math.min(startPage.value + blockSize - 1, totalPages.value);
});

const pageNumbers = computed(() => {
  const pages = [];
  for (let i = startPage.value; i <= endPage.value; i++) {
    pages.push(i);
  }
  return pages;
});

const showModal = ref(false);
const modalSpotId = ref(null);
const modalTitle = ref("");

// “자세히 보기” 버튼 클릭
function openModal(id) {
  modalSpotId.value = id;
  showModal.value = true;
}

function triggerMarkerClick(index) {
  window.kakao.maps.event.trigger(mapMarkers[index], "click");
}

// 라이프사이클 훅
onMounted(async () => {
  setMinDate();
  await loadContentList();
  await loadKakaoMapScript();
});

// 초기화 함수들
const setMinDate = () => {
  const today = new Date();
  const yyyy = today.getFullYear();
  const mm = String(today.getMonth() + 1).padStart(2, "0");
  const dd = String(today.getDate()).padStart(2, "0");
  minDate.value = `${yyyy}-${mm}-${dd}`;
};

const loadKakaoMapScript = async () => {
  if (!window.kakao || !window.kakao.maps) {
    await new Promise((resolve) => {
      const script = document.createElement("script");
      script.src = "https://dapi.kakao.com/v2/maps/sdk.js?appkey=a1b7d43f74e8d7c4fa60d02ce2c13f58&autoload=false";
      script.onload = () => {
        window.kakao.maps.load(() => {
          resolve();
        });
      };
      document.head.appendChild(script);
    });
  }

  // 이 아래는 스크립트가 로드된 뒤 실행돼야 함
  const centerLatLng = { latitude: 35.205432, longitude: 126.811591 };
  const options = {
    center: new window.kakao.maps.LatLng(centerLatLng.latitude, centerLatLng.longitude),
    level: 6,
  };
  map = new window.kakao.maps.Map(mapContainer.value, options);
  imageSize.value = new window.kakao.maps.Size(30, 30);
  imageOption.value = { offset: new window.kakao.maps.Point(15, 30) };

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition((pos) => {
      const { latitude, longitude } = pos.coords;
      map.setCenter(new window.kakao.maps.LatLng(latitude, longitude));
    });
  }
};

const loadContentList = async () => {
  try {
    const response = await apiGroup.apiNoAuth({
      url: "/api/map/content-types",
      method: "GET",
    });
    const data = await response.data;
    contentTypes.value = data;
  } catch (err) {
    console.error("컨텐츠 목록 로딩 실패", err);
  }
};

// 유틸리티 함수
const containsNonKorean = (text) => {
  // 한글 이외의 문자가 하나라도 있으면 true
  return /[^가-힣]/.test(text);
};

const searchAttractions = () => {
  fetchTourSpots(0, true);
};

const fetchTourSpots = async (pageIndex, mapBoundUpdate) => {
  let contentTypeValue = parseInt(selectedContentType.value) || -1;

  if (![12, 14, 15, 25, 28, 32, 38, 39].includes(contentTypeValue)) {
    contentTypeValue = -1;
  }

  let keyword = keywordText.value;
  if (keyword.length < 2 || containsNonKorean(keyword)) {
    keyword = "";
  }

  // 지도의 현재 bounds 업데이트
  if (mapBoundUpdate && map) {
    const bounds = map.getBounds();
    mapBound.value = {
      southWest: {
        latitude: bounds.getSouthWest().getLat(),
        longitude: bounds.getSouthWest().getLng(),
      },
      northEast: {
        latitude: bounds.getNorthEast().getLat(),
        longitude: bounds.getNorthEast().getLng(),
      },
    };
  }

  const params = new URLSearchParams({
    mapBound: JSON.stringify(mapBound.value),
    page: pageIndex,
    size: 10,
    contentType: contentTypeValue,
    keyword: keyword,
  });

  try {
    const response = await apiGroup.api({
      url: `/api/map/region-contents?${params.toString()}`,
      method: "GET",
    });

    const data = response.data;
    spots.value = data || [];
    likedAttractions.value = data.reduce((acc, item) => {
      acc[item.no] = item.attractionLikeId !== 0;
      return acc;
    }, {});

    likeCounts.value = data.reduce((acc, item) => {
      acc[item.no] = item.likes;
      return acc;
    }, {});

    totalPages.value = Math.ceil(data[0].totalCount / 10) || 1;
    currentPage.value = pageIndex + 1;
    updateMap(data || []);
    //setPagination(Math.ceil(data[0].totalCount / 10), 1)
  } catch (error) {
    console.error("관광지 데이터 가져오기 실패", error);
  }
};

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  fetchTourSpots(page - 1, false);
};

const removeMarkers = () => {
  for (let i = 0; i < mapMarkers.length; i++) {
    mapMarkers[i].setMap(null);
  }
  mapMarkers = [];
  infoWindows = [];
};

const updateMap = (spots) => {
  try {
    removeMarkers();

    if (!window.kakao || !window.kakao.maps || !map) {
      console.error("카카오맵 API가 로드되지 않았거나 맵이 초기화되지 않았습니다.");
      return;
    }

    spots.forEach((info, i) => {
      const markerImage = new window.kakao.maps.MarkerImage(
        markerIcons[info.contentTypeId],
        imageSize.value,
        imageOption.value
      );

      const markerPosition = new window.kakao.maps.LatLng(info.latitude, info.longitude);
      const marker = new window.kakao.maps.Marker({
        position: markerPosition,
        clickable: true,
        image: markerImage,
      });
      marker.setMap(map);

      const iwContent = `
    <div class="custom-infowindow">
      <h4>${info.title}</h4>
      <img src="${info.image}" alt="${info.title}" style="max-width: 100px; height: 100px;">
      <p><strong>주소:</strong>${info.addr1 + " " + (info.addr2 || "")}</p>
      <p><strong>전화번호:</strong>${info.tel ? info.tel : "없음"}</p>
    </div>`;

      const infoWindow = new window.kakao.maps.InfoWindow({
        position: markerPosition,
        content: iwContent,
      });

      window.kakao.maps.event.addListener(marker, "click", () => {
        // 현재 클릭한 게 이미 열려있는 인포윈도우라면 -> 닫기
        if (currentOpenInfoWindow === infoWindow) {
          infoWindow.close();
          currentOpenInfoWindow = null;
        } else {
          // 다른 인포윈도우가 열려있으면 닫기
          if (currentOpenInfoWindow) {
            currentOpenInfoWindow.close();
          }
          // 새로운 인포윈도우 열기
          infoWindow.open(map, marker);
          currentOpenInfoWindow = infoWindow;

          // 닫기 버튼 눌렀을 때 상태 초기화
          window.kakao.maps.event.addListener(infoWindow, "closeclick", () => {
            currentOpenInfoWindow = null;
          });
        }
      });

      mapMarkers.push(marker);
      infoWindows.push(infoWindow);
    });
  } catch (e) {
    console.error("지도 업데이트 중 오류 발생:", e);
  }
};

const contentTypeMap = {
  12: "관광지",
  14: "문화시설",
  15: "축제공연행사",
  25: "여행코스",
  28: "레포츠",
  32: "숙박",
  38: "쇼핑",
  39: "음식점",
};

function removePlace(index) {
  selectedPlaces.value.splice(index, 1);
}

watch(selectedPlaces, (newVal, oldVal) => {
  const unique = [];
  const seenTitles = new Set();
  for (const place of newVal) {
    if (!seenTitles.has(place.title)) {
      seenTitles.add(place.title);
      unique.push(place);
    }
  }
  if (unique.length !== newVal.length) {
    selectedPlaces.value = unique;
  }

  drawLines();
});

const saveMarkers = ref([]);

const drawLines = () => {
  if (!window.kakao || !window.kakao.maps || !map) return;

  if (savePolyline.value) {
    savePolyline.value.setMap(null);
  }

  saveMarkers.value.forEach((marker) => {
    marker.setMap(null);
  });
  saveMarkers.value = [];

  if (selectedPlaces.value.length >= 2) {
    const linePath = selectedPlaces.value.map((place) => new window.kakao.maps.LatLng(place.latitude, place.longitude));

    const polyline = new window.kakao.maps.Polyline({
      path: linePath,
      strokeWeight: 3,
      strokeColor: "#005666",
      strokeOpacity: 0.7,
      strokeStyle: "solid",
    });

    polyline.setMap(map);
    savePolyline.value = polyline;

    selectedPlaces.value.forEach((place, idx) => {
      const position = new window.kakao.maps.LatLng(place.latitude, place.longitude);

      // HTML 컨텐츠: 번호를 표시할 DIV
      const content = `<div style="
  background: #3f51b5;
  color: white;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  font-weight: bold;
  box-shadow: 0 0 5px rgba(0,0,0,0.3);
">
  ${idx + 1}
</div>`;
      const overlay = new window.kakao.maps.CustomOverlay({
        position,
        content,
        yAnchor: 1, // 숫자가 마커 꼭대기에 붙도록 조정
      });
      overlay.setMap(map);
      saveMarkers.value.push(overlay);
    });
  }
};

const savePlan = async () => {
  if (selectedPlaces.value.length === 0) {
    alert("선택된 장소가 없습니다.");
    return;
  }

  try {
    const planData = {
      userId: userId.value,
      travelDate: selectedDate.value,
      isShared: isShared.value ? 1 : 0,
      attractionIds: selectedPlaces.value.map((place) => place.no),
    };

    const response = await apiGroup.api({
      url: "/api/map/plans",
      method: "POST",
      data: planData,
    });

    const result = response.data;
    alert(result);

    // 저장 후 선택 목록 초기화
    selectedPlaces.value = [];

    // 경로선 제거
    if (savePolyline.value) {
      savePolyline.value.setMap(null);
      savePolyline.value = null;
    }
  } catch (error) {
    console.error("여행 계획 저장 중 오류 발생:", error);
    alert("여행 계획 저장에 실패했습니다.");
  }
};

const toggleLike = async (attractionId) => {
  try {
    const response = await apiGroup.api({
      url: `/api/map/likes/attractions/${attractionId}`,
      method: "POST",
    });
    if (likedAttractions.value[attractionId]) {
      likeCounts.value[attractionId]--;
    } else {
      likeCounts.value[attractionId]++;
    }
    likedAttractions.value[attractionId] = !likedAttractions.value[attractionId];
  } catch (error) {
    console.error("좋아요 토글 실패:", error);
    alert("좋아요 처리 중 오류 발생");
  }
};
</script>

<style scoped>
.custom-infowindow {
  padding: 10px;
  max-width: 300px;
}

.list-group-item {
  cursor: pointer;
}

.list-group-item:hover {
  background-color: #f8f9fa;
}
.spot-item:hover {
  background-color: #d9f99d;
}

.evaluation-container {
  background-color: #1a1a1a;
  border-radius: 8px;
  overflow: hidden;
  font-family: "Courier New", monospace;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  margin-bottom: 20px;
}

.evaluation-header {
  background-color: #333;
  color: #0f0;
  padding: 12px 15px;
  font-size: 1.2rem;
  font-weight: bold;
  letter-spacing: 1px;
  border-bottom: 1px solid #444;
  display: flex;
  align-items: center;
}

.evaluation-header i {
  margin-right: 10px;
}

.evaluation-section {
  margin: 0;
  border-bottom: 1px solid #333;
}

.section-header {
  padding: 10px 15px;
  font-weight: bold;
  display: flex;
  align-items: center;
  color: #fff;
  background-color: #222;
}

.section-header i {
  margin-right: 10px;
}

.advantages-section .section-header i {
  color: #4caf50;
}

.disadvantages-section .section-header i {
  color: #f44336;
}

.recommendations-section .section-header i {
  color: #2196f3;
}

.section-content {
  white-space: pre-line;
  line-height: 1.6;
  font-size: 0.95rem;
  padding: 15px;
  color: #ddd;
  background-color: #2a2a2a;
}

.section-content p {
  margin-bottom: 10px;
}
</style>
