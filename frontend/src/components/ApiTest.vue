<template>
  <div class="api-test">
    <h2>API 연동 테스트</h2>

    <div class="test-section">
      <h3>지역 정보 테스트</h3>
      <button @click="loadSidos" class="btn btn-primary">시도 목록 불러오기</button>

      <div v-if="sidos.length > 0" class="mt-3">
        <h4>시도 목록</h4>
        <select v-model="selectedSido" class="form-select" @change="loadGuguns">
          <option value="">시/도 선택</option>
          <option v-for="sido in sidos" :key="sido.code" :value="sido.code">
            {{ sido.name }}
          </option>
        </select>
      </div>

      <div v-if="guguns.length > 0" class="mt-3">
        <h4>구군 목록</h4>
        <select v-model="selectedGugun" class="form-select">
          <option value="">구/군 선택</option>
          <option v-for="gugun in guguns" :key="gugun.code" :value="gugun.code">
            {{ gugun.name }}
          </option>
        </select>
      </div>
    </div>

    <div class="test-section mt-4">
      <h3>숙박 정보 테스트</h3>
      <button @click="loadAccommodations" class="btn btn-primary" :disabled="!selectedSido">
        숙박 시설 불러오기
      </button>

      <div v-if="accommodations.length > 0" class="mt-3">
        <h4>숙박 시설 목록 ({{ totalCount }}개 중 {{ accommodations.length }}개 표시)</h4>
        <div class="row">
          <div v-for="(item, index) in accommodations" :key="index" class="col-md-4 mb-3">
            <div class="card">
              <img :src="item.firstimage || 'https://via.placeholder.com/150'" class="card-img-top" alt="숙소 이미지">
              <div class="card-body">
                <h5 class="card-title">{{ item.title }}</h5>
                <p class="card-text">{{ item.addr1 }}</p>
                <button @click="loadRoomInfo(item.contentid)" class="btn btn-sm btn-info">
                  상세 정보
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="error" class="alert alert-danger mt-3">
      {{ error }}
      <div v-if="error.includes('네트워크 오류') || error.includes('요청 시간 초과')" class="mt-2">
        <p>가능한 해결 방법:</p>
        <ol>
          <li>백엔드 서버가 실행 중인지 확인하세요.</li>
          <li>백엔드 서버가 포트 8080에서 실행 중인지 확인하세요.</li>
          <li>방화벽이 연결을 차단하고 있지 않은지 확인하세요.</li>
        </ol>
        <button @click="retryLastOperation" class="btn btn-sm btn-primary mt-2">다시 시도</button>
      </div>
    </div>

    <div v-if="loading" class="text-center mt-3">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>

    <div v-if="roomInfo" class="mt-4">
      <h3>객실 정보</h3>
      <div class="card">
        <div class="card-body">
          <h4>{{ roomInfo && roomInfo.basicInfo && roomInfo.basicInfo.title }}</h4>
          <p>{{ roomInfo && roomInfo.basicInfo && roomInfo.basicInfo.addr1 }}</p>
          <p v-if="roomInfo && roomInfo.basicInfo && roomInfo.basicInfo.overview" v-html="roomInfo.basicInfo.overview"></p>

          <h5 class="mt-3">객실 목록</h5>
          <div v-if="roomInfo && roomInfo.roomInfo && roomInfo.roomInfo.length > 0">
            <div v-for="(room, index) in roomInfo.roomInfo" :key="index" class="mb-3 p-3 border">
              <h6>{{ room.roomtitle || '객실 ' + (index + 1) }}</h6>
              <p v-if="room && room.roomintro" v-html="room.roomintro"></p>
              <p v-if="room && room.roomsize1">크기: {{ room.roomsize1 }}</p>
              <p v-if="room && room.roomcount">객실 수: {{ room.roomcount }}</p>
              <p v-if="room && room.roombasecount">기준 인원: {{ room.roombasecount }}명</p>
              <p v-if="room && room.roommaxcount">최대 인원: {{ room.roommaxcount }}명</p>
            </div>
          </div>
          <div v-else>
            <p>객실 정보가 없습니다.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import regionApi from '@/api/regionApi';
import accommodationApi from '@/api/accommodationApi';

