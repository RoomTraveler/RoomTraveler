<template>
  <Layout>
    <div class="container mt-5 mb-5">
      <h2 class="text-center mb-4">숙소 등록</h2>

      <div class="form-container">
        <div v-if="error" class="alert alert-danger">{{ error }}</div>

        <form @submit.prevent="submitForm">
          <!-- 숙소 이름 -->
          <div class="mb-3">
            <label for="title" class="form-label required-field">숙소 이름</label>
            <input
                id="title"
                type="text"
                class="form-control"
                v-model="accommodation.title"
                required
            />
          </div>

          <!-- 숙소 설명 -->
          <div class="mb-3">
            <label for="description" class="form-label required-field">숙소 설명</label>
            <textarea
                id="description"
                class="form-control"
                rows="5"
                v-model="accommodation.description"
                required
            ></textarea>
          </div>

          <!-- 주소 & 지도 검색 -->
          <div class="mb-3">
            <label for="address" class="form-label required-field">주소</label>
            <input
                id="address"
                type="text"
                class="form-control"
                v-model="accommodation.address"
                @blur="searchAddress"
                required
            />
          </div>

          <!-- 시/도, 구/군 -->
          <div class="row mb-3">
            <div class="col-md-6">
              <label for="sidoCode" class="form-label required-field">시/도</label>
              <select
                  id="sidoCode"
                  class="form-select"
                  v-model="accommodation.sidoCode"
                  @change="loadGuguns"
                  required
              >
                <option value="">시/도 선택</option>
                <option
                    v-for="sido in sidos"
                    :key="sido.sidoCode"
                    :value="sido.sidoCode"
                >
                  {{ sido.sidoName }}
                </option>
              </select>
            </div>
            <div class="col-md-6">
              <label for="gugunCode" class="form-label required-field">구/군</label>
              <select
                  id="gugunCode"
                  class="form-select"
                  v-model="accommodation.gugunCode"
                  required
              >
                <option value="">구/군 선택</option>
                <option
                    v-for="gugun in guguns"
                    :key="gugun.gugunCode"
                    :value="gugun.gugunCode"
                >
                  {{ gugun.gugunName }}
                </option>
              </select>
            </div>
          </div>

          <!-- 위도/경도 -->
          <div class="row mb-3">
            <div class="col-md-6">
              <label for="latitude" class="form-label">위도</label>
              <input
                  id="latitude"
                  type="text"
                  class="form-control"
                  v-model="accommodation.latitude"
              />
              <div class="form-text">
                지도에서 위치를 선택하면 자동으로 입력됩니다.
              </div>
            </div>
            <div class="col-md-6">
              <label for="longitude" class="form-label">경도</label>
              <input
                  id="longitude"
                  type="text"
                  class="form-control"
                  v-model="accommodation.longitude"
              />
              <div class="form-text">
                지도에서 위치를 선택하면 자동으로 입력됩니다.
              </div>
            </div>
          </div>

          <!-- 지도 -->
          <div class="mb-3">
            <label class="form-label">지도에서 위치 선택</label>
            <div id="map" style="width: 100%; height: 400px"></div>
          </div>

          <!-- 숙소 유형 -->
          <div class="mb-3">
            <label for="accommodationType" class="form-label required-field">
              숙소 유형
            </label>
            <select
                id="accommodationType"
                class="form-select"
                v-model="accommodation.accommodationType"
                required
            >
              <option value="">숙소 유형 선택</option>
              <option value="HOTEL">호텔</option>
              <option value="MOTEL">모텔</option>
              <option value="PENSION">펜션</option>
              <option value="GUEST_HOUSE">게스트하우스</option>
              <option value="RESORT">리조트</option>
              <option value="CONDO">콘도</option>
              <option value="HANOK">한옥</option>
              <option value="CAMPING">캠핑/글램핑</option>
              <option value="OTHER">기타</option>
            </select>
          </div>

          <!-- 연락처 -->
          <div class="row mb-3">
            <div class="col-md-6">
              <label for="phone" class="form-label">전화번호</label>
              <input
                  id="phone"
                  type="tel"
                  class="form-control"
                  v-model="accommodation.phone"
                  placeholder="예: 02-1234-5678 또는 010-1234-5678"
              />
            </div>
            <div class="col-md-6">
              <label for="email" class="form-label">이메일</label>
              <input
                  id="email"
                  type="email"
                  class="form-control"
                  v-model="accommodation.email"
              />
            </div>
          </div>

          <!-- 웹사이트 -->
          <div class="mb-3">
            <label for="website" class="form-label">웹사이트</label>
            <input
                id="website"
                type="url"
                class="form-control"
                v-model="accommodation.website"
                placeholder="예: https://www.example.com"
            />
          </div>

          <!-- 체크인/체크아웃 -->
          <div class="row mb-3">
            <div class="col-md-6">
              <label for="checkInTime" class="form-label">체크인 시간</label>
              <input
                  id="checkInTime"
                  type="time"
                  class="form-control"
                  v-model="accommodation.checkInTime"
              />
            </div>
            <div class="col-md-6">
              <label for="checkOutTime" class="form-label">체크아웃 시간</label>
              <input
                  id="checkOutTime"
                  type="time"
                  class="form-control"
                  v-model="accommodation.checkOutTime"
              />
            </div>
          </div>

          <!-- 편의시설 -->
          <div class="mb-3">
            <label for="amenities" class="form-label">편의시설</label>
            <textarea
                id="amenities"
                class="form-control"
                rows="3"
                v-model="accommodation.amenities"
                placeholder="예: 와이파이, 주차장, 수영장, 조식 등"
            ></textarea>
          </div>

          <!-- 이미지 업로드 -->
          <div class="mb-3">
            <label for="imageFiles" class="form-label required-field">
              숙소 이미지
            </label>
            <input
                id="imageFiles"
                type="file"
                class="form-control"
                multiple
                accept="image/*"
                @change="handleImageUpload"
                required
            />
            <div class="form-text">
              최소 1개 이상의 이미지를 등록해주세요. 첫 번째 이미지가 대표
              이미지로 설정됩니다.
            </div>
            <div class="d-flex flex-wrap mt-2" id="imagePreviewContainer">
              <div
                  v-for="(src, idx) in imagePreviews"
                  :key="idx"
                  class="image-preview me-2 mb-2"
              >
                <img :src="src" alt="미리보기" />
              </div>
            </div>
          </div>

          <!-- 안내 메시지 -->
          <div class="alert alert-info">
            <p><strong>숙소 등록 안내:</strong></p>
            <ul>
              <li>숙소 등록 후 관리자 승인이 필요합니다.</li>
              <li>정확한 정보를 입력해주세요.</li>
              <li>숙소 등록 후 객실을 추가로 등록할 수 있습니다.</li>
            </ul>
          </div>

          <!-- 버튼 -->
          <div class="d-grid gap-2">
            <button type="submit" class="btn btn-primary">
              숙소 등록
            </button>
            <router-link
                to="/accommodation/host/accommodations"
                class="btn btn-secondary"
            >
              취소
            </router-link>
          </div>
        </form>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import Layout from '@/components/layout/Layout.vue';

