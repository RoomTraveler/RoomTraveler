<template>
  <div class="not-found">
    <div class="not-found-content">
      <h1>404</h1>
      <h2>페이지를 찾을 수 없습니다</h2>
      <p>요청하신 페이지가 존재하지 않거나 이동되었을 수 있습니다.</p>
      <div class="actions">
        <router-link to="/" class="home-button">홈으로 이동</router-link>
        <button @click="goBack" class="back-button">이전 페이지로</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound',
  methods: {
    goBack() {
      this.$router.go(-1);
    }
  }
};
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  text-align: center;
  padding: 2rem;
}

.not-found-content {
  max-width: 600px;
}

h1 {
  font-size: 8rem;
  margin: 0;
  color: #42b983;
  line-height: 1;
}

h2 {
  font-size: 2rem;
  margin: 1rem 0;
  color: #333;
}

p {
  font-size: 1.2rem;
  color: #666;
  margin-bottom: 2rem;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.home-button,
.back-button {
  padding: 12px 24px;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.home-button {
  background-color: #42b983;
  color: white;
  text-decoration: none;
}

.home-button:hover {
  background-color: #3aa876;
}

.back-button {
  background-color: #f8f9fa;
  color: #333;
  border: 1px solid #ddd;
}

.back-button:hover {
  background-color: #e9ecef;
}
</style>