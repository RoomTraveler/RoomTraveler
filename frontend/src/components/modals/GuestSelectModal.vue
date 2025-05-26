<template>
  <el-dialog
      :model-value="props.show"
      @update:model-value="handleClose"
      title="인원 선택"
      width="320px"
      top="15vh"
      custom-class="guest-selection-dialog"
      :center="true"
  >
    <div class="dialog-content px-3 py-3">
      <div class="d-flex justify-content-between align-items-center">
        <div class="d-flex flex-column">
          <span class="fs-6 fw-semibold text-dark">인원</span>
        </div>
        <div class="d-flex align-items-center gap-2">
          <button
              type="button"
              @click="decrementGuests"
              :disabled="tempGuests <= 1"
              class="btn btn-outline-secondary rounded-circle px-0 d-flex align-items-center justify-content-center"
              style="width:36px; height:36px;"
          >
            <i class="bi bi-dash-lg"></i>
          </button>
          <span class="fs-5 fw-bold text-center" style="width:1.7rem;">{{ tempGuests }}</span>
          <button
              type="button"
              @click="incrementGuests"
              :disabled="tempGuests >= maxGuests"
              class="btn btn-outline-secondary rounded-circle px-0 d-flex align-items-center justify-content-center"
              style="width:36px; height:36px;"
          >
            <i class="bi bi-plus-lg"></i>
          </button>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="w-100 px-3 pb-3">
        <el-button
            type="primary"
            @click="applyGuestSelection"
            class="w-100 btn btn-pink fw-semibold py-2 fs-6 shadow-sm"
        >
          확인
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";

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
.guest-selection-dialog .el-dialog__header {
  display: none;
}
.guest-selection-dialog .el-dialog__body {
  padding: 0 !important;
}
.guest-selection-dialog .el-dialog__footer {
  padding: 0;
  border-top: 1px solid #eee;
}
.btn-pink {
  background-color: #ec4899;
  border-color: #ec4899;
  color: #fff;
}
.btn-pink:hover, .btn-pink:focus {
  background-color: #db2777;
  border-color: #db2777;
  color: #fff;
}
</style>
