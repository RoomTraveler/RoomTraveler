import axios from "axios";
import { useUserStore } from "@/store/userStore";
import router from "@/router";
import { storeToRefs } from "pinia"; // 추가

const BASE_URL = "http://localhost:8080";

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 5000,
});

// 모든 요청에 토큰 자동 주입 인터셉터
api.interceptors.request.use(
  (config) => {
    const userStore = useUserStore();
    const { tokens } = storeToRefs(userStore);
    const accessToken = tokens.value?.access_token;
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
      // console.log("[Request Interceptor] Token added to headers:", config.headers.Authorization); // 너무 많은 로그를 유발할 수 있어 주석 처리
    }
    return config; // 설정 반환 로직 복원
  },
  (error) => Promise.reject(error) // 에러 처리 콜백 복원
);

let isRefreshing = false;
let refreshSubscribers = [];

const onRefreshed = (accessToken) => {
  refreshSubscribers.map((callback) => callback(accessToken));
  refreshSubscribers = [];
};

// 응답 인터셉터
api.interceptors.response.use(
  (response) => {
    console.log("[응답 수신 1]: ", response.status, response.data);
    return response;
  },
  async (error) => {
    console.log("[오류 수신 1]: ", error);
    const originalRequest = error.config;
    const userStore = useUserStore();

    if (
      error.response?.status === 401 &&
      originalRequest.url !== "/api/user/auth/login" &&
      originalRequest.url !== "/api/user/refresh"
    ) {
      // 이미 재시도된 요청인지 확인
      if (originalRequest._isRetry) {
        console.error(
          "[API Interceptor] Request already retried but failed with 401. Logging out.",
          originalRequest.url
        );
        isRefreshing = false; // 확실히 초기화
        refreshSubscribers = []; // 대기열 초기화
        await userStore.logout(); // await 추가
        router.push("/login").catch(() => {});
        return Promise.reject(error); // 여기서 에러를 반환해야 루프에 빠지지 않음
      }

      if (!isRefreshing) {
        isRefreshing = true;
        console.log("[API Interceptor] Attempting to refresh token for", originalRequest.url);
        try {
          const refreshResult = await userStore.refresh();
          if (refreshResult.success && userStore.tokens?.access_token) {
            console.log(
              "[API Interceptor] Token refreshed successfully. Retrying original request:",
              originalRequest.url
            );
            console.log("[API Interceptor] New access token for retry:", userStore.tokens.access_token); // 새 토큰 로깅
            isRefreshing = false;
            onRefreshed(userStore.tokens.access_token);
            originalRequest.headers.Authorization = `Bearer ${userStore.tokens.access_token}`;
            originalRequest._isRetry = true; // 재시도 플래그 설정
            console.log(
              "[API Interceptor] Original request headers for retry:",
              JSON.parse(JSON.stringify(originalRequest.headers))
            ); // 헤더 전체 로깅
            return api(originalRequest);
          } else {
            console.error(
              "[API Interceptor] userStore.refresh did not return success or no access token.",
              refreshResult
            );
            throw new Error(refreshResult.error || "Failed to refresh token (no success or token)");
          }
        } catch (refreshError) {
          console.error("[API Interceptor] Catch block after refresh attempt. Logging out.", refreshError);
          isRefreshing = false;
          refreshSubscribers = []; // Ensure queue is cleared on error
          await userStore.logout(); // await 추가
          router.push("/login").catch(() => {});
          return Promise.reject(refreshError);
        }
      } else {
        console.log("[API Interceptor] Token refresh in progress, queuing request:", originalRequest.url);
        return new Promise((resolve) => {
          refreshSubscribers.push((accessToken) => {
            console.log("[API Interceptor] Executing queued request with new token:", originalRequest.url);
            console.log("[API Interceptor] Access token for queued request:", accessToken); // 콜백으로 받은 토큰 로깅
            originalRequest.headers.Authorization = `Bearer ${accessToken}`;
            originalRequest._isRetry = true; // 재시도 플래그 설정
            console.log(
              "[API Interceptor] Queued request headers for retry:",
              JSON.parse(JSON.stringify(originalRequest.headers))
            ); // 헤더 전체 로깅
            resolve(api(originalRequest));
          });
        });
      }
    }
    return Promise.reject(error);
  }
);

const apiNoAuth = axios.create({
  // apiNoAuth 인스턴스 생성 복원
  baseURL: BASE_URL,
  timeout: 1000,
});

export default { api, apiNoAuth }; // 모듈 export 복원
