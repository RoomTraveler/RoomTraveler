import { defineStore } from "pinia";
import axios from "axios";

// 사용자 스토어 정의
export const useUserStore = defineStore("user", {
  // 상태(state)
  state: () => ({
    user: null,
    loading: false,
    error: null,
  }),

  // 게터(getters)
  getters: {
    isAuthenticated: (state) => !!state.user,
    userName: (state) => state.user?.name || "",
    userEmail: (state) => state.user?.email || "",
    userRole: (state) => state.user?.role || "GUEST",
  },

  // 액션(actions)
  actions: {
    // 로컬 스토리지에서 사용자 정보 로드 (앱 초기화 시 fetchCurrentUser가 주 로직이 됨)
    loadUserFromStorage() {
      console.log("[userStore] loadUserFromStorage - START");
      try {
        const storedUser = localStorage.getItem("user");
        if (storedUser) {
          const parsedUser = JSON.parse(storedUser);
          this.user = parsedUser;
          console.log("[userStore] loadUserFromStorage - User LOADED from localStorage:", this.user);
        } else {
          console.log("[userStore] loadUserFromStorage - No user in localStorage.");
        }
      } catch (error) {
        console.error("[userStore] loadUserFromStorage - Error loading user from localStorage:", error);
        localStorage.removeItem("user");
        this.user = null;
      }
      console.log("[userStore] loadUserFromStorage - END");
    },

    // 현재 사용자 정보 가져오기 (앱 로드 시 사용)
    async fetchCurrentUser() {
      this.loading = true;
      console.log("[userStore] fetchCurrentUser - START");
      try {
        const response = await axios.get("/api/user/me");
        console.log("[userStore] fetchCurrentUser - API response:", response);
        if (response.data && response.data.success) {
          const userData = response.data.user;
          this.user = {
            id: userData.userId,
            email: userData.email,
            name: userData.username, // 백엔드 username을 스토어 user.name으로 매핑
            role: userData.role,
          };
          localStorage.setItem("user", JSON.stringify(this.user));
          console.log("[userStore] fetchCurrentUser - User SET from API:", this.user);
          return this.user;
        } else {
          console.warn("[userStore] fetchCurrentUser - API success false or no data:", response.data);
          this.user = null;
          localStorage.removeItem("user");
          return null;
        }
      } catch (error) {
        if (error.response && error.response.status === 401) {
          console.log("[userStore] fetchCurrentUser - User not authenticated (401).");
        } else {
          console.error("[userStore] fetchCurrentUser - CATCH error:", error.response || error);
        }
        this.user = null;
        localStorage.removeItem("user");
        return null;
      } finally {
        this.loading = false;
        console.log("[userStore] fetchCurrentUser - END");
      }
    },

    // 로그인 처리
    async login(email, password) {
      this.loading = true;
      this.error = null;
      console.log("[userStore] login - START", { email });
      try {
        const response = await axios.post("/api/user/login", { email, password });
        console.log("[userStore] login - API response:", response);
        if (response.data && response.data.success) {
          const userData = response.data.user;
          this.user = {
            id: userData.userId,
            email: userData.email,
            name: userData.username,
            role: userData.role,
          };
          localStorage.setItem("user", JSON.stringify(this.user));
          console.log("[userStore] login - User SET after login:", this.user);
          return { success: true, user: this.user };
        } else {
          console.warn("[userStore] login - API success false or message:", response.data?.message);
          throw new Error(response.data.message || "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
      } catch (error) {
        console.error("[userStore] login - CATCH error:", error.response?.data?.message || error.message);
        this.error = error.response?.data?.message || error.message || "로그인 중 오류가 발생했습니다.";
        localStorage.removeItem("user");
        this.user = null;
        return { success: false, error: this.error };
      } finally {
        this.loading = false;
        console.log("[userStore] login - END");
      }
    },

    // 회원가입 처리
    async register(userData) {
      this.loading = true;
      this.error = null;
      console.log("[userStore] register - START", userData);
      try {
        const response = await axios.post("/api/user/register", userData);
        console.log("[userStore] register - API response:", response);
        if (response.data && response.data.success) {
          console.log("[userStore] register - Success:", response.data.message);
          return { success: true, message: response.data.message };
        } else {
          console.warn("[userStore] register - API success false or message:", response.data?.message);
          throw new Error(response.data.message || "회원가입에 실패했습니다.");
        }
      } catch (error) {
        console.error("[userStore] register - CATCH error:", error.response?.data?.message || error.message);
        this.error = error.response?.data?.message || error.message || "회원가입 중 오류가 발생했습니다.";
        return { success: false, error: this.error };
      } finally {
        this.loading = false;
        console.log("[userStore] register - END");
      }
    },

    // 로그아웃 처리
    async logout() {
      this.loading = true;
      this.error = null;
      console.log("[userStore] logout - START");
      try {
        await axios.post("/user/logout");
        console.log("[userStore] logout - API call success (assumed)");
        this.user = null;
        localStorage.removeItem("user");
        console.log("[userStore] logout - User CLEARED");
        return { success: true };
      } catch (error) {
        console.error("[userStore] logout - CATCH error:", error.response?.data?.message || error.message);
        this.error = error.response?.data?.message || error.message || "로그아웃 중 오류가 발생했습니다.";
        this.user = null;
        localStorage.removeItem("user"); // 실패 시에도 로컬은 클리어
        return { success: false, error: this.error };
      } finally {
        this.loading = false;
        console.log("[userStore] logout - END");
      }
    },

    // 사용자 정보 업데이트
    async updateProfile(profileData) {
      this.loading = true;
      this.error = null;
      console.log("[userStore] updateProfile - START", profileData);
      if (!this.user || !this.user.email) {
        this.error = "로그인 정보가 없습니다.";
        console.warn("[userStore] updateProfile - No user or email", this.user);
        return { success: false, error: this.error };
      }
      try {
        const requestData = {
          email: this.user.email,
          username: profileData.username,
        };
        const response = await axios.put("/api/user/me", requestData);
        console.log("[userStore] updateProfile - API response:", response);
        if (response.data && response.data.success) {
          if (this.user && profileData.username) {
            this.user.name = profileData.username;
            localStorage.setItem("user", JSON.stringify(this.user));
            console.log("[userStore] updateProfile - User profile UPDATED locallly:", this.user);
          }
          return { success: true, message: response.data.message };
        } else {
          console.warn("[userStore] updateProfile - API success false or message:", response.data?.message);
          throw new Error(response.data.message || "프로필 업데이트에 실패했습니다.");
        }
      } catch (error) {
        console.error("[userStore] updateProfile - CATCH error:", error.response?.data?.message || error.message);
        this.error = error.response?.data?.message || error.message || "프로필 업데이트 중 오류가 발생했습니다.";
        return { success: false, error: this.error };
      } finally {
        this.loading = false;
        console.log("[userStore] updateProfile - END");
      }
    },

    // 비밀번호 변경
    async changePassword(newPassword) {
      this.loading = true;
      this.error = null;
      console.log("[userStore] changePassword - START");
      if (!newPassword || newPassword.trim() === "") {
        this.error = "새 비밀번호를 입력해주세요.";
        console.warn("[userStore] changePassword - New password empty");
        return { success: false, error: this.error };
      }

      try {
        const response = await axios.put("/api/user/password", { password: newPassword });
        console.log("[userStore] changePassword - API response:", response);
        if (response.data && response.data.success) {
          console.log("[userStore] changePassword - Success:", response.data.message);
          return { success: true, message: response.data.message };
        } else {
          console.warn("[userStore] changePassword - API success false or message:", response.data?.message);
          throw new Error(response.data.message || "비밀번호 변경에 실패했습니다.");
        }
      } catch (error) {
        console.error("[userStore] changePassword - CATCH error:", error.response?.data?.message || error.message);
        this.error = error.response?.data?.message || error.message || "비밀번호 변경 중 오류가 발생했습니다.";
        return { success: false, error: this.error };
      } finally {
        this.loading = false;
        console.log("[userStore] changePassword - END");
      }
    },
  },
});
