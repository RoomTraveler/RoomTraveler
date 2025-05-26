<template>
  <div class="container-fluid mt-4">
    <h2>Squad(ID: {{ squadId }})</h2>
    <div class="d-flex flex-row flex-nowrap align-items-center">
      <div v-for="member in squadMembers" :key="member.userId" class="d-flex align-items-center me-3">
        <img :src="cursorImageMap[member.position]" alt="cursor" style="width: 16px; height: 16px" class="me-1" />
        <span>{{ member.username }}</span>
      </div>
    </div>
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
                  v-if="spot.image.trim().length !== 0"
                  :src="spot.image"
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
            <template v-if="squadMembers.length - 1 === position">
              <label>
                <input v-model="isShared" type="checkbox" name="is_shared" />
                모두와 공유하기
              </label>
              <br />
              <label for="datePicker">날짜 선택:</label>
              <input type="date" id="datePicker" v-model="selectedDate" :min="minDate" />
              <button type="button" class="btn btn-success w-100" @click="savePlan">선택한 장소 저장</button>
            </template>
          </div>
        </div>
      </div>
    </div>

    <div v-if="joinMessage" class="join-popup">
      {{ joinMessage }}
    </div>

    <ModalWrapper v-if="showModal" :title="modalTitle" @close="showModal = false">
      <!-- AttractionDetail 컴포넌트를 슬롯에 넣고, id 전달 -->
      <AttractionDetail :id="modalSpotId" />
    </ModalWrapper>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import draggable from "vuedraggable";
import ModalWrapper from "@/components/attraction/ModelWrapper.vue";
import AttractionDetail from "@/components/attraction/AttractionDetail.vue";
import { useRoute, useRouter } from "vue-router";
import SockJS from "sockjs-client/dist/sockjs.min.js";
import { Client } from "@stomp/stompjs";
import { useUserStore } from "@/store/userStore";
import apiGroup from "@/api/index";

const userStore = useUserStore();
const userId = userStore.user.id;
const route = useRoute();
const router = useRouter();
const squadId = route.query.squadId;
let stompClient = null;

// 상태 관리
const mapContainer = ref(null);
const mapBound = ref(null);
const selectedPlaces = ref([]); // { id, title, latitude, longitude }
const isShared = ref(false);
const minDate = ref("");
const selectedDate = ref("");
const contentTypes = ref([]);
const selectedContentType = ref("");
const keywordText = ref("");
const spots = ref([]);
const currentPage = ref(1);
const totalPages = ref(0);
const savePolyline = ref([]);
const imageSize = ref(null);
const imageOption = ref(null);
const likedAttractions = ref({});
const likeCounts = ref({});
const squadMembers = ref([]);

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

const cursorImageMap = {
  0: "/img/redCursor.png",
  1: "/img/greenCursor.png",
  2: "/img/blueCursor.png",
  3: "/img/purpleCursor.png",
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

function removePlace(index) {
  selectedPlaces.value.splice(index, 1);
  stompClient.publish({
    destination: "/app/squad/selectedPlaces",
    body: JSON.stringify({
      squadId,
      messageType: "SelectedPlaces",
      userId,
      content: selectedPlaces.value,
    }),
  });
}

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
  await getSquadMembers();
  setMinDate();
  await loadContentList();
  await loadKakaoMapScript();
  socketConfig();

  publishInterval = setInterval(() => {
    if (lastLatLng && stompClient?.connected) {
      stompClient.publish({
        destination: "/app/squad/mouseMove",
        body: JSON.stringify({
          squadId,
          messageType: "Mousemove",
          userId,
          content: {
            latitude: lastLatLng.getLat(),
            longitude: lastLatLng.getLng(),
          },
          position,
        }),
      });
    }
  }, 100);
});

const getSquadMembers = async () => {
  const response = await apiGroup.api({
    url: "/api/user/squads/member",
    method: "GET",
    params: {
      squadId,
    },
  });
  squadMembers.value = response.data;
  const userObj = squadMembers.value.find((member) => member.userId === userId);
  position = userObj.position;
};

// 초기화 함수들
const setMinDate = () => {
  const today = new Date();
  const yyyy = today.getFullYear();
  const mm = String(today.getMonth() + 1).padStart(2, "0");
  const dd = String(today.getDate()).padStart(2, "0");
  minDate.value = `${yyyy}-${mm}-${dd}`;
};

