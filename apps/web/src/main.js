import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';

// 앱 인스턴스 생성
const app = createApp(App);

// Pinia 상태 관리 스토어 사용
app.use(createPinia());

// Vue Router 사용
app.use(router);

// 앱 마운트
app.mount('#app');