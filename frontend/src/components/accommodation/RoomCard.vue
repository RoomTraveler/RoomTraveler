<template>
  <div class="bg-white d-flex flex-column flex-shrink-0 rounded-start" style="width:320px; min-height:198px; overflow:hidden;">
    <!-- 이미지 캐러셀 + 객실 기본정보 -->
    <div class="d-flex flex-column w-100 flex-shrink-0">
      <!-- 이미지 캐러셀 -->
      <div class="position-relative w-100 bg-light mb-3" style="height:160px;">
        <div v-if="roomImages.length > 0" class="h-100 w-100">
          <img
              :src="roomImages[currentImageIndex]"
              :alt="`${room.name || '객실'} 이미지 ${currentImageIndex + 1}`"
              class="w-100 h-100 object-fit-cover rounded-top"
              style="min-height:160px;max-height:160px;"
          />
          <!-- 좌/우 화살표 -->
          <button
              v-if="roomImages.length > 1"
              @click.stop="prevImage"
              class="btn btn-dark btn-sm position-absolute top-50 start-0 translate-middle-y ms-2 opacity-75"
              style="border-radius:50%;padding:2px 7px;z-index:2;"
              aria-label="이전 이미지"
          >
            <i class="bi bi-chevron-left"></i>
          </button>
          <button
              v-if="roomImages.length > 1"
              @click.stop="nextImage"
              class="btn btn-dark btn-sm position-absolute top-50 end-0 translate-middle-y me-2 opacity-75"
              style="border-radius:50%;padding:2px 7px;z-index:2;"
              aria-label="다음 이미지"
          >
            <i class="bi bi-chevron-right"></i>
          </button>
          <!-- 인디케이터(점) -->
          <div
              v-if="roomImages.length > 1"
              class="position-absolute bottom-0 start-50 translate-middle-x d-flex gap-1 pb-2"
              style="z-index:2;"
          >
            <span
                v-for="(_, idx) in roomImages"
                :key="idx"
                @click.stop="currentImageIndex = idx"
                class="rounded-circle"
                :style="{
                width:'8px',height:'8px',background: currentImageIndex===idx?'#fff':'rgba(128,128,128,.4)', border:currentImageIndex===idx?'1.5px solid #dc3545':'none', cursor:'pointer', display:'inline-block'
              }"
            ></span>
          </div>
        </div>
        <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center bg-secondary bg-opacity-10">
          <i class="bi bi-image-alt fs-1 text-secondary opacity-50"></i>
        </div>
      </div>
      <!-- 객실 정보 -->
      <div class="px-3 py-3">
        <h3 class="fs-6 fw-bold text-dark mb-1 truncate" :title="room.name || '객실명 없음'">
          {{ room.name || "체크인 시 배정" }}
        </h3>
        <p class="small text-secondary mb-1 truncate" :title="room.packageName || '패키지 정보 없음'">
          {{ room.packageName || "패키지명 없음" }}
        </p>
        <p class="small text-secondary d-flex align-items-center mb-1">
          <i class="bi bi-people me-1 text-body-tertiary"></i>
          기준 {{ room.defaultCapacity || 2 }}인 / 최대 {{ room.capacity || 2 }}인
        </p>
        <p class="small text-secondary d-flex align-items-center truncate mb-0" :title="room.roomFeature || '객실 특징 없음'">
          <i class="bi bi-door-closed me-1 text-body-tertiary"></i>
          {{ room.roomFeature || "객실 특징 없음" }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, PropType } from "vue";
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
  checkInTime?: string;
  checkOutTime?: string;
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
  if (props.room.mainImageUrl) images.push(props.room.mainImageUrl);
  if (props.room.imageUrls && props.room.imageUrls.length > 0) {
    props.room.imageUrls.forEach((url) => {
      if (url !== props.room.mainImageUrl) images.push(url);
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

const handleViewDetail = (roomId: number) => emit("view-detail", roomId);
const handleBookRoom = (roomId: number) => {
  if (!props.isBookable) return;
  emit("book-room", roomId);
};
const handleAddToCart = (room: Room) => {
  if (!props.isBookable) return;
  emit("add-to-cart", room);
};
</script>

<style scoped>
.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
