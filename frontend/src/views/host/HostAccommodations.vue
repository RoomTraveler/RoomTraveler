<template>
  <div class="container mt-5 mb-5">
    <h1 class="mb-4">
      <i class="bi bi-houses"></i> 호스트 숙소 관리
    </h1>

    <!-- 알림 메시지 표시 -->
    <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
      {{ message }}
      <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
    </div>

    <!-- 숙소 통계 -->
    <div class="row mb-4">
      <StatCard title="총 숙소" :value="accommodations.value.length" />
      <StatCard title="총 객실" :value="totalRooms.value" />
      <StatCard title="활성 숙소" :value="activeAccommodations.value" />
      <StatCard title="평균 평점">
        <span class="text-warning">
          <i class="bi bi-star-fill"></i>
        </span>
        <span>{{ averageRating.value }}</span>
      </StatCard>
    </div>

    <!-- 숙소 목록 -->
    <div v-if="accommodations.value.length === 0" class="alert alert-info">
      <i class="bi bi-info-circle"></i> 등록된 숙소가 없습니다. 아래 + 버튼을 클릭하여 새 숙소를 등록하세요.
    </div>
    <div v-else class="row">
      <div
          v-for="accommodation in accommodations.value"
          :key="accommodation.accommodationId"
          class="col-md-6 col-lg-4 mb-4"
      >
        <AccommodationCard
            :accommodation="accommodation"
            :rooms="getRooms(accommodation.accommodationId)"
            @deleted="handleDelete"
            @room-deleted="handleRoomDelete"
        />
      </div>
    </div>

    <!-- 숙소 추가 버튼 -->
    <router-link to="/accommodation/register-form" class="add-btn">
      <i class="bi bi-plus-lg"></i>
    </router-link>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

// 통계 카드 컴포넌트
const StatCard = defineComponent({
  props: ['title', 'value'],
  setup(props, { slots }) {
    return () => (
        <div class="col-md-3">
          <div class="card stats-card">
            <div class="card-body">
              <h5 class="card-title">{props.title}</h5>
              <p class="card-text fs-2">{slots.default ? slots.default() : props.value}</p>
            </div>
          </div>
        </div>
    );
  }
});

// 숙소 카드 컴포넌트
const AccommodationCard = defineComponent({
  props: ['accommodation', 'rooms'],
  emits: ['deleted', 'room-deleted'],
  setup(props, { emit }) {
    const router = useRouter();

    // 상태 배지 클래스
    function getStatusBadgeClass(status) {
      const baseClass = 'status-badge';
      switch (status) {
        case 'ACTIVE': return `${baseClass} status-active`;
        case 'INACTIVE': return `${baseClass} status-inactive`;
        case 'PENDING_REVIEW': return `${baseClass} status-pending`;
        default: return baseClass;
      }
    }
    // 상태 텍스트
    function getStatusText(status) {
      switch (status) {
        case 'ACTIVE': return '활성';
        case 'INACTIVE': return '비활성';
        case 'PENDING_REVIEW': return '검토중';
        default: return status;
      }
    }
    // 금액 포맷
    function formatCurrency(amount) {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
        maximumFractionDigits: 0
      }).format(amount);
    }
    // 객실 삭제
    async function deleteRoom(roomId) {
      if (!confirm('정말로 이 객실을 삭제하시겠습니까?')) return;
      try {
        const res = await fetch(`/api/rooms/${roomId}`, { method: 'DELETE' });
        if (!res.ok) throw new Error();
        emit('room-deleted');
      } catch {
        alert('객실 삭제에 실패했습니다.');
      }
    }
    // 숙소 삭제
    async function deleteAccommodation(accommodationId) {
      if (!confirm('정말로 이 숙소를 삭제하시겠습니까? 모든 객실 정보도 함께 삭제됩니다.')) return;
      try {
        const res = await fetch(`/api/accommodations/${accommodationId}`, { method: 'DELETE' });
        if (!res.ok) throw new Error();
        emit('deleted');
      } catch {
        alert('숙소 삭제에 실패했습니다.');
      }
    }

    return () => (
        <div class="card accommodation-card">
          <img
              src={props.accommodation.mainImageUrl || require('@/assets/default-accommodation.jpg')}
              class="card-img-top"
              alt={props.accommodation.title}
          />
          <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="card-title mb-0">{props.accommodation.title}</h5>
            <span class={getStatusBadgeClass(props.accommodation.status)}>
            {getStatusText(props.accommodation.status)}
          </span>
          </div>
          <div class="card-body">
            <p class="card-text">
              <i class="bi bi-geo-alt"></i> {props.accommodation.address}
            </p>
            <p class="card-text">
              <i class="bi bi-telephone"></i> {props.accommodation.phone}
            </p>
            {/* 객실 정보 */}
            <div class="mt-3">
              <h6>객실</h6>
              {!props.rooms.length ? (
                  <p class="text-muted">등록된 객실이 없습니다.</p>
              ) : (
                  props.rooms.map((room, idx) => (
                      <div class={"room-card p-2" + (idx !== props.rooms.length - 1 ? " mb-2" : "")} key={room.roomId}>
                        <div class="d-flex justify-content-between align-items-center">
                          <div>
                            <strong>{room.name}</strong>
                            <span class="ms-2 text-muted">{formatCurrency(room.price)}</span>
                          </div>
                          <div>
                            <router-link
                                to={`/accommodation/update-room-form?roomId=${room.roomId}`}
                                class="btn btn-sm btn-outline-primary"
                            >
                              <i class="bi bi-pencil"></i>
                            </router-link>
                            <button
                                class="btn btn-sm btn-outline-danger"
                                onClick={() => deleteRoom(room.roomId)}
                            >
                              <i class="bi bi-trash"></i>
                            </button>
                          </div>
                        </div>
                      </div>
                  ))
              )}
              {/* 객실 추가 */}
              <router-link
                  to={`/accommodation/register-room-form?accommodationId=${props.accommodation.accommodationId}`}
                  class="btn btn-sm btn-outline-success w-100 mt-2"
              >
                <i class="bi bi-plus-circle"></i> 객실 추가
              </router-link>
            </div>
          </div>
          <div class="card-footer">
            <div class="d-flex justify-content-between">
              <router-link
                  to={`/accommodation/detail?accommodationId=${props.accommodation.accommodationId}`}
                  class="btn btn-sm btn-info"
              >
                <i class="bi bi-eye"></i> 보기
              </router-link>
              <div>
                <router-link
                    to={`/accommodation/update-form?accommodationId=${props.accommodation.accommodationId}`}
                    class="btn btn-sm btn-primary"
                >
                  <i class="bi bi-pencil"></i> 수정
                </router-link>
                <button
                    class="btn btn-sm btn-danger"
                    onClick={() => deleteAccommodation(props.accommodation.accommodationId)}
                >
                  <i class="bi bi-trash"></i> 삭제
                </button>
              </div>
            </div>
          </div>
        </div>
    );
  }
});

