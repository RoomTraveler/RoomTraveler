<template>
  <div
    class="flex flex-row items-stretch max-w-[768px] mx-auto bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden gap-x-0 pl-4"
    style="min-height: 198px"
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
});

const emit = defineEmits(["view-detail", "book-room", "add-to-cart"]);

const emitViewDetail = () => {
  // RoomCard와 RoomInfoDisplay 모두 roomId를 직접 emit하지 않으므로,
  // 여기서는 props.room.roomId를 사용합니다.
  // 만약 하위 컴포넌트가 roomId를 payload로 emit한다면, 해당 payload를 그대로 전달합니다.
  // 현재 RoomCard는 @view-detail 시 roomId를 emit하지 않고, RoomInfoDisplay는 합니다.
  // 일관성을 위해 RoomListItem에서 roomId를 emit하도록 통일합니다.
  emit("view-detail", props.room.roomId);
};

const emitBookRoom = () => {
  emit("book-room", props.room.roomId);
};

const emitAddToCart = () => {
  emit("add-to-cart", props.room.roomId);
};
</script>

<style scoped>
/* 필요한 경우 여기에 스타일 추가 */
</style>
