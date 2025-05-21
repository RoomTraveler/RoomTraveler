import axios from "axios";
import { useUserStore } from "@/store/userStore";
import router from "@/router";

const BASE_URL = "http://localhost:8080";

const instance = axios.create({
  baseURL: BASE_URL,
  timeout: 5000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청 인터셉터
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

const onRefreshed = (accessToken) => {
  refreshSubscribers.map((callback) => callback(accessToken));
  refreshSubscribers = [];
};

// 응답 인터셉터
instance.interceptors.response.use(
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

export default {
  api: instance,
  apiNoAuth,
};
