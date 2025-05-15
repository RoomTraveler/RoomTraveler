<template>
  <div class="plan-container">
    <div class="container">
      <h1 class="page-title">여행 계획</h1>
      <div class="plan-content">
        <p class="placeholder-text">여행 계획 페이지입니다. 여기에 여행 계획 관련 컨텐츠가 표시됩니다.</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Plan',
  data() {
    return {
      // 데이터 속성들
    }
  },
  mounted() {
    // 컴포넌트가 마운트될 때 실행되는 코드
  },
  methods: {
    // 메소드들
  }
}
</script>

<style scoped>
.plan-container {
  padding: 30px 0;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.plan-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.placeholder-text {
  font-size: 16px;
  color: #666;
  text-align: center;
  margin: 40px 0;
}
</style>