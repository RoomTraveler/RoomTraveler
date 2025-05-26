import axios from "axios";
import { useUserStore } from "@/store/userStore";
import router from "@/router";
import { storeToRefs } from "pinia"; // 추가

const BASE_URL = "http://localhost:8080";

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 20000,
});

// 모든 요청에 토큰 자동 주입 인터셉터
api.interceptors.request.use(
  (config) => {
    const userStore = useUserStore();
    const { tokens } = storeToRefs(userStore);
    const accessToken = tokens.value?.access_token;
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
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

const apiNoAuth = axios.create({
  // apiNoAuth 인스턴스 생성 복원
  baseURL: BASE_URL,
  timeout: 5000,
});

export default { api, apiNoAuth }; // 모듈 export 복원
