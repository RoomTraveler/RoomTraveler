<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">한국관광공사 API 설정</h2>
      
      <!-- 알림 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>
      
      <!-- 에러 메시지 -->
      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
      </div>
      
      <!-- 로딩 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">API 설정을 불러오는 중입니다...</p>
      </div>
      
      <div v-else class="row">
        <!-- API 설정 폼 -->
        <div class="col-md-6">
          <div class="card">
            <div class="card-header">
              <h5 class="mb-0">API 설정 관리</h5>
            </div>
            <div class="card-body">
              <form @submit.prevent="saveApiConfig">
                <div class="mb-3">
                  <label for="apiKey" class="form-label">API 키</label>
                  <div class="input-group">
                    <input 
                      :type="showApiKey ? 'text' : 'password'" 
                      class="form-control" 
                      id="apiKey" 
                      v-model="apiConfig.apiKey" 
                      placeholder="한국관광공사 API 키를 입력하세요"
                      required
                    >
                    <button 
                      class="btn btn-outline-secondary" 
                      type="button" 
                      @click="showApiKey = !showApiKey"
                    >
                      <i :class="showApiKey ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                    </button>
                  </div>
                  <div class="form-text">한국관광공사 API 키는 <a href="https://www.data.go.kr/tcs/dss/selectApiDataDetailView.do?publicDataPk=15101578" target="_blank">공공데이터 포털</a>에서 발급받을 수 있습니다.</div>
                </div>
                
                <div class="mb-3">
                  <label for="baseUrl" class="form-label">기본 URL</label>
                  <input 
                    type="url" 
                    class="form-control" 
                    id="baseUrl" 
                    v-model="apiConfig.baseUrl" 
                    placeholder="API 기본 URL을 입력하세요"
                    required
                  >
                </div>
                
                <div class="mb-3">
                  <label for="mobileOs" class="form-label">모바일 OS</label>
                  <select class="form-select" id="mobileOs" v-model="apiConfig.mobileOs">
                    <option value="ETC">ETC</option>
                    <option value="IOS">iOS</option>
                    <option value="AND">Android</option>
                  </select>
                </div>
                
                <div class="mb-3">
                  <label for="mobileApp" class="form-label">모바일 앱 이름</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    id="mobileApp" 
                    v-model="apiConfig.mobileApp" 
                    placeholder="앱 이름을 입력하세요"
                    required
                  >
                </div>
                
                <div class="mb-3">
                  <label for="defaultNumOfRows" class="form-label">기본 행 수</label>
                  <input 
                    type="number" 
                    class="form-control" 
                    id="defaultNumOfRows" 
                    v-model="apiConfig.defaultNumOfRows" 
                    min="1" 
                    max="100"
                    required
                  >
                  <div class="form-text">한 번에 가져올 데이터 행 수 (1-100)</div>
                </div>
                
                <div class="mb-3">
                  <label for="defaultPageNo" class="form-label">기본 페이지 번호</label>
                  <input 
                    type="number" 
                    class="form-control" 
                    id="defaultPageNo" 
                    v-model="apiConfig.defaultPageNo" 
                    min="1"
                    required
                  >
                </div>
                
                <div class="mb-3">
                  <label for="defaultArrange" class="form-label">기본 정렬</label>
                  <select class="form-select" id="defaultArrange" v-model="apiConfig.defaultArrange">
                    <option value="A">제목 오름차순</option>
                    <option value="C">생성일 내림차순</option>
                    <option value="D">수정일 내림차순</option>
                    <option value="O">조회수 내림차순</option>
                    <option value="P">인기순</option>
                    <option value="Q">좌표순</option>
                    <option value="R">무작위</option>
                  </select>
                </div>
                
                <div class="mb-3">
                  <label for="syncInterval" class="form-label">동기화 주기 (시간)</label>
                  <input 
                    type="number" 
                    class="form-control" 
                    id="syncInterval" 
                    v-model="apiConfig.syncInterval" 
                    min="1"
                    required
                  >
                  <div class="form-text">API에서 데이터를 가져오는 주기 (시간 단위)</div>
                </div>
                
                <div class="mb-3 form-check">
                  <input 
                    type="checkbox" 
                    class="form-check-input" 
                    id="autoSync" 
                    v-model="apiConfig.autoSync"
                  >
                  <label class="form-check-label" for="autoSync">자동 동기화 활성화</label>
                </div>
                
                <div class="d-grid gap-2">
                  <button type="submit" class="btn btn-primary" :disabled="saving">
                    <span v-if="saving" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    설정 저장
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
        
        <!-- API 테스트 및 동기화 -->
        <div class="col-md-6">
          <div class="card mb-4">
            <div class="card-header">
              <h5 class="mb-0">API 테스트</h5>
            </div>
            <div class="card-body">
              <form @submit.prevent="testApiConnection">
                <div class="mb-3">
                  <label for="testEndpoint" class="form-label">테스트 엔드포인트</label>
                  <select class="form-select" id="testEndpoint" v-model="testEndpoint">
                    <option value="areaCode">지역 코드</option>
                    <option value="areaBasedList">지역 기반 관광정보</option>
                    <option value="detailCommon">공통 상세 정보</option>
                    <option value="detailIntro">소개 정보</option>
                    <option value="detailInfo">반복 정보</option>
                    <option value="detailImage">이미지 정보</option>
                  </select>
                </div>
                
                <div class="d-grid">
                  <button type="submit" class="btn btn-info" :disabled="testing">
                    <span v-if="testing" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    API 연결 테스트
                  </button>
                </div>
              </form>
              
              <!-- 테스트 결과 -->
              <div v-if="testResult" class="mt-3">
                <h6>테스트 결과:</h6>
                <div class="alert" :class="testSuccess ? 'alert-success' : 'alert-danger'">
                  {{ testResult }}
                </div>
              </div>
            </div>
          </div>
          
          <div class="card">
            <div class="card-header">
              <h5 class="mb-0">데이터 동기화</h5>
            </div>
            <div class="card-body">
              <div class="mb-3">
                <label for="syncType" class="form-label">동기화 유형</label>
                <select class="form-select" id="syncType" v-model="syncType">
                  <option value="all">전체 데이터</option>
                  <option value="area">지역 코드</option>
                  <option value="accommodation">숙박 정보</option>
                  <option value="restaurant">음식점 정보</option>
                  <option value="attraction">관광지 정보</option>
                </select>
              </div>
              
              <div class="d-grid">
                <button @click="syncData" class="btn btn-success" :disabled="syncing">
                  <span v-if="syncing" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  데이터 동기화 시작
                </button>
              </div>
              
              <!-- 동기화 상태 -->
              <div v-if="syncStatus" class="mt-3">
                <h6>동기화 상태:</h6>
                <div class="alert" :class="syncSuccess ? 'alert-success' : 'alert-warning'">
                  {{ syncStatus }}
                </div>
                
                <div v-if="syncing && syncProgress > 0" class="progress mt-2">
                  <div 
                    class="progress-bar progress-bar-striped progress-bar-animated" 
                    role="progressbar" 
                    :style="{ width: syncProgress + '%' }" 
                    :aria-valuenow="syncProgress" 
                    aria-valuemin="0" 
                    aria-valuemax="100"
                  >
                    {{ syncProgress }}%
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 마지막 동기화 정보 -->
          <div class="card mt-4">
            <div class="card-header">
              <h5 class="mb-0">동기화 이력</h5>
            </div>
            <div class="card-body">
              <table class="table table-sm">
                <thead>
                  <tr>
                    <th>유형</th>
                    <th>마지막 동기화</th>
                    <th>상태</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(history, type) in syncHistory" :key="type">
                    <td>{{ getSyncTypeName(type) }}</td>
                    <td>{{ formatDateTime(history.lastSync) }}</td>
                    <td>
                      <span :class="getSyncStatusBadgeClass(history.status)">
                        {{ getSyncStatusName(history.status) }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 한국관광공사 API 설정 컴포넌트
 * 
 * 이 컴포넌트는 관리자가 한국관광공사 API 설정을 관리하고 테스트할 수 있는 페이지입니다.
 * API 키 설정, 연결 테스트, 데이터 동기화 기능을 제공합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'ApiConfig',
  components: {
    Layout
  },
  data() {
    return {
      loading: true,
      saving: false,
      testing: false,
      syncing: false,
      message: '',
      error: '',
      showApiKey: false,
      apiConfig: {
        apiKey: '',
        baseUrl: 'https://apis.data.go.kr/B551011/KorService1',
        mobileOs: 'ETC',
        mobileApp: 'RoomTraveler',
        defaultNumOfRows: 10,
        defaultPageNo: 1,
        defaultArrange: 'C',
        syncInterval: 24,
        autoSync: true
      },
      testEndpoint: 'areaCode',
      testResult: '',
      testSuccess: false,
      syncType: 'all',
      syncStatus: '',
      syncSuccess: false,
      syncProgress: 0,
      syncHistory: {
        all: { lastSync: null, status: 'NONE' },
        area: { lastSync: null, status: 'NONE' },
        accommodation: { lastSync: null, status: 'NONE' },
        restaurant: { lastSync: null, status: 'NONE' },
        attraction: { lastSync: null, status: 'NONE' }
      }
    };
  },
  computed: {
    ...mapState({
      isAdmin: state => state.user.user?.role === 'ADMIN'
    })
  },
  created() {
    // 관리자 권한 확인
    if (!this.isAdmin) {
      this.$router.push({
        path: '/error/access-denied',
        query: { message: '관리자만 접근할 수 있는 페이지입니다.' }
      });
      return;
    }
    
    // API 설정 로드
    this.loadApiConfig();
    
    // 동기화 이력 로드
    this.loadSyncHistory();
  },
  methods: {
    ...mapActions('admin', [
      'fetchApiConfig', 
      'saveApiConfig', 
      'testApiConnection', 
      'syncTourApiData',
      'fetchSyncHistory'
    ]),
    
    /**
     * API 설정 로드
     */
    async loadApiConfig() {
      this.loading = true;
      
      try {
        const config = await this.fetchApiConfig();
        this.apiConfig = { ...this.apiConfig, ...config };
      } catch (error) {
        console.error('API 설정을 불러오는 중 오류가 발생했습니다:', error);
        this.error = 'API 설정을 불러오는 중 오류가 발생했습니다.';
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * API 설정 저장
     */
    async saveApiConfig() {
      this.saving = true;
      this.message = '';
      this.error = '';
      
      try {
        await this.saveApiConfig(this.apiConfig);
        this.message = 'API 설정이 성공적으로 저장되었습니다.';
      } catch (error) {
        console.error('API 설정 저장 중 오류가 발생했습니다:', error);
        this.error = 'API 설정 저장에 실패했습니다. 다시 시도해주세요.';
      } finally {
        this.saving = false;
      }
    },
    
    /**
     * API 연결 테스트
     */
    async testApiConnection() {
      this.testing = true;
      this.testResult = '';
      this.testSuccess = false;
      
      try {
        const result = await this.testApiConnection({
          endpoint: this.testEndpoint,
          apiKey: this.apiConfig.apiKey,
          baseUrl: this.apiConfig.baseUrl
        });
        
        this.testResult = result.message;
        this.testSuccess = result.success;
      } catch (error) {
        console.error('API 연결 테스트 중 오류가 발생했습니다:', error);
        this.testResult = '테스트 중 오류가 발생했습니다: ' + (error.message || '알 수 없는 오류');
        this.testSuccess = false;
      } finally {
        this.testing = false;
      }
    },
    
    /**
     * 데이터 동기화
     */
    async syncData() {
      this.syncing = true;
      this.syncStatus = '동기화 시작 중...';
      this.syncSuccess = true;
      this.syncProgress = 0;
      
      try {
        // 동기화 시작
        const syncId = await this.syncTourApiData({
          type: this.syncType,
          apiKey: this.apiConfig.apiKey,
          baseUrl: this.apiConfig.baseUrl
        });
        
        // 동기화 상태 폴링
        this.pollSyncStatus(syncId);
      } catch (error) {
        console.error('데이터 동기화 시작 중 오류가 발생했습니다:', error);
        this.syncStatus = '동기화 시작에 실패했습니다: ' + (error.message || '알 수 없는 오류');
        this.syncSuccess = false;
        this.syncing = false;
      }
    },
    
    /**
     * 동기화 상태 폴링
     * @param {string} syncId - 동기화 ID
     */
    async pollSyncStatus(syncId) {
      const checkStatus = async () => {
        try {
          const status = await this.fetchSyncStatus(syncId);
          
          this.syncProgress = status.progress;
          this.syncStatus = status.message;
          
          if (status.status === 'COMPLETED') {
            this.syncSuccess = true;
            this.syncing = false;
            // 동기화 이력 다시 로드
            this.loadSyncHistory();
          } else if (status.status === 'FAILED') {
            this.syncSuccess = false;
            this.syncing = false;
          } else {
            // 계속 폴링
            setTimeout(checkStatus, 2000);
          }
        } catch (error) {
          console.error('동기화 상태 확인 중 오류가 발생했습니다:', error);
          this.syncStatus = '동기화 상태 확인에 실패했습니다.';
          this.syncSuccess = false;
          this.syncing = false;
        }
      };
      
      // 첫 번째 상태 확인
      setTimeout(checkStatus, 1000);
    },
    
    /**
     * 동기화 이력 로드
     */
    async loadSyncHistory() {
      try {
        const history = await this.fetchSyncHistory();
        this.syncHistory = { ...this.syncHistory, ...history };
      } catch (error) {
        console.error('동기화 이력을 불러오는 중 오류가 발생했습니다:', error);
      }
    },
    
    /**
     * 동기화 상태 확인
     * @param {string} syncId - 동기화 ID
     * @returns {Object} 동기화 상태 정보
     */
    async fetchSyncStatus(syncId) {
      // 실제 구현에서는 API 호출
      // return await this.$axios.get(`/api/admin/tour-api/sync/${syncId}/status`);
      
      // 임시 구현 (실제로는 API에서 상태를 가져와야 함)
      return new Promise(resolve => {
        const progress = this.syncProgress + Math.floor(Math.random() * 20);
        const finalProgress = Math.min(progress, 100);
        
        let status = 'IN_PROGRESS';
        let message = `${this.getSyncTypeName(this.syncType)} 데이터 동기화 중... (${finalProgress}%)`;
        
        if (finalProgress >= 100) {
          status = 'COMPLETED';
          message = `${this.getSyncTypeName(this.syncType)} 데이터 동기화가 완료되었습니다.`;
        }
        
        resolve({
          status,
          progress: finalProgress,
          message
        });
      });
    },
    
    /**
     * 동기화 유형 이름 반환
     * @param {string} type - 동기화 유형
     * @returns {string} 동기화 유형 이름
     */
    getSyncTypeName(type) {
      const types = {
        'all': '전체 데이터',
        'area': '지역 코드',
        'accommodation': '숙박 정보',
        'restaurant': '음식점 정보',
        'attraction': '관광지 정보'
      };
      
      return types[type] || type;
    },
    
    /**
     * 동기화 상태 이름 반환
     * @param {string} status - 동기화 상태
     * @returns {string} 동기화 상태 이름
     */
    getSyncStatusName(status) {
      const statuses = {
        'NONE': '없음',
        'IN_PROGRESS': '진행 중',
        'COMPLETED': '완료됨',
        'FAILED': '실패'
      };
      
      return statuses[status] || status;
    },
    
    /**
     * 동기화 상태 배지 클래스 반환
     * @param {string} status - 동기화 상태
     * @returns {string} 배지 클래스
     */
    getSyncStatusBadgeClass(status) {
      const classes = {
        'NONE': 'badge bg-secondary',
        'IN_PROGRESS': 'badge bg-warning',
        'COMPLETED': 'badge bg-success',
        'FAILED': 'badge bg-danger'
      };
      
      return classes[status] || 'badge bg-secondary';
    },
    
    /**
     * 날짜 및 시간 포맷팅
     * @param {string|Date} dateTime - 포맷팅할 날짜 및 시간
     * @returns {string} 포맷팅된 날짜 및 시간 문자열
     */
    formatDateTime(dateTime) {
      if (!dateTime) return '없음';
      
      const d = new Date(dateTime);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      const hours = String(d.getHours()).padStart(2, '0');
      const minutes = String(d.getMinutes()).padStart(2, '0');
      
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    }
  }
};
</script>

<style scoped>
.card {
  margin-bottom: 20px;
}

.card-header {
  background-color: #f8f9fa;
}

.progress {
  height: 20px;
}

.table th {
  background-color: #f8f9fa;
}
</style>