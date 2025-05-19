# 방구석 여행자 (RoomTraveler) - 프론트엔드

이 디렉토리는 방구석 여행자 프로젝트의 Vue.js 기반 프론트엔드 애플리케이션을 포함하고 있습니다. 백엔드와 분리된 구조로, Vue 3와 Vite를 사용하여 개발된 SPA(Single Page Application)입니다.

## 프론트엔드 구조

```
frontend/
├── node_modules/     # npm 패키지 (git에서는 무시됨)
├── public/           # 정적 파일
├── src/
│   ├── assets/       # 이미지, 폰트 등 자산 파일
│   ├── components/   # 재사용 가능한 컴포넌트
│   │   └── layout/   # 레이아웃 관련 컴포넌트
│   ├── api/          # API 호출 함수
│   ├── router/       # Vue Router 설정
│   ├── store/        # Pinia 상태 관리
│   └── views/        # 페이지 컴포넌트
│       ├── accommodation/  # 숙소 관련 페이지
│       ├── review/         # 리뷰 관련 페이지
│       └── user/           # 사용자 관련 페이지
├── .env              # 환경 변수
├── package.json      # 의존성 및 스크립트
├── package-lock.json # 의존성 잠금 파일
└── vite.config.mjs    # Vite 설정
```

## 기술 스택

- **Vue.js 3**: 사용자 인터페이스 구축을 위한 프레임워크
- **Vite**: 빠른 개발 서버 및 빌드 도구
- **Pinia**: Vue 3용 상태 관리 라이브러리
- **Vue Router**: SPA 라우팅 라이브러리
- **Axios**: HTTP 클라이언트
- **Bootstrap 5**: UI 컴포넌트 및 스타일링

## 주요 컴포넌트

### 레이아웃 컴포넌트

- `Layout.vue`: 전체 레이아웃을 담당하는 컴포넌트
- `Header.vue`: 헤더 영역을 담당하는 컴포넌트
- `Footer.vue`: 푸터 영역을 담당하는 컴포넌트

### 페이지 컴포넌트

- `Home.vue`: 메인 페이지
- `accommodation/AccommodationList.vue`: 숙소 목록 페이지
- `accommodation/AccommodationDetail.vue`: 숙소 상세 페이지
- `user/Login.vue`: 로그인 페이지
- `user/Register.vue`: 회원가입 페이지
- `review/MyReviews.vue`: 내 리뷰 목록 페이지
- `review/ReviewForm.vue`: 리뷰 작성 페이지
- `review/ReviewEditForm.vue`: 리뷰 수정 페이지
- `review/ReviewList.vue`: 숙소별 리뷰 목록 페이지

## 다음 단계

### 라우팅 설정

Vue Router를 사용하여 다음과 같은 라우팅을 설정해야 합니다:

```javascript
// router/index.js
import { createRouter, createWebHistory } from "vue-router";
import Home from "@/views/Home.vue";
import AccommodationList from "@/views/accommodation/AccommodationList.vue";
import AccommodationDetail from "@/views/accommodation/AccommodationDetail.vue";
import Login from "@/views/user/Login.vue";
import Register from "@/views/user/Register.vue";
import MyReviews from "@/views/review/MyReviews.vue";
import ReviewForm from "@/views/review/ReviewForm.vue";
import ReviewEditForm from "@/views/review/ReviewEditForm.vue";
import ReviewList from "@/views/review/ReviewList.vue";

const routes = [
  {
    path: "/",
    name: "Home",
    component: Home,
  },
  {
    path: "/accommodation/list",
    name: "AccommodationList",
    component: AccommodationList,
  },
  {
    path: "/accommodation/detail/:id",
    name: "AccommodationDetail",
    component: AccommodationDetail,
    props: true,
  },
  {
    path: "/user/login",
    name: "Login",
    component: Login,
  },
  {
    path: "/user/register",
    name: "Register",
    component: Register,
  },
  {
    path: "/review/my-reviews",
    name: "MyReviews",
    component: MyReviews,
    meta: { requiresAuth: true },
  },
  {
    path: "/review/write/:accommodationId",
    name: "ReviewForm",
    component: ReviewForm,
    props: true,
    meta: { requiresAuth: true },
  },
  {
    path: "/review/edit/:reviewId",
    name: "ReviewEditForm",
    component: ReviewEditForm,
    props: true,
    meta: { requiresAuth: true },
  },
  {
    path: "/review/list/:accommodationId",
    name: "ReviewList",
    component: ReviewList,
    props: true,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 인증이 필요한 라우트에 대한 네비게이션 가드
router.beforeEach((to, from, next) => {
  const isLoggedIn = store.state.user.isLoggedIn;

  if (to.matched.some((record) => record.meta.requiresAuth) && !isLoggedIn) {
    next({ name: "Login", query: { redirect: to.fullPath } });
  } else {
    next();
  }
});

export default router;
```

