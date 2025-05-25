<template>
  <el-card class="summary-card text-center border-0 shadow-sm mb-3">
    <div class="card-body p-3">
      <div :class="['icon', 'mb-2', 'text-' + color]">
        <i :class="['bi', icon, 'display-6']"></i>
      </div>
      <div class="h5 fw-bold mb-1">{{ title }}</div>
      <div class="display-6 fw-bolder">{{ value }}</div>
    </div>
  </el-card>
</template>

<script setup>
import { ElCard } from 'element-plus';
import 'bootstrap-icons/font/bootstrap-icons.css'; // Bootstrap Icons CSS 추가

defineProps({
  title: String,
  value: [String, Number],
  icon: String,
  color: String
});
</script>

<style scoped>
.summary-card .icon {
  font-size: 2.5rem; /* 아이콘 크기 조정 */
}
.summary-card .card-body {
  padding: 1.25rem; /* 내부 패딩 조정 */
}
.summary-card .h5 {
  font-size: 1rem; /* 제목 폰트 크기 조정 */
}
.summary-card .display-6 {
  font-size: 1.75rem; /* 값 폰트 크기 조정 */
}
</style> 