<!-- 
  App.vue - 방구석 여행자 프론트엔드 애플리케이션의 루트 컴포넌트

  이 파일은 애플리케이션의 기본 레이아웃을 정의합니다.
  헤더, 메인 콘텐츠 영역, 푸터로 구성되어 있으며,
  라우터를 통해 다양한 페이지 컴포넌트를 표시합니다.
-->
<template>
  <div id="app">
    <!-- 라우터 뷰 - 현재 경로에 맞는 컴포넌트가 렌더링됩니다 -->
    <router-view />
  </div>
</template>

<script>
/**
 * 앱의 루트 컴포넌트
 * 
 * 이 컴포넌트는 애플리케이션의 기본 구조를 정의하며,
 * 모든 페이지에 공통적으로 적용되는 레이아웃을 제공합니다.
 */
export default {
  name: 'App'
}
</script>

<style>
:root {
  --yanolja-red: #f0213b;
  --yanolja-pink: #ff3478;
  --yanolja-light-gray: #f5f5f5;
  --yanolja-dark-gray: #666;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Noto Sans KR', Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #333;
  background-color: #f9f9f9;
  line-height: 1.5;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Bootstrap Icons 추가 */
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css");

/* 모바일 최적화 */
@media (max-width: 768px) {
  html {
    font-size: 14px;
  }
}
</style>
