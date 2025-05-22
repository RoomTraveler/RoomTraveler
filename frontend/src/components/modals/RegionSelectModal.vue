<template>
  <el-dialog
      :model-value="props.show"
      @update:model-value="handleClose"
      title="지역 선택"
      width="700px"
      :max-width="'700px'"
      top="10vh"
      custom-class="region-selection-dialog"
  >
    <div class="dialog-content p-2">
      <div
          class="d-flex flex-row gap-3 region-lists-container"
          style="min-height:210px; max-height:330px;"
      >
        <!-- 시도 리스트 -->
        <div class="flex-fill region-list-scroll pe-3 border-end">
          <div
              v-for="sido in sidoList"
              :key="sido.code === null ? 'sido-all' : sido.code"
              class="py-2 px-1 mb-1 rounded-3 text-center fw-semibold fs-5 text-secondary bg-white region-item"
              :class="{
              'bg-light text-primary shadow-sm scale-105': sido.code === selectedSidoCode,
              'region-item-hover': sido.code !== selectedSidoCode
            }"
              style="cursor:pointer; user-select:none; transition:all .15s;"
              @click="selectSido(sido)"
          >
            {{ sido.name }}
          </div>
        </div>
        <!-- 구군 리스트 -->
        <div
            v-if="gugunList.length > 0 && selectedSidoCode !== ALL_SIDO_CODE"
            class="flex-fill region-list-scroll ps-3"
        >
          <div
              v-for="gugun in gugunList"
              :key="gugun.code === null ? 'gugun-all' : gugun.code"
              class="py-2 px-1 mb-1 rounded-3 text-center fw-semibold fs-5 text-secondary bg-white region-item"
              :class="{
              'bg-light text-primary shadow-sm scale-105': gugun.code === selectedGugunCode,
              'region-item-hover': gugun.code !== selectedGugunCode
            }"
              style="cursor:pointer; user-select:none; transition:all .15s;"
              @click="selectGugun(gugun)"
          >
            {{ gugun.name }}
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="w-100 px-2 pb-2">
        <el-button
            type="primary"
            @click="apply"
            class="w-100 py-3 fs-5 fw-bold rounded-2"
            :disabled="!canApply"
            style="background-color:#e83e8c; border-color:#e83e8c;"
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
import regionApi from '../../api/regionApi';

// props, emit 정의
const props = defineProps<{ show: boolean }>();
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
    }
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

  if (finalSidoCode === null) return;

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
/* flex-row에 강제 가로 스크롤 방지 */
.region-lists-container {
  min-width: 0;
  overflow-x: hidden !important;
}

/* 각 리스트는 세로 스크롤만 허용 */
.region-list-scroll {
  max-height: 270px;
  overflow-y: auto;
  overflow-x: hidden;
}
.region-item.region-item-hover:hover {
  background-color: #f8d7da;
  color: #e83e8c;
  box-shadow: 0 2px 8px 0 rgba(232, 62, 140, .08);
}
.scale-105 {
  transform: scale(1.05);
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
  padding: 18px 12px 8px 12px !important;
}
.region-selection-dialog .el-dialog__footer {
  padding: 0 1.5rem 0.8rem 1.5rem;
}
</style>
