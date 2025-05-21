<template>
  <el-dialog
    :model-value="props.show"
    @update:model-value="handleClose"
    title="인원 선택"
    width="90%"
    :max-width="'320px'"
    top="20vh"
    custom-class="guest-selection-dialog"
    :center="true"
  >
    <div class="dialog-content px-4 py-6">
      <div class="flex justify-between items-center">
        <div class="flex flex-col">
          <span class="text-lg font-medium text-gray-800">인원</span>
        </div>
        <div class="flex items-center gap-x-3">
          <button
            @click="decrementGuests"
            :disabled="tempGuests <= 1"
            class="p-2 w-10 h-10 rounded-full border border-gray-300 text-gray-700 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center text-xl"
          >
            <i class="bi bi-dash-lg"></i>
          </button>
          <span class="text-xl font-semibold w-8 text-center">{{ tempGuests }}</span>
          <button
            @click="incrementGuests"
            :disabled="tempGuests >= maxGuests"
            class="p-2 w-10 h-10 rounded-full border border-gray-300 text-gray-700 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center text-xl"
          >
            <i class="bi bi-plus-lg"></i>
          </button>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="w-full px-4 pb-4">
        <el-button
          type="primary"
          @click="applyGuestSelection"
          class="w-full bg-pink-500 hover:bg-pink-600 border-pink-500 py-3 text-base font-semibold rounded-lg shadow-md"
        >
          확인
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { ElButton, ElDialog } from "element-plus";

interface Props {
  show: boolean;
  initialGuests?: number;
  maxGuests?: number;
}

const props = withDefaults(defineProps<Props>(), {
  show: false,
  initialGuests: 1,
  maxGuests: 10,
});

const emit = defineEmits(["close", "apply", "update:show"]);

const tempGuests = ref(props.initialGuests);

function handleClose() {
  emit("update:show", false);
  emit("close");
}

watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      tempGuests.value = props.initialGuests;
    }
  }
);

watch(
  () => props.initialGuests,
  (newVal) => {
    tempGuests.value = newVal;
  }
);

function incrementGuests() {
  if (tempGuests.value < props.maxGuests) {
    tempGuests.value++;
  }
}

function decrementGuests() {
  if (tempGuests.value > 1) {
    tempGuests.value--;
  }
}

function applyGuestSelection() {
  emit("apply", { guests: tempGuests.value });
  handleClose();
}
</script>

<style scoped>
/* 필요한 경우 scoped 스타일 추가 */
.guest-selection-dialog .el-dialog__header {
  display: none;
}
.guest-selection-dialog .el-dialog__body {
  padding: 0 !important;
}
.guest-selection-dialog .el-dialog__footer {
  padding: 0; /* 내부에서 패딩 관리 */
  border-top: 1px solid #eee; /* 구분선 추가 */
}
</style>
<style>
.guest-selection-dialog .el-dialog__headerbtn .el-dialog__close {
  font-size: 20px; /* Element Plus 기본 닫기 버튼 아이콘 크기 조정 */
  color: #555;
}
.guest-selection-dialog .el-dialog__headerbtn:hover .el-dialog__close {
  color: #e53e3e; /* 호버 시 색상 변경 */
}
</style>
