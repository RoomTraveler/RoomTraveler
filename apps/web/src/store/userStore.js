import { defineStore } from 'pinia';

// 사용자 스토어 정의
export const useUserStore = defineStore('user', {
  // 상태(state)
  state: () => ({
    user: null,
    loading: false,
    error: null
  }),

  // 게터(getters)
  getters: {
    isAuthenticated: (state) => !!state.user,
    userName: (state) => state.user?.name || '',
    userEmail: (state) => state.user?.email || '',
    userRole: (state) => state.user?.role || 'GUEST'
  },

  // 액션(actions)
  actions: {
    // 로컬 스토리지에서 사용자 정보 로드
    loadUserFromStorage() {
      try {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
          this.user = JSON.parse(storedUser);
        }
      } catch (error) {
        console.error('사용자 정보 로드 중 오류 발생:', error);
        localStorage.removeItem('user');
      }
    },

    // 로그인 처리
    async login(email, password) {
      this.loading = true;
      this.error = null;

      try {
        // 실제 API 호출 대신 임시 로직 사용
        await new Promise(resolve => setTimeout(resolve, 1000));

        // 간단한 유효성 검사 (실제로는 서버에서 처리)
        if (email === 'user@example.com' && password === 'password') {
          // 로그인 성공
          const user = {
            id: 1,
            email: email,
            name: '홍길동',
            role: 'USER',
            createdAt: new Date().toISOString()
          };

          // 상태 업데이트
          this.user = user;

          // 로컬 스토리지에 사용자 정보 저장
          localStorage.setItem('user', JSON.stringify(user));

          return { success: true };
        } else {
          // 로그인 실패
          throw new Error('이메일 또는 비밀번호가 올바르지 않습니다.');
        }
      } catch (error) {
        this.error = error.message;
        return { success: false, error: error.message };
      } finally {
        this.loading = false;
      }
    },

    // 회원가입 처리
    async register(userData) {
      this.loading = true;
      this.error = null;

      try {
        // 실제 API 호출 대신 임시 로직 사용
        await new Promise(resolve => setTimeout(resolve, 1000));

        // 이메일 중복 체크 (실제로는 서버에서 처리)
        if (userData.email === 'user@example.com') {
          throw new Error('이미 사용 중인 이메일 주소입니다.');
        }

        // 회원가입 성공 (실제로는 서버에서 처리)
        return { success: true };
      } catch (error) {
        this.error = error.message;
        return { success: false, error: error.message };
      } finally {
        this.loading = false;
      }
    },

    // 로그아웃 처리
    logout() {
      this.user = null;
      localStorage.removeItem('user');
    },

    // 사용자 정보 업데이트
    async updateProfile(profileData) {
      this.loading = true;
      this.error = null;

      try {
        // 실제 API 호출 대신 임시 로직 사용
        await new Promise(resolve => setTimeout(resolve, 1000));

        // 사용자 정보 업데이트
        this.user = {
          ...this.user,
          ...profileData
        };

        // 로컬 스토리지 업데이트
        localStorage.setItem('user', JSON.stringify(this.user));

        return { success: true };
      } catch (error) {
        this.error = error.message;
        return { success: false, error: error.message };
      } finally {
        this.loading = false;
      }
    },

    // 비밀번호 변경
    async changePassword(currentPassword, newPassword) {
      this.loading = true;
      this.error = null;

      try {
        // 실제 API 호출 대신 임시 로직 사용
        await new Promise(resolve => setTimeout(resolve, 1000));

        // 현재 비밀번호 확인 (실제로는 서버에서 처리)
        if (currentPassword !== 'password') {
          throw new Error('현재 비밀번호가 올바르지 않습니다.');
        }

        // 비밀번호 변경 성공 (실제로는 서버에서 처리)
        return { success: true };
      } catch (error) {
        this.error = error.message;
        return { success: false, error: error.message };
      } finally {
        this.loading = false;
      }
    }
  }
});