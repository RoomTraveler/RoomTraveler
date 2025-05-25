<template>
  <div class="host-room-list-container p-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3>객실 관리 (숙소 ID: {{ accommodationId }})</h3>
      <el-button type="primary" @click="goToNewRoomForm">새 객실 추가</el-button>
    </div>

    <el-table :data="rooms" style="width: 100%" v-loading="isLoading">
      <el-table-column prop="roomId" label="ID" width="80" />
      <el-table-column label="대표 이미지" width="120">
        <template #default="{ row }">
          <el-image 
            style="width: 100px; height: 70px" 
            :src="getMainImageUrl(row.images)" 
            fit="cover"
            lazy
          >
            <template #error>
              <div class="image-slot-error"><span>이미지 없음</span></div>
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="객실명" />
      <el-table-column prop="roomType" label="객실 타입" width="150" />
      <el-table-column prop="price" label="가격" width="120">
        <template #default="{ row }">
          {{ formatPrice(row.price) }}
        </template>
      </el-table-column>
      <el-table-column prop="capacity" label="기준 인원" width="100" />
      <el-table-column prop="maxCapacity" label="최대 인원" width="100" />
      <el-table-column prop="status" label="상태" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="작업" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="goToEditRoomForm(row.roomId)">수정</el-button>
          <el-button size="small" type="danger" @click="handleDeleteRoom(row.roomId)">삭제</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElTable, ElTableColumn, ElButton, ElImage, ElTag, ElMessage, ElMessageBox } from 'element-plus';
import { getRoomsByAccommodationForHost, deleteRoom } from '@/api/hostApi';

const props = defineProps({
  accommodationId: {
    type: [String, Number],
    required: true,
  },
});

const router = useRouter();
const rooms = ref([]);
const isLoading = ref(false);

const fetchRooms = async () => {
  isLoading.value = true;
  try {
    const data = await getRoomsByAccommodationForHost(props.accommodationId);
    rooms.value = data; // API 응답이 객실 배열이라고 가정
  } catch (error) {
    ElMessage.error('객실 목록을 불러오는 중 오류가 발생했습니다.');
    console.error('Error fetching rooms:', error);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchRooms();
});

const getMainImageUrl = (images) => {
  if (!images || images.length === 0) return ''; // 기본 이미지 URL 또는 빈 문자열
  const mainImage = images.find(img => img.isMain);
  return mainImage ? mainImage.imageUrl : images[0].imageUrl; // 대표 이미지가 없으면 첫 번째 이미지 사용
};

const formatPrice = (price) => {
  return price ? `${Number(price).toLocaleString()}원` : '가격 정보 없음';
};

const getStatusTagType = (status) => {
  if (status === 'AVAILABLE') return 'success';
  if (status === 'UNAVAILABLE') return 'info';
  if (status === 'UNDER_MAINTENANCE') return 'warning';
  return 'danger'; // PENDING_DELETION 등
};

const goToNewRoomForm = () => {
  router.push({ name: 'HostRoomNew', params: { accommodationId: props.accommodationId } });
};

const goToEditRoomForm = (roomId) => {
  router.push({ name: 'HostRoomEdit', params: { roomId: roomId } });
};

const handleDeleteRoom = async (roomId) => {
  try {
    await ElMessageBox.confirm('정말로 이 객실을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.', '경고', {
      confirmButtonText: '삭제',
      cancelButtonText: '취소',
      type: 'warning',
    });
    await deleteRoom(roomId);
    ElMessage.success('객실이 성공적으로 삭제되었습니다.');
    fetchRooms(); // 목록 새로고침
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('객실 삭제 중 오류가 발생했습니다.');
      console.error('Error deleting room:', error);
    }
  }
};

</script>

<style scoped>
.host-room-list-container {
  max-width: 1200px;
  margin: 20px auto;
}
.image-slot-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  font-size: 14px;
}
</style> 