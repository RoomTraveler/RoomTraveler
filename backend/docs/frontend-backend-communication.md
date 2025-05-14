# Vue.js와 Spring Boot 백엔드 통신 가이드

이 문서는 Vue.js 프론트엔드와 Spring Boot 백엔드 간의 통신 설정 방법을 설명합니다.

## 개요

프론트엔드(Vue.js)와 백엔드(Spring Boot)는 서로 다른 포트에서 실행되기 때문에 CORS(Cross-Origin Resource Sharing) 설정이 필요합니다.

- Vue.js 개발 서버: `http://localhost:5173` (기본 포트)
- Spring Boot 백엔드: `http://localhost:8080`

## 백엔드 CORS 설정

백엔드에서는 다음과 같이 CORS를 설정했습니다:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
```

## 프론트엔드 API 통신 설정

Vue.js에서 백엔드 API와 통신하기 위해 Axios를 사용하는 것을 권장합니다.

### 1. Axios 설치

```bash
npm install axios
```

### 2. API 기본 설정

`src/api/index.js` 파일을 생성하고 다음과 같이 설정합니다:

```javascript
import axios from 'axios';

// API 기본 설정
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // 쿠키 및 인증 정보 전송을 위해 필요
});

export default api;
```

### 3. API 모듈 예시

각 API 기능별로 모듈을 만들어 사용하는 것이 좋습니다. 예를 들어, 사용자 관련 API:

```javascript
// src/api/user.js
import api from './index';

export const userApi = {
  // 로그인
  login(userData) {
    return api.post('/user/login', userData);
  },

  // 회원가입
  register(userData) {
    return api.post('/user/register', userData);
  },

  // 사용자 정보 조회
  getUserInfo() {
    return api.get('/user/me');
  },

  // 사용자 정보 수정
  updateUserInfo(userData) {
    return api.put('/user/me', userData);
  }
};
```

### 4. Vue 컴포넌트에서 API 사용 예시

```javascript
<script>
import { userApi } from '@/api/user';

export default {
  data() {
    return {
      user: null,
      error: null
    }
  },
  methods: {
    async fetchUserData() {
      try {
        const response = await userApi.getUserInfo();
        this.user = response.data;
      } catch (error) {
        this.error = '사용자 정보를 불러오는데 실패했습니다.';
        console.error(error);
      }
    }
  },
  mounted() {
    this.fetchUserData();
  }
};
</script>
```

## 주의사항

1. **인증 토큰 관리**: JWT 등의 인증 토큰을 사용할 경우, 요청 헤더에 포함시켜야 합니다.

```javascript
// 인증 토큰을 요청 헤더에 추가하는 인터셉터
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);
```

2. **에러 처리**: API 요청 실패 시 적절한 에러 처리가 필요합니다.

```javascript
// 응답 인터셉터로 에러 처리
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      // 인증 오류 처리 (예: 로그인 페이지로 리다이렉트)
      router.push('/login');
    }
    return Promise.reject(error);
  }
);
```

3. **환경 변수 사용**: 개발/운영 환경에 따라 API 주소가 달라질 수 있으므로, 환경 변수를 사용하는 것이 좋습니다.

```javascript
// .env.development 파일
// VUE_APP_API_URL=http://localhost:8080/api

// .env.production 파일
// VUE_APP_API_URL=https://api.yourdomain.com/api

// api/index.js
const api = axios.create({
  baseURL: process.env.VUE_APP_API_URL,
  // ...
});
```

이 설정을 통해 Vue.js 프론트엔드와 Spring Boot 백엔드 간의 원활한 통신이 가능합니다.
