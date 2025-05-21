/**
 * main.js - 방구석 여행자 프론트엔드 애플리케이션의 진입점
 *
 * 이 파일은 Vue 애플리케이션을 초기화하고 필요한 플러그인을 설정합니다.
 * 애플리케이션의 시작점으로, 모든 Vue 관련 설정이 이루어집니다.
 */

// 필요한 라이브러리와 컴포넌트 가져오기
import { createApp } from "vue"; // Vue 3 앱 생성 함수
import { createPinia } from "pinia"; // 상태 관리 라이브러리
import App from "./App.vue"; // 루트 컴포넌트
import router from "./router"; // 라우터 설정
import "./assets/main.css"; //Tailwind 가져오기
import "element-plus/dist/index.css"; // Element Plus CSS 추가
import piniaPersist from "pinia-plugin-persistedstate";

import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap";

import { useKakao } from "vue3-kakao-maps/@utils";
useKakao("a1b7d43f74e8d7c4fa60d02ce2c13f58");
// Vue 앱 인스턴스 생성
const app = createApp(App);

// Pinia 상태 관리 스토어 설정
// 이를 통해 애플리케이션 전체에서 상태를 공유하고 관리할 수 있습니다.

const pinia = createPinia();
pinia.use(piniaPersist);

app.use(pinia);

// Vue Router 설정
// 이를 통해 SPA(단일 페이지 애플리케이션)에서 페이지 간 네비게이션이 가능합니다.
app.use(router);

// 앱을 DOM에 마운트
// #app 요소에 Vue 애플리케이션을 연결합니다.
app.mount("#app");
