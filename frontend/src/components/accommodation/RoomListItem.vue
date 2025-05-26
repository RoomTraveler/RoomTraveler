<template>
  <div
      class="d-flex flex-row align-items-stretch mx-auto bg-white rounded shadow border overflow-hidden"
      style="max-width:768px; min-height:198px; gap:0; padding-left:1rem;"
      :class="{ 'disabled-item': !isBookable }"
  >
    <!-- 객실 이미지/캐러셀 카드 -->
    <RoomCard :room="room" @view-detail="emitViewDetail" />

    <!-- 객실 정보 표시 영역 -->
    <div class="border-start flex-grow-1 d-flex flex-column h-100">
      <RoomInfoDisplay
          :room-info="room"
          @view-detail="emitViewDetail"
          @book-room="emitBookRoom"
          @add-to-cart="emitAddToCart"
          class="h-100"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { PropType } from "vue";
import RoomCard from "./RoomCard.vue";
import RoomInfoDisplay from "./RoomInfoDisplay.vue";

// RoomCard, RoomInfoDisplay에서 요구하는 Room 타입을 정의
interface Room {
  roomId: number;
  name?: string;
  packageName?: string;
  defaultCapacity?: number;
  capacity?: number | string;
  roomFeature?: string;
  bedType?: string;
  price?: number;
  originalPrice?: number;
  discountRate?: number;
  stock?: number;
  minAvailableCount?: number;
  cancellationPolicy?: string;
  checkInTime?: string;
  checkOutTime?: string;
  mainImageUrl?: string;
  imageUrls?: string[];
  // 필요에 따라 추가 필드 정의
}

// props 정의 (객실 정보, 예약 가능 여부)
const props = defineProps({
  room: {
    type: Object as PropType<Room>,
    required: true,
  },
  isBookable: {
    type: Boolean,
    default: true, // 기본값: 예약 가능
  },
});

// 부모 컴포넌트로 이벤트 전달
const emit = defineEmits(["view-detail", "book-room", "add-to-cart"]);

/**
 * 상세보기 버튼/이벤트를 클릭했을 때 호출
 */
const emitViewDetail = () => {
  if (!props.isBookable) {
    // 예약 불가 시 상세보기 차단 (선택)
    console.log("RoomListItem: 예약 불가 객실 상세보기 차단됨");
    return;
  }
  emit("view-detail", props.room.roomId);
};

/**
 * 예약하기 버튼/이벤트를 클릭했을 때 호출
 */
const emitBookRoom = () => {
  if (!props.isBookable) {
    console.log("RoomListItem: 예약 불가 객실 예약 차단됨");
    return;
  }
  emit("book-room", props.room.roomId);
};

/**
 * 장바구니 담기 버튼/이벤트를 클릭했을 때 호출
 */
const emitAddToCart = () => {
  if (!props.isBookable) {
    console.log("RoomListItem: 예약 불가 객실 장바구니 차단됨");
    return;
  }
  // roomId만 넘기는게 아니라, room 객체 전체를 부모로 전달
  emit("add-to-cart", props.room);
};
</script>

<style scoped>
/* 예약 불가 상태 시 전체 영역 흐림/비활성화 */
.disabled-item {
  opacity: 0.6;
  cursor: not-allowed;
  pointer-events: none; /* 하위 모든 요소 클릭 차단 */
}
</style>
