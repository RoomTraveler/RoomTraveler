<template>
  <el-dialog
    :model-value="props.show"
    @update:model-value="handleClose"
    title="날짜 선택"
    width="90%"
    top="5vh"
    custom-class="date-selection-dialog"
  >
    <div class="dialog-content px-2 sm:px-4">
      <p class="text-sm text-gray-600 bg-gray-100 p-3 rounded-md mb-4">
        <i class="bi bi-info-circle-fill mr-1.5"></i>이 숙소는 최대 {{ maxBookingDays }}박까지 예약할 수 있어요.
      </p>
      
      <el-calendar v-model="internalCalendarDate">
        <template #header="{ date }">
          <div class="flex justify-between items-center w-full">
            <el-button link @click="prevMonth" class="text-pink-600 hover:text-pink-700">&lt; 이전 달</el-button>
            <span class="text-lg font-semibold text-gray-700">{{ date }}</span>
            <el-button link @click="nextMonth" class="text-pink-600 hover:text-pink-700">다음 달 &gt;</el-button>
          </div>
        </template>
        <template #date-cell="{ data }">
          <div class="date-cell-content w-full h-full flex flex-col items-center justify-center" 
               :class="{ 'is-selected': isDateSelected(data.day), 'is-disabled': isDateDisabled(data.day) }"
               @click="handleDateClick(data.day)">
            <p class="day-number" :class="{'text-red-500': [0,6].includes(new Date(data.day).getDay())}">
              {{ data.day.split('-').slice(2).join('') }}
            </p>
            <!-- 가격 표시는 필요 시 추가 -->
            <!-- <p v-if="getDatePrice(data.day)" class="text-xs text-gray-500 mt-1">{{ getDatePrice(data.day) }}</p> -->
          </div>
        </template>
      </el-calendar>
      <!-- <p class="text-xs text-gray-500 mt-4 text-center">가격: 1박 기준 (단위: 만원)</p> -->
    </div>
    <template #footer>
      <div class="flex justify-between items-center w-full px-2 sm:px-4 pb-2">
        <el-button @click="resetDateSelection" link class="text-gray-600 hover:text-pink-500">초기화</el-button>
        <el-button 
          type="primary" 
          @click="applyDateSelection" 
          class="bg-pink-500 hover:bg-pink-600 border-pink-500 flex-grow sm:flex-grow-0 min-w-[150px] py-2.5 text-base font-semibold rounded-md"
          :disabled="!tempSelectedCheckInDate || !tempSelectedCheckOutDate"
        >
          {{ selectedRangeFooterDisplayComputed }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue';
import { ElCalendar, ElButton, ElDialog } from 'element-plus'; // ElDialog 추가

const KOREAN_DAYS = ['일', '월', '화', '수', '목', '금', '토'];

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

const emit = defineEmits(['close', 'apply', 'update:show']); // 'update:show' 추가

const internalCalendarDate = ref(new Date());
const tempSelectedCheckInDate = ref<Date | null>(null);
const tempSelectedCheckOutDate = ref<Date | null>(null);

// `props.show`를 직접 `v-model`로 사용하기보다는,
// 부모로부터 받은 `show` prop을 기반으로 내부 상태를 관리하고,
// 모달을 닫을 때 `emit('update:show', false)` 또는 `emit('close')`를 호출합니다.
// 여기서는 `@update:model-value`를 사용하여 `props.show`를 업데이트하는 방식을 사용합니다.
function handleClose() {
  emit('update:show', false); // FilterHeader.vue에서 v-model:show로 받을 수 있도록
  emit('close'); // 기존 close 이벤트도 유지
}


watch(() => props.show, (newVal) => {
  if (newVal) {
    // 모달이 열릴 때 초기 날짜 설정 로직
    if (props.initialDateRange && props.initialDateRange[0]) {
      const initialStart = new Date(props.initialDateRange[0]);
      initialStart.setHours(0,0,0,0);
      tempSelectedCheckInDate.value = initialStart;
      internalCalendarDate.value = new Date(initialStart);
      
      if (props.initialDateRange[1]) {
        const initialEnd = new Date(props.initialDateRange[1]);
        initialEnd.setHours(0,0,0,0);
        tempSelectedCheckOutDate.value = initialEnd;
      } else {
        tempSelectedCheckOutDate.value = null;
      }
    } else {
      // 초기 선택된 날짜가 없으면, 선택 상태는 비우고 달력은 오늘 날짜로
      tempSelectedCheckInDate.value = null;
      tempSelectedCheckOutDate.value = null;
      const today = new Date();
      today.setHours(0,0,0,0);
      internalCalendarDate.value = new Date(today);
    }
  }
}, { immediate: true }); // immediate: true 로 초기 마운트 시에도 실행


function calculateNights(startDate: Date | null, endDate: Date | null): number {
  if (!startDate || !endDate) return 0;
  // 동일 날짜 선택 시 0박이 아닌 1일로 간주될 수 있으므로, 명시적으로 0박 처리 또는 정책에 맞게 조정
  if (startDate.getTime() === endDate.getTime()) return 0; // 또는 1로 할수도 있음 (정책따라)
  const diffTime = Math.abs(endDate.getTime() - startDate.getTime());
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays > 0 ? diffDays : 0;
}

function formatDateWithDay(date: Date | null): string {
  if (!date) return "";
  const month = (date.getMonth() + 1);
  const day = date.getDate();
  const dayOfWeek = KOREAN_DAYS[date.getDay()];
  return `${month}.${day}(${dayOfWeek})`;
}

const selectedRangeFooterDisplayComputed = computed(() => {
  if (!tempSelectedCheckInDate.value) return '날짜를 선택해주세요';
  if (!tempSelectedCheckOutDate.value) return `${formatDateWithDay(tempSelectedCheckInDate.value)} 선택됨`;
  const nights = calculateNights(tempSelectedCheckInDate.value, tempSelectedCheckOutDate.value);
  // AccommodationDetail.vue와 유사하게 "지금부터" 대신 실제 시작 날짜 표시
  return `${formatDateWithDay(tempSelectedCheckInDate.value)} ~ ${formatDateWithDay(tempSelectedCheckOutDate.value)} • ${nights}박`;
});

function prevMonth() {
  const currentDate = new Date(internalCalendarDate.value);
  currentDate.setMonth(currentDate.getMonth() - 1);
  internalCalendarDate.value = currentDate;
}

function nextMonth() {
  const currentDate = new Date(internalCalendarDate.value);
  currentDate.setMonth(currentDate.getMonth() + 1);
  internalCalendarDate.value = currentDate;
}

function isDateSelected(dateStr: string): boolean {
  const date = new Date(dateStr);
  date.setHours(0, 0, 0, 0);
  
  if (tempSelectedCheckInDate.value && tempSelectedCheckOutDate.value) {
    const checkIn = new Date(tempSelectedCheckInDate.value);
    // checkIn.setHours(0,0,0,0); // 이미 위에서 처리됨
    const checkOut = new Date(tempSelectedCheckOutDate.value);
    // checkOut.setHours(0,0,0,0); // 이미 위에서 처리됨
    return date >= checkIn && date <= checkOut;
  }
  if (tempSelectedCheckInDate.value) {
     const checkIn = new Date(tempSelectedCheckInDate.value);
     // checkIn.setHours(0,0,0,0);
     return date.getTime() === checkIn.getTime();
  }
  return false;
}

function isDateDisabled(dateStr: string): boolean {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const targetDate = new Date(dateStr);
  targetDate.setHours(0,0,0,0);
  return targetDate < today;
}

function handleDateClick(dateStr: string) {
  const clickedDate = new Date(dateStr);
  clickedDate.setHours(0,0,0,0);

  if (isDateDisabled(dateStr)) return;

  if (!tempSelectedCheckInDate.value || (tempSelectedCheckInDate.value && tempSelectedCheckOutDate.value)) {
    // 1. 아무것도 선택되지 않았거나, 체크인/체크아웃 모두 선택된 후 다시 선택하는 경우
    tempSelectedCheckInDate.value = clickedDate;
    tempSelectedCheckOutDate.value = null;
  } else if (clickedDate < tempSelectedCheckInDate.value) {
    // 2. 선택된 체크인 날짜보다 이전 날짜를 클릭한 경우
    tempSelectedCheckInDate.value = clickedDate;
    tempSelectedCheckOutDate.value = null; 
  } else if (clickedDate.getTime() === tempSelectedCheckInDate.value.getTime()) {
    // 3. 선택된 체크인 날짜와 동일한 날짜를 클릭한 경우 (체크인 취소 또는 아무것도 안함 - 여기선 초기화)
    tempSelectedCheckInDate.value = null;
    tempSelectedCheckOutDate.value = null;
  } else {
    // 4. 체크인 날짜 이후를 클릭 (체크아웃 날짜 선택)
    const nights = calculateNights(tempSelectedCheckInDate.value, clickedDate);
    if (nights > props.maxBookingDays) {
      // alert(`최대 ${props.maxBookingDays}박까지 선택 가능합니다.`);
      // TODO: Toast 메시지 등으로 사용자에게 알림
      // 최대 숙박일 초과 시, 체크아웃 날짜를 최대 가능일로 설정하거나,
      // 또는 AccommodationDetail.vue처럼 체크아웃 날짜를 제한된 날짜로 자동 설정.
      // 여기서는 체크아웃을 자동으로 최대일로 설정하는 대신, 선택을 무효화하거나 사용자에게 알림 후 새 체크인으로.
      tempSelectedCheckInDate.value = clickedDate; // 새 날짜를 체크인으로
      tempSelectedCheckOutDate.value = null;
    } else {
      tempSelectedCheckOutDate.value = clickedDate;
    }
  }
}

function resetDateSelection() {
  tempSelectedCheckInDate.value = null;
  tempSelectedCheckOutDate.value = null;
  const today = new Date();
  today.setHours(0,0,0,0);
  internalCalendarDate.value = new Date(today);
}

function applyDateSelection() {
  if (tempSelectedCheckInDate.value && tempSelectedCheckOutDate.value) {
    if (tempSelectedCheckInDate.value > tempSelectedCheckOutDate.value) {
       // alert('체크아웃 날짜는 체크인 날짜보다 이후여야 합니다.');
       // TODO: 사용자에게 알림
       return;
    }
    emit('apply', [new Date(tempSelectedCheckInDate.value), new Date(tempSelectedCheckOutDate.value)]);
    handleClose(); // emit('update:show', false) 및 emit('close') 호출
  } else {
    // alert('체크인 및 체크아웃 날짜를 모두 선택해주세요.');
    // TODO: 사용자에게 알림
  }
}

// onMounted는 watch의 immediate:true로 대체 가능하거나,
// props.show가 true일 때 초기화 로직을 한 번 더 확인하는 용도로 남겨둘 수 있습니다.
onMounted(() => {
  if (props.show) {
    // 이미 watch에서 처리하므로 중복될 수 있으나, 안전장치로 둘 수 있음.
    // console.log("DateSelectModal mounted and props.show is true, re-initializing dates if needed.");
  }
});

</script>

<style scoped>
/* 기존 스타일 유지 및 AccommodationDetail.vue 스타일 참고하여 추가 */
.date-cell-content {
  cursor: pointer;
  border-radius: 4px;
  width: 100%; 
  height: 100%; 
  display: flex; 
  flex-direction: column; 
  align-items: center; 
  justify-content: center; 
  /* padding-top: 4px; */ /* el-calendar 셀 자체 패딩 고려 */
  /* padding-bottom: 4px; */
}

.date-cell-content.is-selected {
  background-color: #fde2ec; /* pink-100 */
  color: #c12860; /* pink-700 */
  font-weight: bold;
}

.date-cell-content.is-disabled {
  color: #c0c4cc; /* gray-400 */
  cursor: not-allowed;
  background-color: #f5f7fa; /* gray-100 or lighter */
}
.date-cell-content.is-disabled .day-number {
  color: #a8abb2; /* gray-500 */
}

.date-cell-content:not(.is-disabled):hover {
  background-color: #fef7f9; /* pink-50 */
}

.day-number {
  font-size: 0.875rem; /* text-sm */
}
.day-number.text-red-500 { /* 주말 빨간색 표시 */
 color: #ef4444;
}

/* ElDialog 커스텀 스타일 (AccommodationDetail.vue 참고) */
/* :global 키워드를 사용하거나, scoped 없이 별도 CSS 파일로 분리 가능 */
/* 여기서는 custom-class를 사용하므로 해당 클래스 대상으로 직접 스타일링 */

</style>
<style>
/* Global-like styles for el-dialog via custom-class */
/*
.date-selection-dialog .el-dialog__header {
  display: none; 
}
*/
.date-selection-dialog .el-dialog__body {
  padding: 16px 8px 0px 8px; /* 상하좌우 패딩 조정, AccommodationDetail과 유사하게 */
}
@media (min-width: 640px) { /* sm breakpoint */
  .date-selection-dialog .el-dialog__body {
    padding: 20px 16px 0px 16px;
  }
}
.date-selection-dialog .el-dialog__footer {
  padding: 8px 8px 12px 8px;
}
@media (min-width: 640px) { /* sm breakpoint */
  .date-selection-dialog .el-dialog__footer {
    padding: 8px 16px 12px 16px;
  }
}
</style> 