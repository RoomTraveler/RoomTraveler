<template>
  <div
    class="flex flex-row items-stretch max-w-[768px] mx-auto bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden gap-x-0 pl-4"
    style="min-height: 198px"
    :class="{ 'disabled-item': !isBookable }"
  >
    <RoomCard :room="room" @view-detail="emitViewDetail" />
    <div class="border-l border-gray-200 flex flex-col flex-1">
      <RoomInfoDisplay
        :room-info="room"
        @view-detail="emitViewDetail"
        @book-room="emitBookRoom"
        @add-to-cart="emitAddToCart"
        class="h-full"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { PropType } from "vue";
import RoomCard from "./RoomCard.vue";
import RoomInfoDisplay from "./RoomInfoDisplay.vue";

// Define the Room interface based on what RoomCard and RoomInfoDisplay expect.
// This should be consistent with the Room interface used in AccommodationDetail.
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
  // Add any other fields if necessary based on props of RoomCard/RoomInfoDisplay
}

const props = defineProps({
  room: {
    type: Object as PropType<Room>,
    required: true,
  },
  isBookable: {
    type: Boolean,
    default: true, // 기본적으로 예약 가능하다고 가정
  },
});

const emit = defineEmits(["view-detail", "book-room", "add-to-cart"]);

const emitViewDetail = () => {
  if (!props.isBookable) {
    // 선택적으로 사용자에게 알림 (예: 부모 컴포넌트에서 토스트 메시지)
    console.log("RoomListItem: View detail for non-bookable room blocked.");
    return;
  }
  emit("view-detail", props.room.roomId);
};

const emitBookRoom = () => {
  if (!props.isBookable) {
    console.log("RoomListItem: Book room for non-bookable room blocked.");
    return;
  }
  emit("book-room", props.room.roomId);
};

const emitAddToCart = () => {
  if (!props.isBookable) {
    console.log("RoomListItem: Add to cart for non-bookable room blocked.");
    return;
  }
  // emit("add-to-cart", props.room.roomId); // RoomInfoDisplay에서 room 객체 전체를 emit하므로 여기서는 주석 처리
  // AccommodationDetail.vue에서 room 객체 전체를 받도록 수정되었으므로, RoomListItem도 room 객체 전체를 emit하도록 통일합니다.
  emit("add-to-cart", props.room);
};
</script>

<style scoped>
.disabled-item {
  opacity: 0.6;
  cursor: not-allowed;
  pointer-events: none; /* 하위 요소 클릭도 막음 */
}
.disabled-item :deep(button) {
  /* pointer-events: none; 이미 상위에서 처리 */
}
.disabled-item :deep(a) {
  /* pointer-events: none; 이미 상위에서 처리 */
}
</style>
