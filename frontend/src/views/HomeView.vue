<template>
  <div class="home-container" :class="slideClass">
    <div class="half left" @click="slideTo('left')">
      <div class="modern-signboard">
        <span class="modern-text">숙소 예약하기</span>
      </div>
    </div>
    <div class="half right" @click="slideTo('right')">
      <div class="content">
        <h2>여행 계획 세우기</h2>
      </div>
    </div>
    <div class="glow"></div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
const router = useRouter();

const slide = ref(""); // "", "slide-left", "slide-right"
const slideClass = computed(() => (slide.value ? `sliding ${slide.value}` : ""));

function slideTo(direction) {
  slide.value = direction === "left" ? "slide-left" : "slide-right";
  setTimeout(() => {
    router.push(direction === "left" ? "/accommodation" : "/plan");
  }, 700);
}
</script>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css");

.home-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  position: relative;
  background: linear-gradient(120deg, #f7fafc 0%, #e3e9f3 100%);
  box-shadow: 0 6px 36px 12px rgba(40, 80, 140, 0.08);
  transition: box-shadow 0.6s cubic-bezier(0.77, 0, 0.18, 1);
}

.glow {
  position: absolute;
  left: 50%;
  top: 0;
  width: 80vw;
  height: 70vh;
  background: radial-gradient(ellipse at center, #e0e7ff33 0%, #fff0 80%);
  transform: translateX(-50%);
  filter: blur(20px);
  z-index: 0;
  pointer-events: none;
}

.half {
  position: relative;
  flex: 1;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  transition:
    transform 0.7s cubic-bezier(0.77, 0, 0.18, 1),
    opacity 0.7s;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.07);
  z-index: 1;
  box-shadow: 0 2px 24px 0 rgba(60, 90, 150, 0.06);
}

.half.left:hover {
  transform: scale(1.02) translateX(2%);
  z-index: 2;
}

.half.right:hover {
  transform: scale(1.02) translateX(-2%);
  z-index: 2;
}

/* ---- 모던 숙소 예약하기 버튼 ---- */
.modern-signboard {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 230px;
  min-height: 56px;
  padding: 0.8rem 2.2rem;
  background: rgba(255, 255, 255, 0.82);
  border-radius: 1.5rem;
  box-shadow:
    0 2px 12px 2px #ffb4e522,
    0 0 14px 2px #ffe3f755;
  border: 1.5px solid #ffb5d8;
  z-index: 2;
  transition:
    box-shadow 0.18s,
    background 0.18s;
  /* 버튼 느낌, 네온X */
  cursor: pointer;
  user-select: none;
}

.modern-signboard:hover {
  background: #fff8fd;
  box-shadow:
    0 3px 22px 8px #ffb4e577,
    0 0 18px 3px #ffe3f799;
  border-color: #ff84c9;
}

.modern-text {
  font-family: "Pretendard", "Segoe UI", "Noto Sans KR", sans-serif;
  font-size: 1.38rem;
  color: #ea4b90;
  font-weight: 700;
  letter-spacing: 1.2px;
  text-align: center;
  padding: 0 4px;
  /* 연한 glow */
  text-shadow:
    0 2px 8px #ffd4f333,
    0 0 16px #fff5;
  transition:
    color 0.18s,
    text-shadow 0.18s;
}

/* ---- 여행 계획 세우기 기존 디자인 ---- */
.content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: #253055;
  filter: drop-shadow(0 1px 8px #fff8);
}

.content h2 {
  margin-bottom: 1.5rem;
  font-size: 2.3rem;
  font-weight: 700;
  letter-spacing: 1px;
  text-shadow:
    0 2px 16px #fff7,
    0 2px 4px #b5cdfa22;
  transition: color 0.3s;
}

/* 이미지 효과 */
.half.left::before,
.half.right::before {
  content: "";
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  opacity: 1;
  z-index: 1;
  transition: opacity 0.6s cubic-bezier(0.77, 0, 0.18, 1);
  pointer-events: none;
}
.half.left::before {
  background-image: url("../../public/img/AccommodationIntroImage.png");
  filter: blur(0.2px) grayscale(4%);
}
.half.right::before {
  background-image: url("../../public/img/planIntroImage.png");
  filter: blur(0.2px) grayscale(2%);
}

/* --- SLIDING ANIMATION (기존 유지) --- */
.sliding .half.left {
}
.sliding .half.right {
}

.slide-left .half.left {
  transform: translateX(100vw) scale(1.05) rotateY(18deg);
  opacity: 0;
}
.slide-left .half.right {
  opacity: 0;
  pointer-events: none;
}
.slide-right .half.right {
  transform: translateX(-100vw) scale(1.05) rotateY(-18deg);
  opacity: 0;
}
.slide-right .half.left {
  opacity: 0;
  pointer-events: none;
}

/* (모바일 대응) */
@media (max-width: 800px) {
  .home-container {
    flex-direction: column;
    min-height: 100dvh;
    height: auto;
  }
  .half {
    min-height: 45vh;
  }
  .glow {
    height: 35vh;
  }
  .modern-signboard {
    min-width: 120px;
    min-height: 32px;
    font-size: 0.9rem;
    padding: 0.4rem 0.8rem;
    border-radius: 0.9rem;
  }
  .modern-text {
    font-size: 0.83rem;
    padding: 0 2px;
  }
}
</style>
