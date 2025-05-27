<template>
  <div
    class="bg-white d-flex flex-column flex-shrink-0 rounded-start"
    style="width: 320px; min-height: 198px; overflow: hidden"
  >
    <!-- 이미지 캐러셀 + 객실 기본정보 -->
    <div class="d-flex flex-column w-100 flex-shrink-0">
      <!-- 이미지 캐러셀 -->
      <div class="position-relative w-100 bg-light mb-3" style="height: 160px">
        <swiper
          v-if="roomImages.length > 0 && roomImages[0] !== defaultNoImage"
          :modules="swiperModules"
          :slides-per-view="1"
          :space-between="0"
          navigation
          :pagination="{ clickable: true, el: '.swiper-pagination-custom' }"
          loop
          class="h-100 w-100"
          @swiper="onSwiper"
        >
          <swiper-slide v-for="(imgUrl, index) in roomImages" :key="index">
            <img
              :src="imgUrl"
              :alt="`${room.name || '객실'} 이미지 ${index + 1}`"
              class="w-100 h-100 object-fit-cover rounded-top"
              style="min-height: 160px; max-height: 160px"
            />
          </swiper-slide>
          <!-- Navigation buttons and pagination will be handled by Swiper if configured -->
        </swiper>
        <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center bg-secondary bg-opacity-10">
          <img
            :src="defaultNoImage"
            alt="이미지 없음"
            class="w-100 h-100 object-fit-cover rounded-top"
            style="min-height: 160px; max-height: 160px"
          />
        </div>
        <!-- Custom Pagination (optional, if default Swiper pagination is not enough) -->
        <div
          class="swiper-pagination-custom position-absolute bottom-0 start-50 translate-middle-x d-flex gap-1 pb-2"
          style="z-index: 2"
        ></div>
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
        <p
          class="small text-secondary d-flex align-items-center truncate mb-0"
          :title="room.roomFeature || '객실 특징 없음'"
        >
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
import { Swiper, SwiperSlide } from "swiper/vue";
import type { Swiper as SwiperClass } from "swiper/types";
import { Navigation, Pagination, Autoplay } from "swiper/modules";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/pagination";

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

const swiperInstance = ref<SwiperClass | null>(null);
const swiperModules = [Navigation, Pagination, Autoplay];

const onSwiper = (swiper: SwiperClass) => {
  swiperInstance.value = swiper;
};

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
/* Add Swiper specific styles if needed, for example, to customize navigation arrows or pagination dots */
.swiper-pagination-custom .swiper-pagination-bullet {
  width: 8px;
  height: 8px;
  background-color: rgba(128, 128, 128, 0.4);
  border-radius: 50%;
  display: inline-block;
  cursor: pointer;
  margin: 0 3px; /* 점 사이 간격 */
}
.swiper-pagination-custom .swiper-pagination-bullet-active {
  background-color: #fff;
  border: 1.5px solid #dc3545;
}
</style>
