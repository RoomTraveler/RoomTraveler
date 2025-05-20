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
import { useUserStore } from "./store/userStore"; // 경로 확인
import ElementPlus from "element-plus"; // Element Plus import 추가
import "element-plus/dist/index.css"; // Element Plus CSS 추가
import koKR from "element-plus/es/locale/lang/ko"; // Element Plus 한국어 로케일 import
import "./assets/main.css"; //Tailwind 가져오기 (순서는 Element Plus CSS 뒤 또는 상황에 맞게)
import "bootstrap-icons/font/bootstrap-icons.css"; // Bootstrap Icons CSS 추가
import axios from "axios"; // axios import

// Axios 기본 설정 (중요!)
// axios.defaults.baseURL = 'http://localhost:8080'; // 백엔드 주소에 맞게 설정 (Vite 프록시 사용 시 불필요할 수 있음)
axios.defaults.withCredentials = true; // 모든 요청에 쿠키를 포함하도록 설정

// Vue 앱 인스턴스 생성
const app = createApp(App);
const pinia = createPinia();
app.use(pinia);

// 앱 마운트 전에 사용자 정보 로드 시도
async function initializeApp() {
  const userStore = useUserStore();
  console.log("[main.js] Initializing app, attempting to fetch current user...");
  try {
    await userStore.fetchCurrentUser();
    console.log("[main.js] fetchCurrentUser completed. User state:", JSON.parse(JSON.stringify(userStore.user)));
  } catch (e) {
    console.error("[main.js] Failed to fetch current user on app load:", e);
  }

  app.use(router); // 라우터는 사용자 정보 로드 후 또는 병렬로 설정 가능
  app.use(ElementPlus, { locale: koKR });
  app.mount("#app");
  console.log("[main.js] App mounted.");
}

initializeApp();
