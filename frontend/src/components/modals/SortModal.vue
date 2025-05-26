<template>
  <div
    v-if="show"
    class="modal fade show d-block"
    tabindex="-1"
    @click.self="closeModal"
    style="background-color: rgba(0, 0, 0, 0.5)"
  >
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable" style="max-width: 400px">
      <div class="modal-content rounded-4 shadow">
        <div class="modal-header border-bottom-0 px-4 pt-4 pb-0">
          <h5 class="modal-title fw-bold fs-6 text-dark">정렬</h5>
          <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
        </div>
        <div class="modal-body p-4">
          <ul class="list-group list-group-flush">
            <li
              v-for="option in props.options"
              :key="option.value"
              class="list-group-item list-group-item-action border-0 px-0 py-3 d-flex justify-content-between align-items-center cursor-pointer"
              @click="selectSort(option.value)"
            >
              <span
                :class="{
                  'text-danger fw-semibold': selectedSortValue === option.value,
                  'text-secondary': selectedSortValue !== option.value,
                }"
              >
                {{ option.label }}
              </span>
              <i v-if="selectedSortValue === option.value" class="bi bi-check-lg text-danger fs-5"></i>
            </li>
          </ul>
        </div>
        <div class="modal-footer border-top-0 p-4">
          <button type="button" class="btn btn-primary w-100 py-2 fw-semibold" @click="applySort">적용하기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, PropType } from "vue";

interface SortOption {
  label: string;
  value: string;
}

const props = defineProps({
  show: {
    type: Boolean,
    required: true,
  },
  currentSort: {
    type: String,
    required: true,
  },
  options: {
    type: Array as PropType<SortOption[]>,
    required: true,
  },
});

const emit = defineEmits(["close", "apply-sort"]);

const selectedSortValue = ref(props.currentSort);

watch(
  () => props.currentSort,
  (newVal) => {
    selectedSortValue.value = newVal;
  }
);

watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      selectedSortValue.value = props.currentSort; // 모달 열릴 때 현재 정렬값으로 초기화
    }
  }
);

function selectSort(value: string) {
  selectedSortValue.value = value;
}

function applySort() {
  emit("apply-sort", selectedSortValue.value);
  closeModal();
}

function closeModal() {
  emit("close");
}
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}
.modal-dialog {
  max-width: 360px; /* 모달 너비 조정 */
  margin-left: auto;
  margin-right: auto;
}
.modal-content {
  border-radius: 0.5rem; /* 모달 테두리 둥글게 */
}
.list-group-item {
  padding-left: 0;
  padding-right: 0;
}
.list-group-item:hover {
  background-color: #f8f9fa;
}
</style>