const store = useStore();
const router = useRouter();

// reactive state
const accommodation = reactive({
  title: '',
  description: '',
  address: '',
  sidoCode: '',
  gugunCode: '',
  latitude: '',
  longitude: '',
  accommodationType: '',
  phone: '',
  email: '',
  website: '',
  checkInTime: '',
  checkOutTime: '',
  amenities: '',
});
const sidos = ref([]);
const guguns = ref([]);
const imageFiles = ref([]);
const imagePreviews = ref([]);
const error = ref('');
const map = ref(null);
const marker = ref(null);
const isSubmitting = ref(false);

// computed from Vuex
const isLoggedIn = computed(() => store.state.user.isLoggedIn);
const isHost = computed(() => store.state.user.user?.role === 'HOST');

// lifecycle
onMounted(() => {
  if (!isLoggedIn.value) {
    router.push('/user/login');
    return;
  }
  if (!isHost.value) {
    error.value = '호스트 권한이 필요합니다.';
    router.push('/');
    return;
  }

  // 현재 기능 미지원 안내
  error.value = '현재 숙소 등록 기능은 지원되지 않습니다. 관리자에게 문의해주세요.';

  loadSidos();
  initMap();
});

// methods
async function loadSidos() {
  try {
    const res = await fetch('/api/region/sidos');
    sidos.value = await res.json();
  } catch (e) {
    console.error(e);
    error.value = '시도 목록을 불러올 수 없습니다.';
  }
}

async function loadGuguns() {
  if (!accommodation.sidoCode) {
    guguns.value = [];
    accommodation.gugunCode = '';
    return;
  }
  try {
    const res = await fetch(`/api/region/guguns?sido=${accommodation.sidoCode}`);
    guguns.value = await res.json();
  } catch (e) {
    console.error(e);
    error.value = '구군 목록을 불러올 수 없습니다.';
  }
}

function initMap() {
  if (window.kakao?.maps) {
    createMap();
  } else {
    const script = document.createElement('script');
    script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.VUE_APP_KAKAO_MAP_API_KEY}&libraries=services&autoload=false`;
    script.onload = () => window.kakao.maps.load(createMap);
    document.head.appendChild(script);
  }
}

function createMap() {
  const container = document.getElementById('map');
  map.value = new window.kakao.maps.Map(container, {
    center: new window.kakao.maps.LatLng(37.566826, 126.9786567),
    level: 3,
  });
  window.kakao.maps.event.addListener(map.value, 'click', event => {
    const pos = event.latLng;
    if (!marker.value) {
      marker.value = new window.kakao.maps.Marker({ position: pos, map: map.value });
    } else {
      marker.value.setPosition(pos);
    }
    accommodation.latitude = pos.getLat();
    accommodation.longitude = pos.getLng();
  });
}

function searchAddress() {
  if (!accommodation.address || !window.kakao?.maps) return;
  const geocoder = new window.kakao.maps.services.Geocoder();
  geocoder.addressSearch(accommodation.address, (res, status) => {
    if (status === window.kakao.maps.services.Status.OK) {
      const coords = new window.kakao.maps.LatLng(res[0].y, res[0].x);
      if (!marker.value) {
        marker.value = new window.kakao.maps.Marker({ position: coords, map: map.value });
      } else {
        marker.value.setPosition(coords);
      }
      map.value.setCenter(coords);
      accommodation.latitude = res[0].y;
      accommodation.longitude = res[0].x;
    }
  });
}

function handleImageUpload(e) {
  imageFiles.value = Array.from(e.target.files);
  imagePreviews.value = [];
  imageFiles.value.forEach(file => {
    const reader = new FileReader();
    reader.onload = ev => imagePreviews.value.push(ev.target.result);
    reader.readAsDataURL(file);
  });
}

async function submitForm() {
  // 기능 중단
  error.value = '현재 숙소 등록 기능은 지원되지 않습니다. 관리자에게 문의해주세요.';
  isSubmitting.value = false;
  return;
}
</script>

<style scoped>
.form-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.required-field::after {
  content: ' *';
  color: red;
}

.image-preview {
  width: 150px;
  height: 150px;
  border: 1px solid #ddd;
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

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.badge.bg-danger {
  animation: pulse 2s infinite;
}
</style>
