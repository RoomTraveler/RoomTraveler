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
        const stored = localStorage.getItem("user");
        if (stored) {
          user.value = JSON.parse(stored);
        }
      } catch (err) {
        console.error("사용자 정보 로드 중 오류 발생:", err);
        localStorage.removeItem("user");
      }
    }

    async function login(email, password) {
      loading.value = true;
      error.value = null;

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
      const decoded = jwtDecode(_tokens.value.access_token);
      user.value = { name: decoded.name, email: decoded.email, role: decoded.role };
    }

    async function register(userData) {
      loading.value = true;
      error.value = null;

      try {
        await new Promise((r) => setTimeout(r, 1000));

        if (userData.email === "user@example.com") {
          throw new Error("이미 사용 중인 이메일 주소입니다.");
        }

        return { success: true };
      } catch (err) {
        error.value = err.message;
        return { success: false, error: err.message };
      } finally {
        loading.value = false;
      }
    }

    async function logout() {
      try {
        _tokens.value.accessToken = null;
        await api.api.post("/api/user/auth/logout", {
          headers: {
            "Refresh-Token": _tokens.value.refreshToken,
          },
        });
      } finally {
        user.value = null;
        _tokens.value = {};
        localStorage.removeItem("user");
      }
    }

    async function updateProfile(profileData) {
      loading.value = true;
      error.value = null;

      try {
        await new Promise((r) => setTimeout(r, 1000));

        user.value = { ...user.value, ...profileData };
        localStorage.setItem("user", JSON.stringify(user.value));
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
      _tokens.value.accessToken = null;
      const response = await api.api.post("/api/user/auth/refresh", {
        headers: {
          "Refresh-Token": _tokens.value.refreshToken,
        },
      });
      _tokens.value = response.data.data;
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
      login,
      register,
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
