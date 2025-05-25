import axios from "axios";
import { useUserStore } from "@/store/userStore";
import { storeToRefs } from "pinia";  // 추가

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
        }
        return config; // 설정 반환 로직 복원
    },
    (error) => Promise.reject(error) // 에러 처리 콜백 복원
);

const apiNoAuth = axios.create({ // apiNoAuth 인스턴스 생성 복원
    baseURL: BASE_URL,
    timeout: 1000,
});

export default { api, apiNoAuth }; // 모듈 export 복원