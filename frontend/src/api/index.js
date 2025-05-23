// ================================
// Axios 인스턴스/인터셉터 관리 파일
// - 인증 필요 요청: api
// - 인증 불필요 요청: apiNoAuth
// ================================

import axios from "axios";
import { useUserStore } from "@/store/userStore";
import { useCommonStore } from "@/store/common";
import router from "@/router";

const BASE_URL = "http://localhost:8080";

// =====================
// 공통 작업 상태 관리 헬퍼
// =====================
const handleTask = (add) => {
  const commonStore = useCommonStore();
  if (add) {
    commonStore.addTask();
  } else {
    commonStore.removeTask();
  }
};

// =====================
// 인증이 필요한 axios 인스턴스 (api)
// =====================
const instance = axios.create({
  baseURL: BASE_URL,
  timeout: 5000,
  headers: {
    "Content-Type": "application/json",
  },
});

// ----- 요청 인터셉터: 토큰 있으면 Authorization 헤더 추가 -----
instance.interceptors.request.use(
    (config) => {
      console.log("[API Interceptor - Request] URL:", config.url);

      const userStore = useUserStore();
      const accessToken = userStore._tokens?.access_token;

      if (accessToken) {
        console.log("[API Interceptor - Request] Access Token FOUND:", accessToken);
        config.headers.Authorization = `Bearer ${accessToken}`;
        console.log("[API Interceptor - Request] Authorization header SET:", config.headers.Authorization);
      } else {
        console.warn("[API Interceptor - Request] Access Token NOT FOUND. Headers:", config.headers);
      }

      // 로그인 요청은 Content-Type만 변경
      if (config.url === "/api/user/auth/login" && config.method === "post") {
        config.headers["Content-Type"] = "application/x-www-form-urlencoded";
      }

      return config;
    },
    (error) => {
      console.error("[요청 오류]:", error);
      return Promise.reject(error);
    }
);

let isRefreshing = false;
let refreshSubscribers = [];

/**
 * 토큰 리프레시 완료 후 대기중인 요청에 새 토큰 적용
 */
const onRefreshed = (accessToken) => {
  refreshSubscribers.forEach((callback) => callback(accessToken));
  refreshSubscribers = [];
};

// ----- 응답 인터셉터: 401 발생 시 토큰 리프레시/재요청 처리 -----
instance.interceptors.response.use(
    (response) => {
      console.log("[응답 수신 1]: ", response.status, response.data);
      return response;
    },
    async (error) => {
      console.log("[오류 수신 1]: ", error);

      const originalRequest = error.config;
      const userStore = useUserStore();

      // 401(토큰만료) 발생 & 로그인/리프레시 API가 아닐 때만 동작
      if (
          error.response?.status === 401 &&
          originalRequest.url !== "/api/user/auth/login" &&
          originalRequest.url !== "/api/user/refresh"
      ) {
        if (!isRefreshing) {
          isRefreshing = true;
          try {
            console.log("Access token might be expired. Attempting to refresh...");
            const refreshResult = await userStore.refresh();
            if (refreshResult.success && userStore._tokens.value?.access_token) {
              console.log("Token refreshed successfully.");
              isRefreshing = false;
              onRefreshed(userStore._tokens.value.access_token);
              originalRequest.headers.Authorization = `Bearer ${userStore._tokens.value.access_token}`;
              return instance(originalRequest);
            } else {
              throw new Error(refreshResult.error || "Failed to refresh token");
            }
          } catch (refreshError) {
            console.error("Failed to refresh token after 401. Logging out.", refreshError);
            isRefreshing = false;
            userStore.logout();
            router.push("/login").catch(() => {});
            return Promise.reject(refreshError);
          }
        } else {
          // 이미 토큰 리프레시 중이면 큐에 추가하여 나중에 재시도
          return new Promise((resolve) => {
            refreshSubscribers.push((accessToken) => {
              originalRequest.headers.Authorization = `Bearer ${accessToken}`;
              resolve(instance(originalRequest));
            });
          });
        }
      }
      return Promise.reject(error);
    }
);

// =====================
// 인증 불필요 인스턴스 (apiNoAuth) - 비회원 API, 단순조회 등
// =====================
const apiNoAuth = axios.create({
  baseURL: BASE_URL,
  timeout: 1000,
});

// 요청/응답 시 로딩바 등 공통작업 처리
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

// =====================
// export
// =====================

/**
 * api: 인증 필요 요청 (로그인/회원/관리자)
 * apiNoAuth: 인증 불필요 요청 (비회원, 공개 데이터 등)
 */
export default {
  api: instance,
  apiNoAuth,
};