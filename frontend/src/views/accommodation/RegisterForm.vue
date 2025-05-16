<template>
  <Layout>
    <div class="container mt-5 mb-5">
      <h2 class="text-center mb-4">숙소 등록</h2>

      <div class="form-container">
        <div v-if="error" class="alert alert-danger">{{ error }}</div>

        <form @submit.prevent="submitForm">
          <div class="mb-3">
            <label for="title" class="form-label required-field">숙소 이름</label>
            <input type="text" class="form-control" id="title" v-model="accommodation.title" required />
          </div>

          <div class="mb-3">
            <label for="description" class="form-label required-field">숙소 설명</label>
            <textarea
              class="form-control"
              id="description"
              v-model="accommodation.description"
              rows="5"
              required
            ></textarea>
          </div>

          <div class="mb-3">
            <label for="address" class="form-label required-field">주소</label>
            <input
              type="text"
              class="form-control"
              id="address"
              v-model="accommodation.address"
              required
              @blur="searchAddress"
            />
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label for="sidoCode" class="form-label required-field">시/도</label>
              <select class="form-select" id="sidoCode" v-model="accommodation.sidoCode" required @change="loadGuguns">
                <option value="">시/도 선택</option>
                <option v-for="sido in sidos" :key="sido.sidoCode" :value="sido.sidoCode">{{ sido.sidoName }}</option>
              </select>
            </div>
            <div class="col-md-6">
              <label for="gugunCode" class="form-label required-field">구/군</label>
              <select class="form-select" id="gugunCode" v-model="accommodation.gugunCode" required>
                <option value="">구/군 선택</option>
                <option v-for="gugun in guguns" :key="gugun.gugunCode" :value="gugun.gugunCode">
                  {{ gugun.gugunName }}
                </option>
              </select>
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label for="latitude" class="form-label">위도</label>
              <input type="text" class="form-control" id="latitude" v-model="accommodation.latitude" />
              <div class="form-text">지도에서 위치를 선택하면 자동으로 입력됩니다.</div>
            </div>
            <div class="col-md-6">
              <label for="longitude" class="form-label">경도</label>
              <input type="text" class="form-control" id="longitude" v-model="accommodation.longitude" />
              <div class="form-text">지도에서 위치를 선택하면 자동으로 입력됩니다.</div>
            </div>
          </div>

          <div class="mb-3">
            <label for="map" class="form-label">지도에서 위치 선택</label>
            <div id="map" style="width: 100%; height: 400px"></div>
          </div>

          <div class="mb-3">
            <label for="accommodationType" class="form-label required-field">숙소 유형</label>
            <select class="form-select" id="accommodationType" v-model="accommodation.accommodationType" required>
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

          <div class="mb-3">
            <label for="phone" class="form-label">전화번호</label>
            <input
              type="tel"
              class="form-control"
              id="phone"
              v-model="accommodation.phone"
              placeholder="예: 02-1234-5678 또는 010-1234-5678"
            />
          </div>

          <div class="mb-3">
            <label for="email" class="form-label">이메일</label>
            <input type="email" class="form-control" id="email" v-model="accommodation.email" />
          </div>

          <div class="mb-3">
            <label for="website" class="form-label">웹사이트</label>
            <input
              type="url"
              class="form-control"
              id="website"
              v-model="accommodation.website"
              placeholder="예: https://www.example.com"
            />
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label for="checkInTime" class="form-label">체크인 시간</label>
              <input type="time" class="form-control" id="checkInTime" v-model="accommodation.checkInTime" />
            </div>
            <div class="col-md-6">
              <label for="checkOutTime" class="form-label">체크아웃 시간</label>
              <input type="time" class="form-control" id="checkOutTime" v-model="accommodation.checkOutTime" />
            </div>
          </div>

          <div class="mb-3">
            <label for="amenities" class="form-label">편의시설</label>
            <textarea
              class="form-control"
              id="amenities"
              v-model="accommodation.amenities"
              rows="3"
              placeholder="예: 와이파이, 주차장, 수영장, 조식 등"
            ></textarea>
          </div>

          <div class="mb-3">
            <label for="imageFiles" class="form-label required-field">숙소 이미지</label>
            <input
              type="file"
              class="form-control"
              id="imageFiles"
              @change="handleImageUpload"
              multiple
              accept="image/*"
              required
            />
            <div class="form-text">
              최소 1개 이상의 이미지를 등록해주세요. 첫 번째 이미지가 대표 이미지로 설정됩니다.
            </div>
            <div id="imagePreviewContainer" class="d-flex flex-wrap mt-2">
              <div v-for="(preview, index) in imagePreviews" :key="index" class="image-preview me-2 mb-2">
                <img :src="preview" alt="이미지 미리보기" />
              </div>
            </div>
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
            <router-link to="/accommodation/host/accommodations" class="btn btn-secondary">취소</router-link>
          </div>
        </form>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 숙소 등록 폼 컴포넌트
 *
 * 이 컴포넌트는 호스트가 새로운 숙소를 등록하기 위한 폼을 제공합니다.
 * 숙소 정보 입력, 위치 선택(카카오맵 API 사용), 이미지 업로드 기능을 포함합니다.
 */
