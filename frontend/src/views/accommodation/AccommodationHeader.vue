<template>
  <header class="yanolja-header">
    <div class="top-header">
      <div class="container">
        <div class="top-header-content">
          <!-- 왼쪽: 뒤로가기 -->
          <div class="left-area">
            <button @click="goBack" class="back-btn" title="뒤로가기">
              <i class="bi bi-arrow-left-short"></i>
            </button>
          </div>
          <!-- 중앙: 타이틀 -->
          <div class="center-area">
            <span class="header-title">{{ title }}</span>
          </div>
          <!-- 오른쪽: 홈, 장바구니 -->
          <div class="user-menu">
            <button @click="goToHome" class="user-menu-item icon-btn" title="홈">
              <i class="bi bi-house"></i>
            </button>
            <button @click="goToCart" class="user-menu-item icon-btn" title="장바구니">
              <i class="bi bi-cart3"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
const props = defineProps<{ title: string }>();

const router = useRouter();

function goBack() {
  if (
    window.history.length > 1 &&
    document.referrer &&
    new URL(document.referrer).hostname === window.location.hostname
  ) {
    router.back();
  } else {
    router.push({ name: "Home" });
  }
}
function goToHome() {
  router.push({ name: "Home" });
}
function goToCart() {
  router.push({ name: "Cart" });
}
</script>

<style scoped>
:root {
  --yanolja-red: #f0213b;
}

.yanolja-header {
  width: 100%;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}
.top-header {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}
.top-header .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
}
.top-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 44px;
  position: relative;
}
.left-area {
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}
.back-btn {
  background: none;
  border: none;
  font-size: 30px;
  color: var(--yanolja-red);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 8px;
  border-radius: 50%;
  transition: background 0.15s;
}
.back-btn:hover {
  background: #f9f2f5;
}
.center-area {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-width: 0;
}
.header-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}
.user-menu {
  flex: 0 0 90px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}
.user-menu-item {
  margin-left: 8px;
  color: #333;
  background: none;
  border: none;
  text-decoration: none;
  font-size: 22px;
  cursor: pointer;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition:
    background 0.15s,
    color 0.15s;
}
.user-menu-item:hover {
  color: var(--yanolja-red);
  background: #f9f2f5;
}
.icon-btn {
  font-size: 22px;
}

@media (max-width: 768px) {
  .top-header-content {
    min-height: 44px;
    gap: 0;
  }
  .left-area,
  .user-menu {
    flex: 0 0 44px;
  }
  .center-area .header-title {
    font-size: 18px;
  }
}
</style>
