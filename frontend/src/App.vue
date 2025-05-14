<!-- 
  App.vue - 방구석 여행자 프론트엔드 애플리케이션의 루트 컴포넌트

  이 파일은 애플리케이션의 기본 레이아웃을 정의합니다.
  헤더, 메인 콘텐츠 영역, 푸터로 구성되어 있으며,
  라우터를 통해 다양한 페이지 컴포넌트를 표시합니다.
-->
<template>
  <div id="app">
    <!-- 헤더 섹션: 네비게이션 메뉴를 포함합니다 -->
    <header>
      <nav>
        <router-link to="/">홈</router-link> |
        <router-link to="/accommodation">숙소</router-link> |
        <router-link to="/user">사용자</router-link>
      </nav>
    </header>

    <!-- 메인 콘텐츠 영역: 현재 라우트에 해당하는 컴포넌트가 표시됩니다 -->
    <main>
      <!-- 라우터 뷰 - 현재 경로에 맞는 컴포넌트가 렌더링됩니다 -->
      <router-view />
    </main>

    <!-- 푸터 섹션: 저작권 정보를 표시합니다 -->
    <footer>
      <p>&copy; 2023 Room Traveler - 모든 권리 보유</p>
    </footer>
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
#app {
  font-family: 'Noto Sans KR', Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

header {
  background-color: #f8f9fa;
  padding: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

nav {
  padding: 1rem 0;
}

nav a {
  font-weight: bold;
  color: #2c3e50;
  text-decoration: none;
  margin: 0 0.5rem;
}

nav a.router-link-exact-active {
  color: #42b983;
}

main {
  flex: 1;
  padding: 2rem;
}

footer {
  background-color: #f8f9fa;
  padding: 1rem;
  border-top: 1px solid #e9ecef;
}
</style>