### 상태 관리 설정

Vuex를 사용하여 다음과 같은 상태 관리를 설정해야 합니다:

```javascript
// store/index.js
import { createStore } from 'vuex'
import user from './modules/user'
import accommodation from './modules/accommodation'
import review from './modules/review'
import reservation from './modules/reservation'

export default createStore({
  modules: {
    user,
    accommodation,
    review,
    reservation
  }
})

// store/modules/user.js
import axios from 'axios'

export default {
  namespaced: true,
  state: {
    isLoggedIn: false,
    user: null,
    token: null
  },
  mutations: {
    SET_USER(state, user) {
      state.user = user
      state.isLoggedIn = !!user
    },
    SET_TOKEN(state, token) {
      state.token = token
    }
  },
  actions: {
    async loginUser({ commit }, credentials) {
      const response = await axios.post('/api/user/login', credentials)
      const { user, token } = response.data

      commit('SET_USER', user)
      commit('SET_TOKEN', token)

      localStorage.setItem('token', token)

      return user
    },
    async registerUser({ commit }, userData) {
      const response = await axios.post('/api/user/register', userData)
      return response.data
    },
    async logout({ commit }) {
      await axios.post('/api/user/logout')

      commit('SET_USER', null)
      commit('SET_TOKEN', null)

      localStorage.removeItem('token')
    },
    async checkAuth({ commit }) {
      try {
        const token = localStorage.getItem('token')

        if (!token) return

        const response = await axios.get('/api/user/me')
        commit('SET_USER', response.data)
        commit('SET_TOKEN', token)
      } catch (error) {
        commit('SET_USER', null)
        commit('SET_TOKEN', null)
        localStorage.removeItem('token')
      }
    }
  }
}

// 다른 모듈(accommodation, review, reservation)도 유사한 방식으로 구현
```

### API 연동 설정

Axios를 사용하여 다음과 같은 API 연동을 설정해야 합니다:

```javascript
// plugins/axios.js
import axios from "axios";
import store from "@/store";
import router from "@/router";

// API 기본 URL 설정
axios.defaults.baseURL = import.meta.env.VITE_API_URL || "/api";

// 요청 인터셉터
axios.interceptors.request.use(
  (config) => {
    const token = store.state.user.token;

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터
axios.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      store.dispatch("user/logout");
      router.push("/user/login");
    }

    return Promise.reject(error);
  }
);

export default axios;
```

## 실행 방법

1. 의존성 설치:

```bash
rmdir /s /q node_modules
del package-lock.json
npm install vite@5 @vitejs/plugin-vue@5 -D
npm install tailwindcss@latest @tailwindcss/vite@latest -D
npm install
npm install @vuepic/vue-datepicker
npm install @vuepic/vue-datepicker date-fns
npm install element-plus
npm install vuedraggable@next
npm install @vueuse/motion
npm install bootstrap-icons
```

2. 개발 서버 실행:

```bash
# 반드시 frontend 디렉토리에서 실행해야 합니다
npm run dev
```

3. 프로덕션 빌드:

````bash
npm run build
```f

> **주의**: `npm run dev` 명령은 반드시 frontend 디렉토리에서 실행해야 합니다. backend 디렉토리에서 실행하면 작동하지 않습니다.
````
