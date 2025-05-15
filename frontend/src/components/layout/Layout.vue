<template>
  <div class="yanolja-layout">
    <Header />
    <main class="main-content">
      <div class="container">
        <!-- 메인 콘텐츠 영역 -->
        <slot></slot>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script>
/**
 * 레이아웃 컴포넌트
 * 
 * 이 컴포넌트는 웹사이트의 전체 레이아웃을 담당합니다.
 * Header와 Footer 컴포넌트를 포함하며, 메인 콘텐츠는 slot을 통해 주입됩니다.
 */
import Header from './Header.vue'
import Footer from './Footer.vue'

export default {
  name: 'Layout',
  components: {
    Header,
    Footer
  }
}
</script>

<style scoped>
:root {
  --yanolja-red: #f0213b;
  --yanolja-pink: #ff3478;
  --yanolja-light-gray: #f5f5f5;
  --yanolja-dark-gray: #666;
}

.yanolja-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f9f9f9;
}

.main-content {
  flex: 1;
  padding: 20px 0;
}

/* 모바일 최적화 */
@media (max-width: 768px) {
  .main-content {
    padding: 10px 0;
  }
}

/* 컨테이너 오버라이드 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

/* 공통 스타일 */
h1, h2, h3, h4, h5, h6 {
  margin-top: 0;
  font-weight: 700;
}

a {
  text-decoration: none;
  color: inherit;
}

button {
  cursor: pointer;
}

/* 섹션 스타일 */
.section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title .view-all {
  font-size: 14px;
  color: var(--yanolja-red);
}

/* 카드 스타일 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.yanolja-card {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.yanolja-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}

.yanolja-card-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.yanolja-card-content {
  padding: 15px;
}

.yanolja-card-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
  color: #333;
}

.yanolja-card-subtitle {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.yanolja-card-price {
  font-size: 18px;
  font-weight: bold;
  color: var(--yanolja-red);
}

.yanolja-card-price .original {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-right: 5px;
}

.yanolja-card-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background-color: var(--yanolja-red);
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

/* 버튼 스타일 */
.yanolja-btn {
  background-color: var(--yanolja-red);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-weight: bold;
  transition: background-color 0.2s;
}

.yanolja-btn:hover {
  background-color: #d01c33;
}

.yanolja-btn-outline {
  background-color: white;
  color: var(--yanolja-red);
  border: 1px solid var(--yanolja-red);
  padding: 10px 20px;
  border-radius: 4px;
  font-weight: bold;
  transition: all 0.2s;
}

.yanolja-btn-outline:hover {
  background-color: var(--yanolja-red);
  color: white;
}
</style>
