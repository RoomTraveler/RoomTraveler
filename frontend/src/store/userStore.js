import { defineStore } from "pinia";
import { ref, computed } from "vue";
import api from "@/api/index";
import { jwtDecode } from "jwt-decode";

export const useUserStore = defineStore(
  "user",
  () => {
    // state
    const user = ref(null);
    const loading = ref(false);
    const error = ref(null);
    const _tokens = ref({});
    const tokens = computed(() => _tokens.value);

    // getters
    const isAuthenticated = computed(() => !!user.value);
    const userName = computed(() => user.value?.name || "");
    const userEmail = computed(() => user.value?.email || "");
    const userRole = computed(() => user.value?.role || "GUEST");

    // actions
    function loadUserFromStorage() {
      try {
        const stored = sessionStorage.getItem("user");
        if (stored && stored !== "undefined") {
          user.value = JSON.parse(stored);
        }
      } catch (err) {
        console.error("사용자 정보 로드 중 오류 발생:", err);
        sessionStorage.removeItem("user");
      }
    }

    async function fetchCurrentUser() {
      const accessToken = _tokens.value?.access_token;
      if (!accessToken) {
        user.value = null;
        // sessionStorage.removeItem("user"); // loadUserFromStorage에서 이미 처리할 수 있음
        return { success: false, error: "No access token found" };
      }

      loading.value = true;
      error.value = null;
      try {
        // '/api/user/me' 또는 실제 사용자 정보 조회 API 엔드포인트로 변경해야 합니다.
        const response = await api.api.get("/api/user/me", {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json", // 필요에 따라 Content-Type 지정
          },
        });
        user.value = response.data.user; // API 응답 구조에 맞게 수정
        sessionStorage.setItem("user", JSON.stringify(user.value)); // sessionStorage로 변경
        return { success: true, user: user.value };
      } catch (err) {
        error.value = err.response?.data?.message || err.message || "Failed to fetch user";
        user.value = null;
        _tokens.value = {}; // 토큰이 유효하지 않으므로 초기화
        sessionStorage.removeItem("user"); // sessionStorage로 변경
        return { success: false, error: error.value };
      } finally {
        loading.value = false;
      }
    }

    async function login(email, password) {
      loading.value = true;
      error.value = null;
      try {
        const response = await api.api({
          url: "/api/user/auth/login",
          method: "post",
          data: {
            email,
            password,
          },
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
        });
        _tokens.value = response.data;
        const decoded = jwtDecode(response.data.access_token);
        user.value = {
          id: decoded.id,
          name: decoded.name,
          email: decoded.email,
          role: decoded.role,
        };
        sessionStorage.setItem("user", JSON.stringify(user.value));
        return { success: true, user: user.value, tokens: _tokens.value };
      } catch (err) {
        error.value = err.response?.data?.message || err.message || "로그인에 실패했습니다.";
        _tokens.value = {};
        user.value = null;
        sessionStorage.removeItem("user"); // sessionStorage로 변경
        return { success: false, error: error.value };
      } finally {
        loading.value = false;
      }
    }

    async function logout() {
      try {
        // await api.api.post("/api/user/auth/logout", {}, { // 백엔드 로그아웃 API 호출 (필요시)
        //   headers: {
        //     "Authorization": `Bearer ${_tokens.value?.access_token}`,
        //   }
        // });
      } catch (e) {
        console.error("Logout API call failed:", e);
      } finally {
        user.value = null;
        _tokens.value = {};
        sessionStorage.removeItem("user"); // sessionStorage로 변경
        // router.push('/login'); // 여기서 리다이렉션 또는 호출한 곳에서 처리
      }
    }

    async function updateProfile(profileData) {
      loading.value = true;
      error.value = null;

      try {
        await new Promise((r) => setTimeout(r, 1000));

        user.value = { ...user.value, ...profileData };
        sessionStorage.setItem("user", JSON.stringify(user.value)); // sessionStorage로 변경
        return { success: true };
      } catch (err) {
        error.value = err.message;
        return { success: false, error: err.message };
      } finally {
        loading.value = false;
      }
    }

    async function changePassword(currentPassword, newPassword) {
      loading.value = true;
      error.value = null;

      try {
        await new Promise((r) => setTimeout(r, 1000));

        if (currentPassword !== "password") {
          throw new Error("현재 비밀번호가 올바르지 않습니다.");
        }

        return { success: true };
      } catch (err) {
        error.value = err.message;
        return { success: false, error: err.message };
      } finally {
        loading.value = false;
      }
    }

    const refresh = async () => {
      // _tokens.value.accessToken = null; // 재발급 요청 중에는 이전 access token을 사용할 수 있으므로, 굳이 null로 만들 필요는 없을 수 있습니다.
      try {
        const response = await api.api.post(
          "/api/user/refresh",
          {},
          {
            // api.api 대신 api 인스턴스 직접 사용, GET 요청이 아니라면 data로 빈 객체 전달
            headers: {
              "Refresh-Token": _tokens.value?.refreshToken, // .value 접근 및 optional chaining
            },
          }
        );
        _tokens.value = response.data; // 백엔드 응답 구조에 맞게 수정
        // 새 Access Token을 originalRequest 헤더에 설정하는 로직은 인터셉터에서 처리합니다.
        return { success: true };
      } catch (refreshError) {
        console.error("Failed to refresh token:", refreshError);
        // 리프레시 실패 시 로그아웃 처리 또는 에러 전파
        logout(); // 예: 리프레시 실패 시 강제 로그아웃
        return { success: false, error: "Failed to refresh token" };
      }
    };

    return {
      // state
      user,
      loading,
      error,
      tokens,
      _tokens,

      // getters
      isAuthenticated,
      userName,
      userEmail,
      userRole,

      // actions
      loadUserFromStorage,
      fetchCurrentUser,
      login,
      logout,
      updateProfile,
      changePassword,
      refresh,
    };
  },
  {
    persist: {
      storage: sessionStorage,
      paths: ["user", "_tokens"],
    },
  }
);
