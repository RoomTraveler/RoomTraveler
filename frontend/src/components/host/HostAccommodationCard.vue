<template>
  <el-card class="accommodation-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="fw-bold">{{ accommodation.title }}</span>
        <el-tag :type="statusTagType" size="small">{{ statusText }}</el-tag>
      </div>
    </template>
    <div class="card-body">
      <div class="image-container mb-2" style="height: 200px">
        <img
          v-if="accommodation.thumbnailImageUrl || accommodation.mainImageUrl"
          :src="(accommodation.thumbnailImageUrl || accommodation.mainImageUrl).replace('http://', 'https://')"
          :alt="accommodation.title || '숙소 이미지'"
          style="width: 100%; height: 100%; object-fit: cover; border-radius: 4px"
        />
        <div v-else class="no-image-placeholder">
          <el-icon :size="40"><Picture /></el-icon>
          <span class="mt-1 small text-muted">이미지 없음</span>
        </div>
      </div>

      <p class="card-text address mt-2 mb-1">
        <el-icon><Location /></el-icon> {{ accommodation.address }}
      </p>
      <p class="card-text type mb-1"><strong>타입:</strong> {{ accommodation.accommodationType }}</p>
      <p class="card-text price" v-if="accommodation.minRoomPrice != null">
        <strong>가격:</strong> {{ formatCurrency(accommodation.minRoomPrice) }} ~
      </p>
    </div>
    <div class="card-footer text-end">
      <el-button type="primary" size="small" @click="goToDetail">상세보기</el-button>
      <el-button
        v-if="accommodation.status === 'ACTIVE' || accommodation.status === 'PENDING_REVIEW'"
        type="success"
        size="small"
        @click="goToRoomManagement"
        >객실 관리</el-button
      >
      <el-button type="warning" size="small" @click="goToEdit" v-if="showEditButton">수정</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from "vue";
import { useRouter } from "vue-router";
import { ElCard, ElTag, ElButton, ElIcon } from "element-plus";
import { Picture, Location } from "@element-plus/icons-vue";

const props = defineProps({
  accommodation: {
    type: Object,
    required: true,
  },
  showEditButton: {
    type: Boolean,
    default: true,
  },
});

const router = useRouter();

const statusMap = {
  ACTIVE: { text: "운영중", type: "success" },
  INACTIVE: { text: "비활성", type: "info" },
  PENDING_REVIEW: { text: "검토중", type: "warning" },
  REJECTED: { text: "반려됨", type: "danger" },
};

const statusInfo = computed(() => {
  return statusMap[props.accommodation.status] || { text: props.accommodation.status, type: "info" };
});

const statusText = computed(() => statusInfo.value.text);
const statusTagType = computed(() => statusInfo.value.type);

function formatCurrency(value) {
  if (value == null) return "";
  return new Intl.NumberFormat("ko-KR", { style: "currency", currency: "KRW" }).format(value);
}

function goToDetail() {
  router.push({ name: "HostAccommodationDetail", params: { accommodationId: props.accommodation.accommodationId } });
}

function goToEdit() {
  router.push({ name: "HostAccommodationEdit", params: { accommodationId: props.accommodation.accommodationId } });
}

function goToRoomManagement() {
  router.push({ name: "HostRoomList", params: { accommodationId: props.accommodation.accommodationId } });
}
</script>

<style scoped>
.accommodation-card {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.accommodation-image {
  width: 100%;
  background-color: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-container {
  width: 100%;
  background-color: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
  overflow: hidden;
}

.no-image-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: var(--el-text-color-secondary);
}

.card-text {
  font-size: 0.9rem;
  color: #606266;
}
.card-text.address .el-icon {
  vertical-align: middle;
  margin-right: 4px;
}
.card-body {
  flex-grow: 1;
}
.card-footer {
  border-top: 1px solid var(--el-card-border-color);
  padding-top: 10px;
  margin-top: auto;
}
</style>
