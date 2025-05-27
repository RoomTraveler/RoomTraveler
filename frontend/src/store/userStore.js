import { defineStore } from "pinia";
import { ref, computed } from "vue";
import apiUtils from "@/api";
import { jwtDecode } from "jwt-decode";
import apiGroup from "@/api/index";

export const useUserStore = defineStore(
  "user",
  () => {
    // state
    const user = ref(null);
    const loading = ref(false);
    const error = ref(null);
    const _tokens = ref({});
    const tokens = computed(() => _tokens.value);
    const successMessage = ref(null);

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
          if (parsedData && parsedData.user && parsedData.user.id) {
            user.value = parsedData.user;
          } else {
            sessionStorage.removeItem("user");
            user.value = null;
          }
        } else {
          user.value = null;
        }
      } catch (err) {
        sessionStorage.removeItem("user");
        user.value = null;
      }
    }

    async function fetchCurrentUser() {
      const accessToken = _tokens.value?.access_token;
      if (!accessToken) {
        return { success: false, error: "No access token found" };
      }
      loading.value = true;
      error.value = null;
      try {
        const response = await apiUtils.api.get("/api/user/me", {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        });
        if (response.data && response.data.success && response.data.user && response.data.user.userId) {
          const serverUser = response.data.user;
          user.value = {
            id: serverUser.userId,
            name: serverUser.username,
            username: serverUser.username,
            email: serverUser.email,
            role: serverUser.role,
            profileImage: serverUser.profileImage,
            createdAt: serverUser.createdAt,
            phone: serverUser.phone,
            status: serverUser.status,
            updatedAt: serverUser.updatedAt,
            hostId: serverUser.hostId,
          };
          sessionStorage.setItem("user", JSON.stringify({ user: user.value }));
          return { success: true, user: user.value };
        } else {
          error.value = response.data?.message || "서버로부터 유효한 사용자 정보를 받지 못했습니다.";
          return { success: false, error: error.value };
        }
      } catch (err) {
        error.value = err.response?.data?.message || err.message || "Failed to fetch user";
        if (err.response?.status === 401 || err.response?.status === 403) {
          await logout();
        }
        return { success: false, error: error.value };
      } finally {
        loading.value = false;
      }
    }

    async function login(email, password) {
      loading.value = true;
      error.value = null;
      try {
        const response = await apiUtils.api.post(
          "/api/user/auth/login",
          { email, password },
          { headers: { "Content-Type": "application/x-www-form-urlencoded" } }
        );
        _tokens.value = response.data;
        const decoded = jwtDecode(response.data.access_token);
        user.value = {
          id: decoded.id,
          name: decoded.name,
          email: decoded.email,
          role: decoded.role,
          profileImage: decoded.profileImage || null,
          hostId: decoded.hostId || null,
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
      user.value = null;
      _tokens.value = {};
      sessionStorage.removeItem("user");
      sessionStorage.removeItem("_tokens");
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
        await apiUtils.api.put("/api/user/me", payloadToSend, {
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
        sessionStorage.setItem("user", JSON.stringify({ user: user.value }));
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
        const response = await apiUtils.api.put("/api/user/profile-image", formData, {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            // Content-Type 생략(FormData 자동 처리)
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
        error.value = err.response?.data?.message || err.message || "Failed to update profile image.";
        return { success: false, error: error.value };
      } finally {
        loading.value = false;
      }
    }

    async function changePassword(currentPassword, newPassword) {
      loading.value = true;
      error.value = null;
      successMessage.value = null;

      try {
        const payload = { currentPassword, newPassword };
        const res = await apiGroup.api({
          url: "/api/user/password",
          method: "PUT",
          data: payload,
        });

        // HTTP 200일 때
        if (res.status === 200 && res.data.success) {
          successMessage.value = res.data.message || "비밀번호 변경 성공";
          return { success: true };
        } else {
          // 서버가 success=false 로 내려준 경우
          const msg = res.data.message || "비밀번호 변경에 실패했습니다.";
          error.value = msg;
          return { success: false, message: msg };
        }
      } catch (err) {
        // 네트워크 오류나 4xx/5xx 에러
        if (err.response && err.response.data && err.response.data.message) {
          error.value = err.response.data.message;
        } else {
          error.value = err.message || "서버 연결 중 오류가 발생했습니다.";
        }
        return { success: false, message: error.value };
      } finally {
        loading.value = false;
      }
    }

    const refresh = async () => {
      try {
        const response = await apiUtils.api.post(
          "/api/user/refresh",
          {},
          {
            headers: { "Refresh-Token": _tokens.value?.refreshToken },
          }
        );
        _tokens.value = response.data;
        return { success: true };
      } catch (refreshError) {
        await logout();
        return { success: false, error: "Failed to refresh token" };
      }
    };

    return {
      user,
      loading,
      error,
      tokens,
      _tokens,
      isAuthenticated,
      userName,
      userEmail,
      userRole,
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
