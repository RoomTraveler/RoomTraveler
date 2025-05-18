<template>
  <el-dialog
    :model-value="props.show"
    @update:model-value="handleClose"
    title="지역 선택"
    width="90%"
    :max-width="'600px'"
    top="10vh"
    custom-class="region-selection-dialog"
  >
    <div class="dialog-content">
      <div :class="['flex gap-x-3 min-h-[calc(100%-0px)] sm:min-h-[400px] max-h-[60vh] sm:max-h-[50vh] overflow-y-hidden', gugunList.length > 0 && selectedSidoCode !== ALL_SIDO_CODE ? 'sm:gap-x-4' : '']">
        <div class="flex-1 overflow-y-auto custom-scrollbar pr-1 sm:pr-2 border-r border-gray-200">
          <div
            v-for="sido in sidoList"
            :key="sido.code === null ? 'sido-all' : sido.code"
            class="py-2.5 px-2 mb-1.5 rounded-lg cursor-pointer select-none transition-all duration-150 ease-in-out font-medium text-sm text-gray-700 hover:text-pink-600 hover:bg-pink-50 text-center"
            :class="
              sido.code === selectedSidoCode 
                ? 'bg-pink-100 text-pink-700 font-semibold shadow-md scale-105'
                : 'bg-white hover:shadow-sm'
            "
            @click="selectSido(sido)" 
          >
            {{ sido.name }}
          </div>
        </div>
        <div class="flex-1 overflow-y-auto custom-scrollbar pl-1 sm:pl-2" v-if="gugunList.length > 0 && selectedSidoCode !== ALL_SIDO_CODE">
          <div
            v-for="gugun in gugunList" 
            :key="gugun.code === null ? 'gugun-all' : gugun.code"
            class="py-2.5 px-2 mb-1.5 rounded-lg cursor-pointer select-none transition-all duration-150 ease-in-out font-medium text-sm text-gray-700 hover:text-pink-600 hover:bg-pink-50 text-center"
            :class="
              gugun.code === selectedGugunCode 
                ? 'bg-pink-100 text-pink-700 font-semibold shadow-md scale-105'
                : 'bg-white hover:shadow-sm'
            "
            @click="selectGugun(gugun)" 
          >
            {{ gugun.name }}
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="w-full px-2 sm:px-4 pb-1">
        <el-button 
          type="primary" 
          @click="apply" 
          class="w-full bg-pink-500 hover:bg-pink-600 border-pink-500 py-2.5 text-base font-semibold rounded-md"
          :disabled="!canApply"
        >
          선택 완료
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from "vue";
import { ElDialog, ElButton } from 'element-plus';
import regionApi from '@/api/regionApi';

const props = defineProps({
  show: Boolean,
});

const emit = defineEmits(["close", "selected", "update:show"]);

const ALL_SIDO_CODE = 0; 
const ALL_SIDO_NAME = "전체";
const ALL_GUGUN_NAME = "전체";

const sidoList = ref<any[]>([]); 
const gugunList = ref<any[]>([]); 
const selectedSidoCode = ref<number | null>(null);
const selectedGugunCode = ref<number | null>(null);
const selectedSidoName = ref("");
const selectedGugunName = ref("");

const canApply = computed(() => {
  if (selectedSidoCode.value === ALL_SIDO_CODE) return true;
  if (selectedSidoCode.value !== null && selectedSidoCode.value !== ALL_SIDO_CODE && selectedGugunCode.value !== undefined) return true;
  return false;
});

watch(
  () => props.show,
  (newShow) => {
    if (newShow) {
      selectedSidoCode.value = null;
      selectedSidoName.value = "";
      selectedGugunCode.value = null;
      selectedGugunName.value = "";
      gugunList.value = [];
      fetchSido();
    } 
  },
  { immediate: false }
);

async function fetchSido() {
  try {
    const response = await regionApi.getSidos();
    const data = response.data;
    if (Array.isArray(data)) {
      sidoList.value = [{ code: ALL_SIDO_CODE, name: ALL_SIDO_NAME }, ...data];
    } else {
      sidoList.value = [{ code: ALL_SIDO_CODE, name: ALL_SIDO_NAME }];
    }
  } catch (err: any) {
    console.error("Error fetching sido list:", err);
    sidoList.value = [{ code: ALL_SIDO_CODE, name: ALL_SIDO_NAME }];
  }
}

async function fetchGugun(sidoCodeValue: number) {
  try {
    const response = await regionApi.getGuguns(sidoCodeValue);
    const data = response.data;
    gugunList.value = [{ code: null, name: ALL_GUGUN_NAME }, ...data]; 
    selectedGugunCode.value = null;
    selectedGugunName.value = ALL_GUGUN_NAME;
  } catch (err) {
    console.error("Error fetching gugun list:", err);
    gugunList.value = [{ code: null, name: ALL_GUGUN_NAME }]; 
  }
}

function selectSido(sido: any) {
  selectedSidoCode.value = sido.code;
  selectedSidoName.value = sido.name;

  if (sido.code === ALL_SIDO_CODE) {
    gugunList.value = []; 
    selectedGugunCode.value = null;
    selectedGugunName.value = "";
  } else {
    fetchGugun(sido.code);
  }
}

function selectGugun(gugun: any) {
  selectedGugunCode.value = gugun.code;
  selectedGugunName.value = gugun.name;
}

function apply() {
  if (!canApply.value) return;

  let finalSidoCode: number | null = selectedSidoCode.value;
  let finalGugunCode: number | null = selectedGugunCode.value;
  let displayName = "";

  if (finalSidoCode === null) {
    return;
  }

  if (finalSidoCode === ALL_SIDO_CODE) {
    displayName = ALL_SIDO_NAME;
    finalGugunCode = null;
  } else {
    displayName = selectedSidoName.value;
    if (finalGugunCode !== null && selectedGugunName.value && selectedGugunName.value !== ALL_GUGUN_NAME) {
      displayName += ` ${selectedGugunName.value}`;
    } else {
      finalGugunCode = null;
    }
  }

  emit("selected", {
    sidoCode: finalSidoCode,
    gugunCode: finalGugunCode,
    name: displayName,
  });
  handleClose();
}

function handleClose() {
  emit('update:show', false);
  emit('close');
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.dialog-content {
  padding: 8px;
}

@media (min-width: 640px) {
  .dialog-content {
    padding: 12px;
  }
}
</style>

<style>
.region-selection-dialog .el-dialog__header {
  display: none;
}
.region-selection-dialog .el-dialog__body {
  padding: 16px 4px 8px 4px !important;
}
@media (min-width: 640px) {
  .region-selection-dialog .el-dialog__body {
    padding: 20px 8px 12px 8px !important;
  }
}

.region-selection-dialog .el-dialog__footer {
  padding: 0rem 0.5rem 0.5rem 0.5rem;
}

@media (min-width: 640px) {
  .region-selection-dialog .el-dialog__footer {
    padding: 0rem 1rem 0.5rem 1rem;
  }
}
</style>