import { mapState, mapActions } from "vuex";
import Layout from "@/components/layout/Layout.vue";

export default {
  name: "RegisterForm",
  components: {
    Layout,
  },
  data() {
    return {
      accommodation: {
        title: "",
        description: "",
        address: "",
        sidoCode: "",
        gugunCode: "",
        latitude: "",
        longitude: "",
        accommodationType: "",
        phone: "",
        email: "",
        website: "",
        checkInTime: "",
        checkOutTime: "",
        amenities: "",
      },
      sidos: [],
      guguns: [],
      imageFiles: [],
      imagePreviews: [],
      error: "",
      map: null,
      marker: null,
      isSubmitting: false,
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: (state) => state.user.isLoggedIn,
      isHost: (state) => state.user.user?.role === "HOST",
    }),
  },
  mounted() {
    // 로그인 및 호스트 권한 확인
    if (!this.isLoggedIn) {
      this.$router.push("/user/login");
      return;
    }

    if (!this.isHost) {
      this.$router.push("/");
      this.error = "호스트 권한이 필요합니다.";
      return;
    }

    // 기능 비활성화 안내
    this.error = "현재 숙소 등록 기능은 지원되지 않습니다. 관리자에게 문의해주세요.";

    this.loadSidos();
    this.initializeMap();
  },
  methods: {
    ...mapActions("accommodation", ["registerAccommodation"]),

    /**
     * 시도 목록 로드
     */
    async loadSidos() {
      try {
        const response = await fetch("/api/region/sidos");
        this.sidos = await response.json();
      } catch (error) {
        console.error("시도 목록을 불러오는 중 오류가 발생했습니다:", error);
        this.error = "시도 목록을 불러올 수 없습니다. 다시 시도해주세요.";
      }
    },

    /**
     * 구군 목록 로드
     */
    async loadGuguns() {
      if (!this.accommodation.sidoCode) {
        this.guguns = [];
        this.accommodation.gugunCode = "";
        return;
      }

      try {
        const response = await fetch(`/api/region/guguns?sido=${this.accommodation.sidoCode}`);
        this.guguns = await response.json();
      } catch (error) {
        console.error("구군 목록을 불러오는 중 오류가 발생했습니다:", error);
        this.error = "구군 목록을 불러올 수 없습니다. 다시 시도해주세요.";
      }
    },

    /**
     * 카카오맵 초기화
     */
    initializeMap() {
      if (window.kakao && window.kakao.maps) {
        this.loadMap();
      } else {
        const script = document.createElement("script");
        script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.VUE_APP_KAKAO_MAP_API_KEY}&libraries=services&autoload=false`;
        script.onload = () => {
          window.kakao.maps.load(() => {
            this.loadMap();
          });
        };
        document.head.appendChild(script);
      }
    },

    /**
     * 카카오맵 로드
     */
    loadMap() {
      const mapContainer = document.getElementById("map");
      const mapOption = {
        center: new window.kakao.maps.LatLng(37.566826, 126.9786567), // 서울 시청
        level: 3,
      };

      this.map = new window.kakao.maps.Map(mapContainer, mapOption);

      // 지도 클릭 이벤트
      window.kakao.maps.event.addListener(this.map, "click", (mouseEvent) => {
        const latlng = mouseEvent.latLng;

        // 마커 생성 또는 이동
        if (!this.marker) {
          this.marker = new window.kakao.maps.Marker({
            position: latlng,
            map: this.map,
          });
        } else {
          this.marker.setPosition(latlng);
        }

        // 위도, 경도 입력
        this.accommodation.latitude = latlng.getLat();
        this.accommodation.longitude = latlng.getLng();
      });
    },

    /**
     * 주소 검색
     */
    searchAddress() {
      if (!this.accommodation.address || !window.kakao || !window.kakao.maps) return;

      const geocoder = new window.kakao.maps.services.Geocoder();

      geocoder.addressSearch(this.accommodation.address, (result, status) => {
        if (status === window.kakao.maps.services.Status.OK) {
          const coords = new window.kakao.maps.LatLng(result[0].y, result[0].x);

          // 마커 생성 또는 이동
          if (!this.marker) {
            this.marker = new window.kakao.maps.Marker({
              position: coords,
              map: this.map,
            });
          } else {
            this.marker.setPosition(coords);
          }

          // 지도 중심 이동
          this.map.setCenter(coords);

          // 위도, 경도 입력
          this.accommodation.latitude = result[0].y;
          this.accommodation.longitude = result[0].x;
        }
      });
    },

    /**
     * 이미지 업로드 처리
     */
    handleImageUpload(event) {
      this.imageFiles = Array.from(event.target.files);
      this.imagePreviews = [];

      this.imageFiles.forEach((file) => {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.imagePreviews.push(e.target.result);
        };
        reader.readAsDataURL(file);
      });
    },

    /**
     * 폼 제출 처리
     */
    async submitForm() {
      this.error = "현재 숙소 등록 기능은 지원되지 않습니다. 관리자에게 문의해주세요.";
      this.isSubmitting = false; // 실제 제출 안함
      return; // 기능 중단

      /* 기존 로직 주석 처리
      this.isSubmitting = true;
      try {
        // 필수 필드 검증
        if (!this.accommodation.title || !this.accommodation.description || !this.accommodation.address ||
            !this.accommodation.sidoCode || !this.accommodation.gugunCode || !this.accommodation.accommodationType ||
            !this.imageFiles.length) {
          this.error = '필수 항목을 모두 입력해주세요.';
          return;
        }

        // FormData 생성
        const formData = new FormData();

        // 숙소 정보 추가
        Object.keys(this.accommodation).forEach(key => {
          if (this.accommodation[key]) {
            formData.append(key, this.accommodation[key]);
          }
        });

        // 이미지 파일 추가
        this.imageFiles.forEach(file => {
          formData.append('imageFiles', file);
        });

        // 숙소 등록 API 호출
        await this.registerAccommodation(formData);

        // 성공 시 호스트 숙소 목록 페이지로 이동
        this.$router.push({
          path: '/accommodation/host/accommodations',
          query: { message: '숙소가 성공적으로 등록되었습니다. 관리자 승인 후 게시됩니다.' }
        });
      } catch (error) {
        console.error('숙소 등록 중 오류가 발생했습니다:', error);
        this.error = '숙소 등록에 실패했습니다. 다시 시도해주세요.';
      }
      this.isSubmitting = false;
      */
    },
  },
};
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
  content: " *";
  color: red;
}

.image-preview {
  width: 150px;
  height: 150px;
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
