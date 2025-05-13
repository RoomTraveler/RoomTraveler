<template>
  <div id="app">
    <header>
      <nav>
        <router-link to="/">홈</router-link> |
        <router-link to="/accommodation">숙소</router-link> |
        <router-link to="/user">사용자</router-link>
      </nav>
    </header>
    
    <main>
      <!-- 라우터 뷰 - 현재 경로에 맞는 컴포넌트가 렌더링됩니다 -->
      <router-view />
    </main>
    
    <footer>
      <p>&copy; 2023 Room Traveler - 모든 권리 보유</p>
    </footer>
  </div>
</template>

<script>
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