const loadKakaoMapScript = async () => {
  if (window.kakao && window.kakao.maps) {
    initKakaoMap();
    return;
  }

  return new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=a1b7d43f74e8d7c4fa60d02ce2c13f58&autoload=false`; // autoload=false로 설정
    script.onload = () => {
      window.kakao.maps.load(() => {
        initKakaoMap();
        resolve();
      });
    };
    script.onerror = reject;
    document.head.appendChild(script);
  });
};

let lastLatLng = null;
let publishInterval = null;

const initKakaoMap = () => {
  const centerLatLng = { latitude: 35.205432, longitude: 126.811591 };
  const options = {
    center: new window.kakao.maps.LatLng(centerLatLng.latitude, centerLatLng.longitude),
    level: 6,
  };
  map = new window.kakao.maps.Map(mapContainer.value, options);
  imageSize.value = new window.kakao.maps.Size(30, 30);
  imageOption.value = { offset: new window.kakao.maps.Point(15, 30) };

  kakao.maps.event.addListener(map, "mousemove", function (mouseEvent) {
    lastLatLng = mouseEvent.latLng;
  });

  mapContainer.value.addEventListener("mouseleave", function () {
    lastLatLng = null;
    stompClient.publish({
      destination: "/app/squad/mouseMove",
      body: JSON.stringify({
        squadId,
        messageType: "Mousemove",
        userId,
        content: {
          latitude: null,
          longitude: null,
        },
        position,
      }),
    });
  });

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition((pos) => {
      const { latitude, longitude } = pos.coords;
      map.setCenter(new window.kakao.maps.LatLng(latitude, longitude));
    });
  }
};

const loadContentList = async () => {
  try {
    const response = await apiGroup.api({
      url: "/api/map/content-types",
      method: "GET",
    });
    contentTypes.value = response;
  } catch (err) {
    console.error("컨텐츠 목록 로딩 실패", err);
  }
};

const userCursors = new Map();
let position = null;

function updateUserCursor(userId, pos, lat, lng) {
  const latlng = new kakao.maps.LatLng(lat, lng);
  const bounds = map.getBounds();

  const imageSrc = cursorImageMap[pos];
  const markerImage = new kakao.maps.MarkerImage(imageSrc, new kakao.maps.Size(32, 32));

  if (bounds.contain(latlng)) {
    // 기존 마커 있으면 위치만 갱신
    if (userCursors.has(userId)) {
      const marker = userCursors.get(userId);
      marker.setPosition(latlng);
    } else {
      // 새 마커 생성
      const marker = new kakao.maps.Marker({
        map,
        position: latlng,
        image: markerImage,
      });
      userCursors.set(userId, marker);
    }
  } else {
    // 지도 밖이면 마커 제거
    if (userCursors.has(userId)) {
      userCursors.get(userId).setMap(null);
      userCursors.delete(userId);
    }
  }
}

const joinMessage = ref(null);
let joinTimeout = null;

function showJoinPopup(message) {
  joinMessage.value = null;
  setTimeout(() => {
    joinMessage.value = message;
  }, 0);
  if (joinTimeout) clearTimeout(joinTimeout);
  joinTimeout = setTimeout(() => {
    joinMessage.value = null;
  }, 3000);
}

const socketConfig = () => {
  const socket = new SockJS("http://localhost:8080/ws");
  stompClient = new Client({
    webSocketFactory: () => socket,
    debug: (str) => console.log(str),
    onConnect: () => {
      console.log("✅ 연결됨");

      stompClient.subscribe(`/topic/squad.${squadId}`, (msg) => {
        const body = JSON.parse(msg.body);
        if (body.messageType === "JOIN") {
          showJoinPopup(body.content);
        } else if (body.messageType === "SelectedPlaces") {
          selectedPlaces.value = body.content;
        } else if (body.messageType === "Mousemove") {
          const { latitude, longitude } = body.content;
          if (body.userId != userId) {
            updateUserCursor(body.userId, body.position, latitude, longitude);
          }
        } else if (body.messageType === "savePlan") {
          router.push("/plans");
        }
      });

      // 입장 메시지 보내기
      stompClient.publish({
        destination: "/app/squad/join",
        body: JSON.stringify({
          squadId,
          messageType: "JOIN",
          userId,
          content: `${userStore.user.username}님이 입장했습니다`,
        }),
      });
    },
  });

  stompClient.activate();
};

watch(
  selectedPlaces,
  (newVal, oldVal) => {
    if (JSON.stringify(newVal) !== JSON.stringify(oldVal)) {
      stompClient.publish({
        destination: "/app/squad/selectedPlaces",
        body: JSON.stringify({
          squadId,
          messageType: "SelectedPlaces",
          userId,
          content: selectedPlaces.value,
        }),
      });
    }
  },
  { deep: true }
);

onBeforeUnmount(() => {
  stompClient?.deactivate();
  clearInterval(publishInterval);
});

// 유틸리티 함수
const containsNonKorean = (text) => {
  // 한글 이외의 문자가 하나라도 있으면 true
  return /[^가-힣]/.test(text);
};

const searchAttractions = () => {
  fetchTourSpots(0);
};

const fetchTourSpots = async (pageIndex) => {
  let contentTypeValue = parseInt(selectedContentType.value) || -1;

  if (![12, 14, 15, 25, 28, 32, 38, 39].includes(contentTypeValue)) {
    contentTypeValue = -1;
  }

  let keyword = keywordText.value;
  if (keyword.length < 2 || containsNonKorean(keyword)) {
    keyword = "";
  }

  // 지도의 현재 bounds 업데이트
  if (map) {
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

  try {
    const response = await apiGroup.api({
      url: "/api/map/region-contents",
      method: "GET",
      params: {
        mapBound: JSON.stringify(mapBound.value),
        page: pageIndex,
        size: 10,
        contentType: contentTypeValue,
        keyword: keyword,
      },
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
  fetchTourSpots(page - 1);
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

const setMarkers = () => {
  // 기존 마커/선 제거
  savePolyline.value.forEach((o) => o.setMap(null));
  savePolyline.value = [];

  const bounds = new window.kakao.maps.LatLngBounds();

  selectedPlaces.value.forEach((item, index) => {
    const position = new window.kakao.maps.LatLng(item.latitude, item.longitude);

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
      ${index + 1}
    </div>`;

    const customOverlay = new window.kakao.maps.CustomOverlay({
      position,
      content,
      xAnchor: 0.5,
      yAnchor: 0.5,
    });

    customOverlay.setMap(map);
    savePolyline.value.push(customOverlay);
    bounds.extend(position);

    if (index < selectedPlaces.value.length - 1) {
      const next = selectedPlaces.value[index + 1];
      const linePath = [position, new window.kakao.maps.LatLng(next.latitude, next.longitude)];

      const polyline = new window.kakao.maps.Polyline({
        path: linePath,
        strokeWeight: 4,
        strokeColor: getColorByIndex(index, selectedPlaces.value.length),
        strokeOpacity: 0.8,
        strokeStyle: "solid",
        map: map,
      });

      savePolyline.value.push(polyline);
    }
  });

  if (selectedPlaces.value.length > 0) {
    map.setBounds(bounds);
  }
};

