<template>
  <el-dialog
    :model-value="props.show"
    @update:model-value="handleClose"
    title="날짜 선택"
    width="700px"
    top="8vh"
    custom-class="date-selection-dialog"
  >
    <div class="dialog-content px-2 px-sm-3">
      <div class="alert alert-info d-flex align-items-center py-2 px-2 mb-3 rounded" role="alert">
        <i class="bi bi-info-circle-fill me-2"></i>
        이 숙소는 최대 <strong class="ms-1 me-1">{{ maxBookingDays }}</strong
        >박까지 예약할 수 있어요.
      </div>
      <el-calendar v-model="internalCalendarDate">
        <template #header="{ date }">
          <div class="d-flex justify-content-between align-items-center w-100 mb-2">
            <el-button link @click="prevMonth" class="text-primary fw-bold px-1 py-0">&lt; 이전 달</el-button>
            <span class="fs-6 fw-semibold text-dark">{{ date }}</span>
            <el-button link @click="nextMonth" class="text-primary fw-bold px-1 py-0">다음 달 &gt;</el-button>
          </div>
        </template>
        <template #date-cell="{ data }">
          <div
            class="date-cell-content w-100 h-100 d-flex flex-column align-items-center justify-content-center"
            :class="{
              selected: isDateSelected(data.day),
              disabled: isDateDisabled(data.day),
              'other-month': isOtherMonth(data.day),
            }"
            @click="handleDateClick(data.day)"
          >
            <span
              class="day-number"
              :class="{
                'text-danger': [0, 6].includes(new Date(data.day).getDay()),
                'text-secondary': ![0, 6].includes(new Date(data.day).getDay()),
              }"
            >
              {{ data.day.split("-").slice(2).join("") }}
            </span>
          </div>
        </template>
      </el-calendar>
    </div>
    <template #footer>
      <div class="d-flex justify-content-between align-items-center w-100 px-2 px-sm-3 pb-2">
        <el-button @click="resetDateSelection" link class="text-secondary">초기화</el-button>
        <el-button
          type="primary"
          @click="applyDateSelection"
          class="flex-grow-1 flex-sm-grow-0"
          :style="{ minWidth: '130px', padding: '8px 0', fontWeight: '600', borderRadius: '0.5rem' }"
          :disabled="!tempSelectedCheckInDate || !tempSelectedCheckOutDate"
        >
          {{ selectedRangeFooterDisplayComputed }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from "vue";
import { ElCalendar, ElButton, ElDialog } from "element-plus";

const KOREAN_DAYS = ["일", "월", "화", "수", "목", "금", "토"];

interface Props {
  show: boolean;
  initialDateRange?: [Date, Date] | null;
  maxBookingDays?: number;
}

const props = withDefaults(defineProps<Props>(), {
  show: false,
  initialDateRange: null,
  maxBookingDays: 9,
});

const emit = defineEmits(["close", "apply", "update:show"]);

const internalCalendarDate = ref(new Date());
const tempSelectedCheckInDate = ref<Date | null>(null);
const tempSelectedCheckOutDate = ref<Date | null>(null);

// 현재 보고 있는 연월 키 (YYYY*12 + MM)
const currentMonthKey = computed(() => {
  const d = internalCalendarDate.value;
  return d.getFullYear() * 12 + d.getMonth();
});

function isOtherMonth(dateStr: string): boolean {
  const cellDate = new Date(dateStr);
  const cellKey = cellDate.getFullYear() * 12 + cellDate.getMonth();
  return cellKey !== currentMonthKey.value;
}

function handleClose() {
  emit("update:show", false);
  emit("close");
}

watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      if (props.initialDateRange && props.initialDateRange[0]) {
        const initialStart = new Date(props.initialDateRange[0]);
        initialStart.setHours(0, 0, 0, 0);
        tempSelectedCheckInDate.value = initialStart;
        internalCalendarDate.value = new Date(initialStart);

        if (props.initialDateRange[1]) {
          const initialEnd = new Date(props.initialDateRange[1]);
          initialEnd.setHours(0, 0, 0, 0);
          tempSelectedCheckOutDate.value = initialEnd;
        } else {
          tempSelectedCheckOutDate.value = null;
        }
      } else {
        tempSelectedCheckInDate.value = null;
        tempSelectedCheckOutDate.value = null;
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        internalCalendarDate.value = new Date(today);
      }
    }
  },
  { immediate: true }
);