export default {
  name: 'ApiTest',
  data() {
    return {
      sidos: [],
      guguns: [],
      selectedSido: '',
      selectedGugun: '',
      accommodations: [],
      totalCount: 0,
      roomInfo: null,
      loading: false,
      error: null,
      lastOperation: null,
      lastOperationParams: null
    };
  },
  methods: {
    // 마지막 작업 재시도
    async retryLastOperation() {
      if (!this.lastOperation) return;

      // 마지막으로 시도한 작업 재실행
      switch (this.lastOperation) {
        case 'loadSidos':
          await this.loadSidos();
          break;
        case 'loadGuguns':
          if (this.lastOperationParams) {
            this.selectedSido = this.lastOperationParams;
          }
          await this.loadGuguns();
          break;
        case 'loadAccommodations':
          await this.loadAccommodations();
          break;
        case 'loadRoomInfo':
          if (this.lastOperationParams) {
            await this.loadRoomInfo(this.lastOperationParams);
          }
          break;
      }
    },

    async loadSidos() {
      this.loading = true;
      this.error = null;
      // 현재 작업 저장
      this.lastOperation = 'loadSidos';
      this.lastOperationParams = null;

      try {
        const response = await regionApi.getSidos();
        // 응답 데이터 유효성 검사
        if (response && response.data) {
          this.sidos = Array.isArray(response.data) ? response.data : [];
          // Proxy 객체가 아닌 일반 객체로 변환하여 콘솔에 출력
          console.log('시도 목록:', JSON.parse(JSON.stringify(this.sidos)));
        } else {
          throw new Error('응답 데이터가 유효하지 않습니다.');
        }
      } catch (error) {
        this.error = `시도 목록을 불러오는 중 오류가 발생했습니다: ${error.message}`;
        console.error('시도 목록 로딩 오류:', error);
      } finally {
        this.loading = false;
      }
    },

    async loadGuguns() {
      if (!this.selectedSido) {
        this.guguns = [];
        return;
      }

      this.loading = true;
      this.error = null;
      // 현재 작업 저장
      this.lastOperation = 'loadGuguns';
      this.lastOperationParams = this.selectedSido;

      try {
        const response = await regionApi.getGuguns(this.selectedSido);
        // 응답 데이터 유효성 검사
        if (response && response.data) {
          this.guguns = Array.isArray(response.data) ? response.data : [];
          // Proxy 객체가 아닌 일반 객체로 변환하여 콘솔에 출력
          console.log('구군 목록:', JSON.parse(JSON.stringify(this.guguns)));
        } else {
          throw new Error('응답 데이터가 유효하지 않습니다.');
        }
      } catch (error) {
        this.error = `구군 목록을 불러오는 중 오류가 발생했습니다: ${error.message}`;
        console.error('구군 목록 로딩 오류:', error);
      } finally {
        this.loading = false;
      }
    },

    async loadAccommodations() {
      if (!this.selectedSido) {
        return;
      }

      this.loading = true;
      this.error = null;
      this.accommodations = [];

      // 현재 작업 저장
      this.lastOperation = 'loadAccommodations';
      this.lastOperationParams = null;

      const params = {
        areaCode: this.selectedSido,
        numOfRows: 10
      };

      if (this.selectedGugun) {
        params.sigunguCode = this.selectedGugun;
      }

      try {
        const response = await accommodationApi.getAccommodations(params);
        // 응답 데이터 유효성 검사
        if (response && response.data) {
          this.accommodations = response.data.items || [];
          this.totalCount = response.data.totalCount || 0;
          // Proxy 객체가 아닌 일반 객체로 변환하여 콘솔에 출력
          console.log('숙박 시설 목록:', JSON.parse(JSON.stringify(this.accommodations)));
        } else {
          throw new Error('응답 데이터가 유효하지 않습니다.');
        }
      } catch (error) {
        this.error = `숙박 시설 목록을 불러오는 중 오류가 발생했습니다: ${error.message}`;
        console.error('숙박 시설 로딩 오류:', error);
      } finally {
        this.loading = false;
      }
    },

    async loadRoomInfo(contentId) {
      this.loading = true;
      this.error = null;
      this.roomInfo = null;

      // 현재 작업 저장
      this.lastOperation = 'loadRoomInfo';
      this.lastOperationParams = contentId;

      try {
        const response = await accommodationApi.getRoomInfo(contentId);
        // 응답 데이터 유효성 검사
        if (response && response.data) {
          this.roomInfo = response.data;

          // roomInfo 객체의 필수 속성 확인 및 초기화
          if (!this.roomInfo.basicInfo) {
            this.roomInfo.basicInfo = {};
          }

          if (!this.roomInfo.roomInfo) {
            this.roomInfo.roomInfo = [];
          }

          // Proxy 객체가 아닌 일반 객체로 변환하여 콘솔에 출력
          console.log('객실 정보:', JSON.parse(JSON.stringify(this.roomInfo)));
        } else {
          throw new Error('응답 데이터가 유효하지 않습니다.');
        }
      } catch (error) {
        this.error = `객실 정보를 불러오는 중 오류가 발생했습니다: ${error.message}`;
        console.error('객실 정보 로딩 오류:', error);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.api-test {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.test-section {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.card-img-top {
  height: 150px;
  object-fit: cover;
}
</style>
