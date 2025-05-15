import axios from 'axios';

// 기본 설정으로 Axios 인스턴스 생성
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  // 요청 타임아웃 설정 (5초)
  timeout: 5000,
});

// 인증 토큰을 추가하기 위한 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 오류 처리를 위한 응답 인터셉터
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // 401 Unauthorized 오류 처리 (토큰 만료)
    if (error.response && error.response.status === 401) {
      // 로컬 스토리지 정보 삭제 및 로그인 페이지로 리디렉션
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('userInfo');
      // 이미 로그인 페이지가 아닌 경우에만 리디렉션
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }

    // 네트워크 오류 처리
    if (error.message === 'Network Error') {
      console.error('네트워크 오류가 발생했습니다. 백엔드 서버가 실행 중인지 확인하세요.');
      error.message = '네트워크 오류: 서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지 확인하세요.';
    }

    // 타임아웃 오류 처리
    if (error.code === 'ECONNABORTED') {
      console.error('요청 시간이 초과되었습니다. 서버 응답이 없습니다.');
      error.message = '요청 시간 초과: 서버 응답이 없습니다. 백엔드 서버 상태를 확인하세요.';
    }

    return Promise.reject(error);
  }
);

export default api;