function calculateNights(startDate: Date | null, endDate: Date | null): number {
  if (!startDate || !endDate) return 0;
  if (startDate.getTime() === endDate.getTime()) return 0;
  const diffTime = Math.abs(endDate.getTime() - startDate.getTime());
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

function formatDateWithDay(date: Date | null): string {
  if (!date) return "";
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const dayOfWeek = KOREAN_DAYS[date.getDay()];
  return `${month}.${day}(${dayOfWeek})`;
}

const selectedRangeFooterDisplayComputed = computed(() => {
  if (!tempSelectedCheckInDate.value) return "날짜를 선택해주세요";
  if (!tempSelectedCheckOutDate.value) return `${formatDateWithDay(tempSelectedCheckInDate.value)} 선택됨`;
  const nights = calculateNights(tempSelectedCheckInDate.value, tempSelectedCheckOutDate.value);
  return `${formatDateWithDay(tempSelectedCheckInDate.value)} ~ ${formatDateWithDay(
    tempSelectedCheckOutDate.value
  )} • ${nights}박`;
});

function prevMonth() {
  const d = new Date(internalCalendarDate.value);
  d.setMonth(d.getMonth() - 1);
  internalCalendarDate.value = d;
}

function nextMonth() {
  const d = new Date(internalCalendarDate.value);
  d.setMonth(d.getMonth() + 1);
  internalCalendarDate.value = d;
}

function isDateSelected(dateStr: string): boolean {
  const date = new Date(dateStr);
  date.setHours(0, 0, 0, 0);

  if (tempSelectedCheckInDate.value && tempSelectedCheckOutDate.value) {
    return date >= tempSelectedCheckInDate.value && date <= tempSelectedCheckOutDate.value;
  }
  if (tempSelectedCheckInDate.value) {
    return date.getTime() === tempSelectedCheckInDate.value.getTime();
  }
  return false;
}

function isDateDisabled(dateStr: string): boolean {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const target = new Date(dateStr);
  target.setHours(0, 0, 0, 0);
  return target < today;
}

function handleDateClick(dateStr: string) {
  if (isDateDisabled(dateStr) || isOtherMonth(dateStr)) return;
  const clicked = new Date(dateStr);
  clicked.setHours(0, 0, 0, 0);

  if (!tempSelectedCheckInDate.value || (tempSelectedCheckInDate.value && tempSelectedCheckOutDate.value)) {
    tempSelectedCheckInDate.value = clicked;
    tempSelectedCheckOutDate.value = null;
  } else if (clicked < tempSelectedCheckInDate.value) {
    tempSelectedCheckInDate.value = clicked;
    tempSelectedCheckOutDate.value = null;
  } else if (clicked.getTime() === tempSelectedCheckInDate.value.getTime()) {
    tempSelectedCheckInDate.value = null;
    tempSelectedCheckOutDate.value = null;
  } else {
    const nights = calculateNights(tempSelectedCheckInDate.value, clicked);
    if (nights > props.maxBookingDays) {
      tempSelectedCheckInDate.value = clicked;
      tempSelectedCheckOutDate.value = null;
    } else {
      tempSelectedCheckOutDate.value = clicked;
    }
  }
}

function resetDateSelection() {
  tempSelectedCheckInDate.value = null;
  tempSelectedCheckOutDate.value = null;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  internalCalendarDate.value = today;
}

function applyDateSelection() {
  if (tempSelectedCheckInDate.value && tempSelectedCheckOutDate.value) {
    emit("apply", [new Date(tempSelectedCheckInDate.value), new Date(tempSelectedCheckOutDate.value)]);
    handleClose();
  }
}

onMounted(() => {});
</script>

<style scoped>
.date-cell-content {
  cursor: pointer;
  border-radius: 0.25rem;
  min-height: 34px;
  padding: 2px 0;
}
.date-cell-content.selected {
  background-color: #ffe0f0;
  color: #d63384;
  font-weight: bold;
}
.date-cell-content.disabled {
  color: #adb5bd !important;
  cursor: not-allowed;
  background-color: #f8f9fa !important;
}
.date-cell-content:not(.disabled):hover {
  background-color: #fff0f6;
}
.day-number {
  font-size: 0.92rem;
}
/* 이번 달이 아닌(이전/다음 달) 날짜 처리 */
.date-selection-dialog .date-cell-content.other-month {
  background-color: #e7f5ff !important; /* 연한 파란색 배경 적용 */
}

.date-selection-dialog .date-cell-content.other-month .day-number {
  color: #adb5bd !important;
  opacity: 1;
}
.date-selection-dialog .date-cell-content.other-month:not(.disabled):hover {
  background-color: #e7f5ff !important; /* 마우스 오버 시에도 연한 파란색 배경 유지 */
}
</style>

<style>
/* 모달 최대폭 제한 및 반응형 */
.date-selection-dialog .el-dialog {
  max-width: 95vw;
  width: 700px;
}
@media (max-width: 750px) {
  .date-selection-dialog .el-dialog {
    width: 98vw !important;
    margin: 0 auto;
  }
}
.date-selection-dialog .el-dialog__body {
  padding: 8px 4px 0 4px;
}
.date-selection-dialog .el-dialog__footer {
  padding: 6px 4px 8px 4px;
}
</style>
