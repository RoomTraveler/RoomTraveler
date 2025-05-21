<template>
  <div
    class="bg-white flex flex-col flex-shrink-0 overflow-hidden rounded-l-lg"
    style="width: 320px; min-height: 198px"
  >
    <!-- 왼쪽 영역: 이미지 캐러셀 + 이미지 하단 객실 기본 정보 -->
    <div class="flex flex-col w-full flex-shrink-0">
      <!-- 이미지 캐러셀 -->
      <div class="w-full h-[160px] bg-gray-200 relative mb-4">
        <div v-if="roomImages.length > 0" class="w-full h-full">
          <img
            :src="roomImages[currentImageIndex]"
            :alt="`${room.name || '객실'} 이미지 ${currentImageIndex + 1}`"
            class="w-full h-full object-cover"
          />
          <button
            v-if="roomImages.length > 1"
            @click.stop="prevImage"
            class="absolute top-1/2 left-2 transform -translate-y-1/2 bg-black/40 text-white p-1 rounded-full hover:bg-black/60 focus:outline-none transition-colors"
            aria-label="이전 이미지"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              fill="currentColor"
              class="bi bi-chevron-left"
              viewBox="0 0 16 16"
            >
              <path
                fill-rule="evenodd"
                d="M11.354 1.646a.5.5 0 0 1 0 .708L5.707 8l5.647 5.646a.5.5 0 0 1-.708.708l-6-6a.5.5 0 0 1 0-.708l6-6a.5.5 0 0 1 .708 0z"
              />
            </svg>
          </button>
          <button
            v-if="roomImages.length > 1"
            @click.stop="nextImage"
            class="absolute top-1/2 right-2 transform -translate-y-1/2 bg-black/40 text-white p-1 rounded-full hover:bg-black/60 focus:outline-none transition-colors"
            aria-label="다음 이미지"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              fill="currentColor"
              class="bi bi-chevron-right"
              viewBox="0 0 16 16"
            >
              <path
                fill-rule="evenodd"
                d="M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708z"
              />
            </svg>
          </button>
          <div
            v-if="roomImages.length > 1"
            class="absolute bottom-2 left-1/2 transform -translate-x-1/2 flex space-x-1.5"
          >
            <span
              v-for="(_, index) in roomImages"
              :key="index"
              @click.stop="currentImageIndex = index"
              class="w-2 h-2 rounded-full cursor-pointer transition-colors"
              :class="currentImageIndex === index ? 'bg-white' : 'bg-gray-400/70 hover:bg-gray-300/90'"
            ></span>
          </div>
        </div>
        <div v-else class="w-full h-full flex items-center justify-center bg-gray-100">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="48"
            height="48"
            fill="currentColor"
            class="bi bi-image-alt text-gray-400"
            viewBox="0 0 16 16"
          >
            <path
              d="M7 2.5a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0zm4.225 4.053a.5.5 0 0 0-.577.093l-3.71 4.71-2.66-2.772a.5.5 0 0 0-.63.062L.002 13.5v-11a.5.5 0 0 1 .5-.5h14a.5.5 0 0 1 .5.5v11l-3.428-3.396a.5.5 0 0 0-.63-.062l-2.66 2.772-3.71-4.71a.5.5 0 0 0-.577-.093z"
            />
          </svg>
        </div>
      </div>
      <!-- 객실 기본 정보 (이미지 하단) -->
      <div class="px-4 py-3">
        <h3 class="text-lg font-bold text-gray-800 mb-0.5 truncate" :title="room.name || '객실명 없음'">
          {{ room.name || "체크인 시 배정" }}
        </h3>
        <p class="text-xs text-gray-500 mb-0.5 truncate" :title="room.packageName || '패키지 정보 없음'">
          {{ room.packageName || "패키지명 없음" }}
        </p>
        <p class="text-xs text-gray-500 flex items-center mb-0.5">
          <i class="bi bi-people mr-1 text-gray-400"></i>
          기준 {{ room.defaultCapacity || 2 }}인 / 최대 {{ room.capacity || 2 }}인
        </p>
        <p class="text-xs text-gray-500 flex items-center truncate" :title="room.roomFeature || '객실 특징 없음'">
          <i class="bi bi-door-closed mr-1 text-gray-400"></i>
          {{ room.roomFeature || "객실 특징 없음" }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, PropType } from "vue";
// import RoomInfoDisplay from "./RoomInfoDisplay.vue"; // RoomInfoDisplay 제거
import defaultNoImage from "@/assets/no-image.jpg";

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
  checkInTime?: string; // RoomInfoDisplay 로 이동될 정보지만, Card 자체 표시용으로 남길 수 있음
  checkOutTime?: string; // RoomInfoDisplay 로 이동될 정보지만, Card 자체 표시용으로 남길 수 있음
  mainImageUrl?: string;
  imageUrls?: string[];
}

const props = defineProps({
  room: {
    type: Object as PropType<Room>,
    required: true,
  },
  isBookable: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(["view-detail", "book-room", "add-to-cart"]);

const currentImageIndex = ref(0);

const roomImages = computed(() => {
  const images: string[] = [];
  if (props.room.mainImageUrl) {
    images.push(props.room.mainImageUrl);
  }
  if (props.room.imageUrls && props.room.imageUrls.length > 0) {
    props.room.imageUrls.forEach((url) => {
      if (url !== props.room.mainImageUrl) {
        images.push(url);
      }
    });
  }
  return images.length > 0 ? images : [defaultNoImage];
});

const prevImage = () => {
  if (roomImages.value.length <= 1) return;
  currentImageIndex.value = (currentImageIndex.value - 1 + roomImages.value.length) % roomImages.value.length;
};

const nextImage = () => {
  if (roomImages.value.length <= 1) return;
  currentImageIndex.value = (currentImageIndex.value + 1) % roomImages.value.length;
};

const roomInfoForDisplay = computed(() => ({
  ...props.room,
  // minAvailableCount: props.room.minAvailableCount || props.room.stock, // 예시: API 응답에 따라 조정
  // 만약 room 객체에 minAvailableCount가 이미 포함되어 있다면, 이 부분은 필요 없을 수 있습니다.
  // RoomListItem에서 전달되는 room 객체의 구조를 확인해야 합니다.
  // 현재는 room 객체에 minAvailableCount가 있다고 가정하고 별도 매핑 안함.
}));

const handleViewDetail = (roomId: number) => {
  // RoomCard에서의 상세보기는 isBookable과 관계없이 항상 가능 (상세 정보 확인 목적)
  emit("view-detail", roomId);
};

const handleBookRoom = (roomId: number) => {
  if (!props.isBookable) return; // isBookable 체크
  emit("book-room", roomId);
};

const handleAddToCart = (room: any) => {
  // roomInfo 전체를 받도록 수정
  if (!props.isBookable) return; // isBookable 체크
  emit("add-to-cart", room);
};
</script>

<style scoped>
.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
/* Bootstrap Icons CDN은 이미 index.html 또는 main.ts 등에 추가되어 있다고 가정합니다. */
/* 만약 그렇지 않다면, i 태그 대신 SVG 아이콘을 직접 사용하거나 CSS import가 필요합니다. */
</style>
