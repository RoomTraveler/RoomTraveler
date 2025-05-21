import axios from "axios";
import { useUserStore } from "@/store/userStore";

// 기본 설정으로 Axios 인스턴스 생성
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  // 요청 타임아웃 설정 (5초)
  timeout: 5000,
});

// 인증 토큰을 추가하기 위한 요청 인터셉터
api.interceptors.request.use(
  async (config) => {
    console.log("[요청 발신]: ", config.mehtod, config.url, config.data);
    handleTask(true);
    const memberStore = useUserStore();

    if (memberStore.token?.accessToken) {
      config.headers["Authorization"] = `Bearer ${memberStore.tokens.accessToken}`;
    }
    return config;
  },
  (error) => {
    console.log("[요청 실패]: ", error);
    handleTask(false);
    return Promise.reject(error);
  }
);

// 오류 처리를 위한 응답 인터셉터
api.interceptors.response.use(
  (response) => {
    console.log("[응답 수신 1]: ", response.status, response.data);
    handleTask(false);
    return response;
  },
  async (error) => {
    console.log("[오류 수신 1]: ", error);
    handleTask(false);
    // 401 Unauthorized 오류 처리 (토큰 만료)
    if (error.status === 401 && error.response?.data.message === "Token_ERROR") {
      console.log("access tokens 만료");
      const originalRequest = error.config;
      const memberStore = useUserStore();
      try {
        await memberStore.refresh();
        return api(originalRequest);
      } catch (e) {
        alert("refresh까지 만료!! 로그아웃 합니다.");
        memberStore.logout();
      }
      // 이미 로그인 페이지가 아닌 경우에만 리디렉션
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }

    // 네트워크 오류 처리
    if (error.message === "Network Error") {
      console.error("네트워크 오류가 발생했습니다. 백엔드 서버가 실행 중인지 확인하세요.");
      error.message = "네트워크 오류: 서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지 확인하세요.";
    }

    // 타임아웃 오류 처리
    if (error.code === "ECONNABORTED") {
      console.error("요청 시간이 초과되었습니다. 서버 응답이 없습니다.");
      error.message = "요청 시간 초과: 서버 응답이 없습니다. 백엔드 서버 상태를 확인하세요.";
    }

    return Promise.reject(error);
  }
);

const apiNoAuth = axios.create({
  baseURL: "http://localhost:8080",
  timeout: 1000,
});

apiNoAuth.interceptors.request.use(
  async (config) => {
    console.log("[요청 발신]: ", config.method, config.url, config.data);
    handleTask(true);
    return config;
  },
  (error) => {
    console.log("[요청 실패]: ", error);
    handleTask(false);
    return Promise.reject(error);
  }
);

apiNoAuth.interceptors.response.use(
  (response) => {
    console.log("[응답 수신 2]: ", response.status, response.data);
    handleTask(false);
    return response;
  },
  async (error) => {
    console.log("[오류 수신 2]: ", error);
    handleTask(false);
    return Promise.reject(error);
  }
);

import { useCommonStore } from "@/store/common";
const handleTask = (add) => {
  const commonStore = useCommonStore();
  if (add) {
    commonStore.addTask();
  } else {
    commonStore.removeTask();
  }
};

export default { api, apiNoAuth };
