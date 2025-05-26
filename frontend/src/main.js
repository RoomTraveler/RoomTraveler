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
import piniaPersist from "pinia-plugin-persistedstate";

// Swiper CSS 추가
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/pagination";
// import 'swiper/css/scrollbar'; // 필요 시 추가

// Toast UI Editor CSS 추가
import "@toast-ui/editor/dist/toastui-editor.css";

// Axios 기본 설정 (중요!)
// axios.defaults.baseURL = 'http://localhost:8080'; // 백엔드 주소에 맞게 설정 (Vite 프록시 사용 시 불필요할 수 있음)
axios.defaults.withCredentials = true; // 모든 요청에 쿠키를 포함하도록 설정
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap";

import { useKakao } from "vue3-kakao-maps/@utils";
useKakao("a1b7d43f74e8d7c4fa60d02ce2c13f58");
// Vue 앱 인스턴스 생성
const app = createApp(App);
const pinia = createPinia();
pinia.use(piniaPersist);

app.use(pinia);

// 앱 마운트 전에 사용자 정보 로드 시도
async function initializeApp() {
  const userStore = useUserStore(); // Pinia 인스턴스가 앱에 등록된 후 스토어 사용
  console.log("[main.js] Initializing app.");

  // persisted state (_tokens)는 자동으로 로드됨.
  // sessionStorage의 user는 각 컴포넌트나 라우트 가드에서 loadUserFromStorage()를 호출하여 필요시 로드 가능.

  // 토큰이 이미 스토어에 있고 (persist 플러그인에 의해 복원됨), 스토어에 사용자 정보가 아직 없거나 불완전하면 가져옴.
  if (userStore.tokens?.access_token && (!userStore.user || !userStore.user.id || !userStore.user.createdAt)) {
    console.log("[main.js] Access token found and user data is missing or incomplete, attempting to fetch current user...");
    try {
      await userStore.fetchCurrentUser();
      console.log("[main.js] fetchCurrentUser completed on app load. User state:", JSON.parse(JSON.stringify(userStore.user)));
    } catch (e) {
      console.error("[main.js] Failed to fetch current user on app load:", e);
      // 여기서 에러 발생 시 (예: 토큰 만료로 401) 자동 로그아웃 처리도 고려 가능
      // if (e?.response?.status === 401 || e?.response?.status === 403) {
      //   await userStore.logout();
      // }
    }
  } else if (userStore.user?.id && userStore.user?.createdAt) {
    console.log("[main.js] User data (including createdAt) already available from persisted state or previous fetch.");
  } else if (userStore.tokens?.access_token) {
    console.log("[main.js] Access token found, but user data might be fetched on demand by components (e.g. UserProfile).");
  } else {
    console.log("[main.js] No access token found. User needs to login. Data will be fetched on demand.");
  }

  app.use(router); // 라우터 설정
  app.use(ElementPlus, { locale: koKR }); // ElementPlus 설정
  app.mount("#app"); // 앱 마운트
  console.log("[main.js] App mounted.");
}

initializeApp();

export { router }; // router export 추가
