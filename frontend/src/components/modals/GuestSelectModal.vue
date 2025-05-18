<template>
  <el-dialog
    :model-value="props.show"
    @update:model-value="handleClose"
    title="인원 선택"
    width="90%"
    :max-width="'400px'"
    top="15vh"
    custom-class="guest-selection-dialog"
    :center="true"
  >
    <div class="dialog-content px-2 sm:px-4">

      <!-- 기존 인원 선택 UI (AccommodationDetail.vue에서 가져옴) -->
      <div class="py-4">
        <div class="flex justify-between items-center mb-6">
          <div class="flex flex-col">
            <span class="text-lg font-medium text-gray-800">성인</span>
            <span class="text-xs text-gray-500">만 13세 이상</span>
          </div>
          <div class="flex items-center gap-x-3">
            <button @click="decrementAdults" :disabled="tempAdults <= 1" class="p-2 w-8 h-8 rounded-full border border-gray-300 text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center">
              <i class="bi bi-dash-lg"></i>
            </button>
            <span class="text-lg font-medium w-6 text-center">{{ tempAdults }}</span>
            <button @click="incrementAdults" :disabled="tempAdults >= maxAdults" class="p-2 w-8 h-8 rounded-full border border-gray-300 text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center">
              <i class="bi bi-plus-lg"></i>
            </button>
          </div>
        </div>

        <div class="flex justify-between items-center">
          <div class="flex flex-col">
            <span class="text-lg font-medium text-gray-800">아동</span>
            <span class="text-xs text-gray-500">만 12세 이하</span>
          </div>
          <div class="flex items-center gap-x-3">
            <button @click="decrementChildren" :disabled="tempChildren <= 0" class="p-2 w-8 h-8 rounded-full border border-gray-300 text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center">
              <i class="bi bi-dash-lg"></i>
            </button>
            <span class="text-lg font-medium w-6 text-center">{{ tempChildren }}</span>
            <button @click="incrementChildren" :disabled="tempChildren >= maxChildren" class="p-2 w-8 h-8 rounded-full border border-gray-300 text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center">
              <i class="bi bi-plus-lg"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="w-full px-2 sm:px-4 pb-2">
        <el-button 
          type="primary" 
          @click="applyGuestSelection" 
          class="w-full bg-pink-500 hover:bg-pink-600 border-pink-500 py-3 text-base font-semibold rounded-md"
        >
          확인
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { ElButton, ElDialog } from 'element-plus';

interface Props {
  show: boolean;
  initialAdults?: number;
  initialChildren?: number;
  maxAdults?: number;
  maxChildren?: number;
}

const props = withDefaults(defineProps<Props>(), {
  show: false,
  initialAdults: 2,
  initialChildren: 0,
  maxAdults: 10,
  maxChildren: 5,
});

const emit = defineEmits(['close', 'apply', 'update:show']);

const tempAdults = ref(props.initialAdults);
const tempChildren = ref(props.initialChildren);

function handleClose() {
  emit('update:show', false);
  emit('close');
}

watch(() => props.show, (newVal) => {
  if (newVal) {
    tempAdults.value = props.initialAdults;
    tempChildren.value = props.initialChildren;
  }
});

watch(() => props.initialAdults, (newVal) => {
  tempAdults.value = newVal;
});

watch(() => props.initialChildren, (newVal) => {
  tempChildren.value = newVal;
});

function incrementAdults() {
  if (tempAdults.value < props.maxAdults) {
    tempAdults.value++;
  }
}

function decrementAdults() {
  if (tempAdults.value > 1) {
    tempAdults.value--;
  }
}

function incrementChildren() {
  if (tempChildren.value < props.maxChildren) {
    tempChildren.value++;
  }
}

function decrementChildren() {
  if (tempChildren.value > 0) {
    tempChildren.value--;
  }
}

function applyGuestSelection() {
  emit('apply', { adults: tempAdults.value, children: tempChildren.value });
  handleClose();
}
</script>

<style scoped>
/* scoped 스타일은 필요 시 유지 또는 수정 */
/* 예: .dialog-content 내부 요소들에 대한 스타일 */
</style>
<style>
/* Global-like styles for el-dialog via custom-class */
.guest-selection-dialog .el-dialog__header {
  display: none; /* 커스텀 닫기 버튼 사용 시 헤더 숨김 */
}
.guest-selection-dialog .el-dialog__body {
  padding: 0 !important; /* AccommodationDetail.vue 스타일과 일치시키거나, 내부 wrapper로 패딩 관리 */
}
/* px-2 sm:px-4가 dialog-content에 있으므로 body의 padding은 0으로 해도 내부 패딩 유지됨 */

.guest-selection-dialog .el-dialog__footer {
  padding: 0rem 0.5rem 0.5rem 0.5rem; /* px-2 pb-2 와 유사하게 */
}

@media (min-width: 640px) { /* sm breakpoint */
  .guest-selection-dialog .el-dialog__footer {
    padding: 0rem 1rem 0.5rem 1rem; /* sm:px-4 pb-2 와 유사하게 */
  }
}
</style> 