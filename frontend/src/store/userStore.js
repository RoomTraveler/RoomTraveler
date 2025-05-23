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
          const parsedData = JSON.parse(stored);
          if (parsedData && parsedData.user) {
            user.value = parsedData.user;
          } else {
            console.warn(
              "sessionStorage의 'user' 데이터 형식이 예상과 다릅니다. (parsedData.user 없음) 저장된 데이터:",
              parsedData
            );
            sessionStorage.removeItem("user");
            user.value = null;
          }
        } else {
          user.value = null;
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
        return { success: false, error: "No access token found" };
      }

      loading.value = true;
      error.value = null;
      try {
        const response = await api.api.get("/api/user/me", {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          },
        });
        user.value = response.data.user;
        if (user.value) {
          sessionStorage.setItem("user", JSON.stringify({ user: user.value }));
        }
        return { success: true, user: user.value };
      } catch (err) {
        error.value = err.response?.data?.message || err.message || "Failed to fetch user";
        user.value = null;
        _tokens.value = {};
        sessionStorage.removeItem("user");
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
          profileImage: decoded.profileImage || user.value?.profileImage || null,
        };
        sessionStorage.setItem("user", JSON.stringify({ user: user.value }));
        return { success: true, user: user.value, tokens: _tokens.value };
      } catch (err) {
        error.value = err.response?.data?.message || err.message || "로그인에 실패했습니다.";
        _tokens.value = {};
        user.value = null;
        sessionStorage.removeItem("user");
        return { success: false, error: error.value };
      } finally {
        loading.value = false;
      }
    }

    async function logout() {
      try {
        // Optional: Call backend logout API if exists
        // await api.api.post("/api/user/auth/logout", {}, {
        //   headers: { Authorization: `Bearer ${_tokens.value?.access_token}` }
        // });
      } catch (e) {
        console.error("Logout API call failed:", e);
      } finally {
        user.value = null;
        _tokens.value = {};
        sessionStorage.removeItem("user");
        sessionStorage.removeItem("_tokens");
        // router.push('/login'); // Usually handled by the component or navigation guard
      }
    }

    async function updateProfile(profileData) {
      loading.value = true;
      error.value = null;
      const accessToken = _tokens.value?.access_token;
      if (!accessToken) {
        return { success: false, error: "No access token found" };
      }

      try {
        const payloadToSend = {
          email: user.value.email,
          username: profileData.username,
          phone: profileData.phone,
        };

        await api.api.put("/api/user/me", payloadToSend, {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          },
        });

        user.value = {
          ...user.value,
          name: profileData.username,
          username: profileData.username,
          phone: profileData.phone,
        };
        if (user.value) {
          sessionStorage.setItem("user", JSON.stringify({ user: user.value }));
        }
        return { success: true, user: user.value };
      } catch (err) {
        error.value = err.response?.data?.message || err.message || "프로필 업데이트에 실패했습니다.";
        return { success: false, error: error.value };
      } finally {
        loading.value = false;
      }
    }

    async function updateProfileImage(formData) {
      loading.value = true;
      error.value = null;
      const accessToken = _tokens.value?.access_token;

      if (!accessToken) {
        loading.value = false;
        return { success: false, error: "No access token found for profile image update." };
      }

      try {
        const response = await api.api.put("/api/user/profile-image", formData, {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": undefined,
          },
        });

        if (response.data && response.data.success && response.data.imageUrl) {
          if (user.value) {
            user.value.profileImage = response.data.imageUrl;
            sessionStorage.setItem("user", JSON.stringify({ user: user.value }));
          }
          return { success: true, imageUrl: response.data.imageUrl };
        } else {
          error.value =
            response.data?.message || "Profile image update failed at server (no imageUrl or success:false).";
          return { success: false, error: error.value };
        }
      } catch (err) {
        console.error("updateProfileImage error:", err);
        error.value = err.response?.data?.message || err.message || "Failed to update profile image.";
        return { success: false, error: error.value };
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
      try {
        const response = await api.api.post(
          "/api/user/refresh",
          {},
          {
            headers: {
              "Refresh-Token": _tokens.value?.refreshToken,
            },
          }
        );
        _tokens.value = response.data;
        return { success: true };
      } catch (refreshError) {
        console.error("Failed to refresh token:", refreshError);
        logout();
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
      updateProfileImage,
      changePassword,
      refresh,
    };
  },
  {
    persist: {
      storage: sessionStorage,
      paths: ["_tokens"],
    },
  }
);