// 본문 data/상태
const message = ref('');
const accommodations = ref([]);
const roomsByAccommodation = ref({});

const totalRooms = computed(() =>
    Object.values(roomsByAccommodation.value).reduce((total, rooms) => total + rooms.length, 0)
);
const activeAccommodations = computed(() =>
    accommodations.value.filter(acc => acc.status === 'ACTIVE').length
);
const averageRating = computed(() => {
  const sum = accommodations.value.reduce((sum, acc) => sum + (acc.rating || 0), 0);
  return accommodations.value.length > 0
      ? (sum / accommodations.value.length).toFixed(1)
      : '0.0';
});

// 숙소ID별 객실 반환
function getRooms(accommodationId) {
  return roomsByAccommodation.value[accommodationId] || [];
}

// 숙소 및 객실 로드
async function loadAccommodations() {
  try {
    const response = await fetch('/api/host/accommodations');
    if (!response.ok) throw new Error('숙소 정보를 불러오는데 실패했습니다.');
    const data = await response.json();
    accommodations.value = data.accommodations;
    roomsByAccommodation.value = data.roomsByAccommodation;
  } catch {
    message.value = '숙소 정보를 불러오는데 실패했습니다.';
  }
}

// 삭제 후 데이터 갱신
function handleDelete() {
  loadAccommodations();
  message.value = '숙소가 삭제되었습니다.';
}
function handleRoomDelete() {
  loadAccommodations();
  message.value = '객실이 삭제되었습니다.';
}

// 쿼리 메시지 처리
onMounted(() => {
  if (window.location.search) {
    const params = new URLSearchParams(window.location.search);
    if (params.get('message')) message.value = params.get('message');
  }
  loadAccommodations();
});
</script>

<style scoped>
/* 숙소 카드 스타일 */
.accommodation-card {
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  height: 100%;
}
.accommodation-card:hover {
  transform: translateY(-5px);
}
.room-card {
  margin-bottom: 15px;
  border-radius: 8px;
  border: 1px solid #dee2e6;
}
.card-img-top {
  height: 200px;
  object-fit: cover;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
}
.status-badge {
  font-size: 0.8rem;
  padding: 5px 10px;
  border-radius: 20px;
}
.status-active {
  background-color: #198754;
  color: white;
}
.status-inactive {
  background-color: #dc3545;
  color: white;
}
.status-pending {
  background-color: #ffc107;
  color: #212529;
}
.stats-card {
  text-align: center;
  margin-bottom: 20px;
}
.add-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #0d6efd;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  font-size: 1.5rem;
  text-decoration: none;
}
.add-btn:hover {
  background-color: #0b5ed7;
  color: white;
}
</style>
