<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">지역 데이터 관리</h2>

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
        <p class="mt-2">지역 데이터를 불러오는 중입니다...</p>
      </div>

      <div v-else class="row">
        <!-- 시도 관리 -->
        <div class="col-md-6">
          <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
              <h5 class="mb-0">시/도 관리</h5>
              <button class="btn btn-sm btn-primary" @click="showAddSidoModal">
                <i class="bi bi-plus-circle"></i> 시/도 추가
              </button>
            </div>
            <div class="card-body">
              <div class="table-responsive">
                <table class="table table-hover">
                  <thead>
                    <tr>
                      <th>코드</th>
                      <th>이름</th>
                      <th>구/군 수</th>
                      <th>관리</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="sido in sidos" :key="sido.sidoCode">
                      <td>{{ sido.sidoCode }}</td>
                      <td>{{ sido.sidoName }}</td>
                      <td>{{ sido.gugunCount || 0 }}</td>
                      <td>
                        <div class="btn-group btn-group-sm">
                          <button class="btn btn-outline-primary" @click="editSido(sido)">
                            <i class="bi bi-pencil"></i>
                          </button>
                          <button class="btn btn-outline-danger" @click="confirmDeleteSido(sido)">
                            <i class="bi bi-trash"></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        <!-- 구군 관리 -->
        <div class="col-md-6">
          <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
              <h5 class="mb-0">구/군 관리</h5>
              <button class="btn btn-sm btn-primary" @click="showAddGugunModal">
                <i class="bi bi-plus-circle"></i> 구/군 추가
              </button>
            </div>
            <div class="card-body">
              <div class="mb-3">
                <label for="sidoFilter" class="form-label">시/도 필터</label>
                <select class="form-select" id="sidoFilter" v-model="selectedSidoCode" @change="loadGuguns">
                  <option value="">전체 시/도</option>
                  <option v-for="sido in sidos" :key="sido.sidoCode" :value="sido.sidoCode">
                    {{ sido.sidoName }}
                  </option>
                </select>
              </div>

              <div class="table-responsive">
                <table class="table table-hover">
                  <thead>
                    <tr>
                      <th>코드</th>
                      <th>시/도</th>
                      <th>이름</th>
                      <th>관리</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="gugun in filteredGuguns" :key="gugun.gugunCode">
                      <td>{{ gugun.gugunCode }}</td>
                      <td>{{ getSidoName(gugun.sidoCode) }}</td>
                      <td>{{ gugun.gugunName }}</td>
                      <td>
                        <div class="btn-group btn-group-sm">
                          <button class="btn btn-outline-primary" @click="editGugun(gugun)">
                            <i class="bi bi-pencil"></i>
                          </button>
                          <button class="btn btn-outline-danger" @click="confirmDeleteGugun(gugun)">
                            <i class="bi bi-trash"></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 시도 추가/수정 모달 -->
      <div class="modal fade" id="sidoModal" tabindex="-1" aria-labelledby="sidoModalLabel" aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="sidoModalLabel">{{ isEditMode ? '시/도 수정' : '시/도 추가' }}</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
              <form @submit.prevent="saveSido">
                <div class="mb-3">
                  <label for="sidoCode" class="form-label">시/도 코드</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    id="sidoCode" 
                    v-model="currentSido.sidoCode" 
                    :readonly="isEditMode"
                    required
                  >
                  <div class="form-text">시/도 코드는 숫자 2자리로 입력해주세요.</div>
                </div>

                <div class="mb-3">
                  <label for="sidoName" class="form-label">시/도 이름</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    id="sidoName" 
                    v-model="currentSido.sidoName" 
                    required
                  >
                </div>

                <div class="d-grid">
                  <button type="submit" class="btn btn-primary">저장</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>

      <!-- 구군 추가/수정 모달 -->
      <div class="modal fade" id="gugunModal" tabindex="-1" aria-labelledby="gugunModalLabel" aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="gugunModalLabel">{{ isEditMode ? '구/군 수정' : '구/군 추가' }}</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
              <form @submit.prevent="saveGugun">
                <div class="mb-3">
                  <label for="gugunSidoCode" class="form-label">시/도</label>
                  <select 
                    class="form-select" 
                    id="gugunSidoCode" 
                    v-model="currentGugun.sidoCode" 
                    :disabled="isEditMode"
                    required
                  >
                    <option value="">시/도 선택</option>
                    <option v-for="sido in sidos" :key="sido.sidoCode" :value="sido.sidoCode">
                      {{ sido.sidoName }}
                    </option>
                  </select>
                </div>

                <div class="mb-3">
                  <label for="gugunCode" class="form-label">구/군 코드</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    id="gugunCode" 
                    v-model="currentGugun.gugunCode" 
                    :readonly="isEditMode"
                    required
                  >
                  <div class="form-text">구/군 코드는 시/도 코드 + 숫자 3자리로 입력해주세요. (예: 11010)</div>
                </div>

                <div class="mb-3">
                  <label for="gugunName" class="form-label">구/군 이름</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    id="gugunName" 
                    v-model="currentGugun.gugunName" 
                    required
                  >
                </div>

                <div class="d-grid">
                  <button type="submit" class="btn btn-primary">저장</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>

      <!-- 데이터 동기화 섹션 -->
      <div class="row mt-4">
        <div class="col-md-12">
          <div class="card">
            <div class="card-header">
              <h5 class="mb-0">지역 데이터 동기화</h5>
            </div>
            <div class="card-body">
              <div class="row">
                <div class="col-md-6">
                  <p>한국관광공사 API에서 지역 데이터를 가져와 데이터베이스를 업데이트합니다.</p>
                  <button 
                    class="btn btn-success" 
                    @click="syncRegionData" 
                    :disabled="syncing"
                  >
                    <span v-if="syncing" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    지역 데이터 동기화
                  </button>
                </div>

                <div class="col-md-6">
                  <div v-if="syncStatus" class="alert" :class="syncSuccess ? 'alert-success' : 'alert-warning'">
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

                  <div v-if="lastSyncDate" class="mt-3">
                    <strong>마지막 동기화:</strong> {{ formatDateTime(lastSyncDate) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 지역 데이터 관리 컴포넌트
 * 
 * 이 컴포넌트는 관리자가 시/도 및 구/군 데이터를 관리할 수 있는 페이지입니다.
 * 지역 데이터 조회, 추가, 수정, 삭제 및 API 동기화 기능을 제공합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'Regions',
  components: {
    Layout
  },
  data() {
    return {
      loading: true,
      message: '',
      error: '',
      sidos: [],
      guguns: [],
      selectedSidoCode: '',
      isEditMode: false,
      currentSido: {
        sidoCode: '',
        sidoName: ''
      },
      currentGugun: {
        sidoCode: '',
        gugunCode: '',
        gugunName: ''
      },
      syncing: false,
      syncStatus: '',
      syncSuccess: false,
      syncProgress: 0,
      lastSyncDate: null,
      sidoModal: null,
      gugunModal: null
    };
  },
  computed: {
    ...mapState({
      isAdmin: state => state.user.user?.role === 'ADMIN'
    }),

    /**
     * 필터링된 구군 목록
     */
    filteredGuguns() {
      if (!this.selectedSidoCode) {
        return this.guguns;
      }

      return this.guguns.filter(gugun => gugun.sidoCode === this.selectedSidoCode);
    }
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

    // 지역 데이터 로드
    this.loadRegionData();
  },
  mounted() {
    // 부트스트랩 모달 초기화
    this.initModals();
  },
  methods: {
    ...mapActions('admin', [
      'fetchSidos', 
      'fetchGuguns', 
      'createSido', 
      'updateSido', 
      'deleteSido', 
      'createGugun', 
      'updateGugun', 
      'deleteGugun',
      'syncRegions'
    ]),

    /**
     * 부트스트랩 모달 초기화
     */
    initModals() {
      if (window.bootstrap) {
        this.sidoModal = new window.bootstrap.Modal(document.getElementById('sidoModal'));
        this.gugunModal = new window.bootstrap.Modal(document.getElementById('gugunModal'));
      }
    },

    /**
     * 지역 데이터 로드
     */
    async loadRegionData() {
      this.loading = true;

      try {
        // 시도 목록 로드
        this.sidos = await this.fetchSidos();

        // 구군 목록 로드
        this.guguns = await this.fetchGuguns();

        // 마지막 동기화 정보 로드
        this.loadLastSyncInfo();
      } catch (error) {
        console.error('지역 데이터를 불러오는 중 오류가 발생했습니다:', error);
        this.error = '지역 데이터를 불러오는 중 오류가 발생했습니다.';
      } finally {
        this.loading = false;
      }
    },

    /**
     * 구군 목록 로드
     */
    async loadGuguns() {
      try {
        if (this.selectedSidoCode) {
          this.guguns = await this.fetchGuguns(this.selectedSidoCode);
        } else {
          this.guguns = await this.fetchGuguns();
        }
      } catch (error) {
        console.error('구군 목록을 불러오는 중 오류가 발생했습니다:', error);
        this.error = '구군 목록을 불러오는 중 오류가 발생했습니다.';
      }
    },

    /**
     * 마지막 동기화 정보 로드
     */
    async loadLastSyncInfo() {
      try {
        // 실제 구현에서는 API 호출
        // const response = await this.$axios.get('/api/admin/regions/sync/last');
        // this.lastSyncDate = response.data.lastSyncDate;

        // 임시 구현
        this.lastSyncDate = new Date();
        this.lastSyncDate.setDate(this.lastSyncDate.getDate() - 7); // 일주일 전
      } catch (error) {
        console.error('동기화 정보를 불러오는 중 오류가 발생했습니다:', error);
      }
    },

    /**
     * 시도 추가 모달 표시
     */
    showAddSidoModal() {
      this.isEditMode = false;
      this.currentSido = {
        sidoCode: '',
        sidoName: ''
      };
      this.sidoModal.show();
    },

    /**
     * 시도 수정 모달 표시
     * @param {Object} sido - 수정할 시도 객체
     */
    editSido(sido) {
      this.isEditMode = true;
      this.currentSido = { ...sido };
      this.sidoModal.show();
    },

    /**
     * 시도 저장 (추가 또는 수정)
     */
    async saveSido() {
      try {
        if (this.isEditMode) {
          // 시도 수정
          await this.updateSido(this.currentSido);
          this.message = '시/도 정보가 성공적으로 수정되었습니다.';
        } else {
          // 시도 추가
          await this.createSido(this.currentSido);
          this.message = '시/도가 성공적으로 추가되었습니다.';
        }

        // 모달 닫기
        this.sidoModal.hide();

        // 시도 목록 다시 로드
        this.sidos = await this.fetchSidos();
      } catch (error) {
        console.error('시도 저장 중 오류가 발생했습니다:', error);
        this.error = '시도 저장에 실패했습니다. 다시 시도해주세요.';
      }
    },

    /**
     * 시도 삭제 확인
     * @param {Object} sido - 삭제할 시도 객체
     */
    confirmDeleteSido(sido) {
      if (confirm(`정말로 "${sido.sidoName}" 시/도를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)) {
        this.deleteSidoItem(sido.sidoCode);
      }
    },

    /**
     * 시도 삭제 처리
     * @param {string} sidoCode - 삭제할 시도 코드
     */
    async deleteSidoItem(sidoCode) {
      try {
        await this.deleteSido(sidoCode);
        this.message = '시/도가 성공적으로 삭제되었습니다.';

        // 시도 목록 다시 로드
        this.sidos = await this.fetchSidos();

        // 구군 목록 다시 로드
        this.guguns = await this.fetchGuguns();
      } catch (error) {
        console.error('시도 삭제 중 오류가 발생했습니다:', error);
        this.error = '시도 삭제에 실패했습니다. 다시 시도해주세요.';
      }
    },

    /**
     * 구군 추가 모달 표시
     */
    showAddGugunModal() {
      this.isEditMode = false;
      this.currentGugun = {
        sidoCode: this.selectedSidoCode || '',
        gugunCode: '',
        gugunName: ''
      };
      this.gugunModal.show();
    },

    /**
     * 구군 수정 모달 표시
     * @param {Object} gugun - 수정할 구군 객체
     */
    editGugun(gugun) {
      this.isEditMode = true;
      this.currentGugun = { ...gugun };
      this.gugunModal.show();
    },

    /**
     * 구군 저장 (추가 또는 수정)
     */
    async saveGugun() {
      try {
        if (this.isEditMode) {
          // 구군 수정
          await this.updateGugun(this.currentGugun);
          this.message = '구/군 정보가 성공적으로 수정되었습니다.';
        } else {
          // 구군 추가
          await this.createGugun(this.currentGugun);
          this.message = '구/군이 성공적으로 추가되었습니다.';
        }

        // 모달 닫기
        this.gugunModal.hide();

        // 구군 목록 다시 로드
        this.guguns = await this.fetchGuguns();

        // 시도 목록 다시 로드 (구군 수 업데이트를 위해)
        this.sidos = await this.fetchSidos();
      } catch (error) {
        console.error('구군 저장 중 오류가 발생했습니다:', error);
        this.error = '구군 저장에 실패했습니다. 다시 시도해주세요.';
      }
    },

    /**
     * 구군 삭제 확인
     * @param {Object} gugun - 삭제할 구군 객체
     */
    confirmDeleteGugun(gugun) {
      if (confirm(`정말로 "${gugun.gugunName}" 구/군을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)) {
        this.deleteGugunItem(gugun.gugunCode);
      }
    },

    /**
     * 구군 삭제 처리
     * @param {string} gugunCode - 삭제할 구군 코드
     */
    async deleteGugunItem(gugunCode) {
      try {
        await this.deleteGugun(gugunCode);
        this.message = '구/군이 성공적으로 삭제되었습니다.';

        // 구군 목록 다시 로드
        this.guguns = await this.fetchGuguns();

        // 시도 목록 다시 로드 (구군 수 업데이트를 위해)
        this.sidos = await this.fetchSidos();
      } catch (error) {
        console.error('구군 삭제 중 오류가 발생했습니다:', error);
        this.error = '구군 삭제에 실패했습니다. 다시 시도해주세요.';
      }
    },

    /**
     * 지역 데이터 동기화
     */
    async syncRegionData() {
      this.syncing = true;
      this.syncStatus = '동기화 시작 중...';
      this.syncSuccess = true;
      this.syncProgress = 0;

      try {
        // 동기화 시작
        const syncId = await this.syncRegions();

        // 동기화 상태 폴링
        this.pollSyncStatus(syncId);
      } catch (error) {
        console.error('지역 데이터 동기화 시작 중 오류가 발생했습니다:', error);
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
            this.lastSyncDate = new Date();

            // 지역 데이터 다시 로드
            this.loadRegionData();
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
     * 동기화 상태 확인
     * @param {string} syncId - 동기화 ID
     * @returns {Object} 동기화 상태 정보
     */
    async fetchSyncStatus(syncId) {
      // 실제 구현에서는 API 호출
      // return await this.$axios.get(`/api/admin/regions/sync/${syncId}/status`);

      // 임시 구현 (실제로는 API에서 상태를 가져와야 함)
      return new Promise(resolve => {
        const progress = this.syncProgress + Math.floor(Math.random() * 20);
        const finalProgress = Math.min(progress, 100);

        let status = 'IN_PROGRESS';
        let message = `지역 데이터 동기화 중... (${finalProgress}%)`;

        if (finalProgress >= 100) {
          status = 'COMPLETED';
          message = '지역 데이터 동기화가 완료되었습니다.';
        }

        resolve({
          status,
          progress: finalProgress,
          message
        });
      });
    },

    /**
     * 시도 이름 반환
     * @param {string} sidoCode - 시도 코드
     * @returns {string} 시도 이름
     */
    getSidoName(sidoCode) {
      const sido = this.sidos.find(sido => sido.sidoCode === sidoCode);
      return sido ? sido.sidoName : sidoCode;
    },

    /**
     * 날짜 및 시간 포맷팅
     * @param {string|Date} dateTime - 포맷팅할 날짜 및 시간
     * @returns {string} 포맷팅된 날짜 및 시간 문자열
     */
    formatDateTime(dateTime) {
      if (!dateTime) return '';

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

.table th {
  background-color: #f8f9fa;
}

.table td {
  vertical-align: middle;
}

.progress {
  height: 20px;
}

.btn-group {
  white-space: nowrap;
}
</style>