// 색상 구하는 함수
const getColorByIndex = (i, total) => `hsl(${(i / total) * 360}, 80%, 60%)`;

// selectedPlaces가 변경될 때 마커 다시 그림
watch(
  selectedPlaces,
  () => {
    if (mapContainer.value) {
      setMarkers();
    }
  },
  { deep: true }
);

const savePlan = async () => {
  if (selectedPlaces.value.length === 0) {
    alert("선택된 장소가 없습니다.");
    return;
  }

  try {
    squadMembers.value.forEach(async (member) => {
      if (member.position === squadMembers.value.length - 1) return;

      const planData = {
        userId: member.userId,
        travelDate: selectedDate.value,
        isShared: 0,
        attractionIds: selectedPlaces.value.map((place) => place.no),
      };

      await apiGroup.api({
        url: "/api/map/plans",
        method: "POST",
        data: planData,
      });
    });
    const planData = {
      userId,
      travelDate: selectedDate.value,
      isShared: isShared.value ? 1 : 0,
      attractionIds: selectedPlaces.value.map((place) => place.no),
    };

    const response = await apiGroup.api({
      url: "/api/map/plans",
      method: "POST",
      data: planData,
    });

    const result = await response.text();
    alert(result);

    stompClient.publish({
      destination: "/app/squad/savePlan",
      body: JSON.stringify({
        squadId,
        messageType: "savePlan",
      }),
    });

    // 저장 후 선택 목록 초기화
    selectedPlaces.value = [];

    // 경로선 제거
    if (savePolyline.value) {
      savePolyline.value.forEach((polyline) => polyline.setMap(null));
      savePolyline.value = [];
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
.join-popup {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(50, 50, 50, 0.9);
  color: white;
  padding: 12px 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  font-size: 16px;
  animation: fadeInOut 3s ease-in-out;
}

@keyframes fadeInOut {
  0% {
    opacity: 0;
    transform: translate(-50%, -10px);
  }
  10% {
    opacity: 1;
    transform: translate(-50%, 0);
  }
  90% {
    opacity: 1;
    transform: translate(-50%, 0);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -10px);
  }
}
</style>
