import { createRouter, createWebHistory } from 'vue-router';

// 홈 페이지 컴포넌트 - 지연 로딩 적용
const Home = () => import('../views/Home.vue');
const Accommodation = () => import('../views/accommodation/AccommodationList.vue');
const AccommodationDetail = () => import('../views/accommodation/AccommodationDetail.vue');
const User = () => import('../views/user/UserProfile.vue');
const Login = () => import('../views/user/Login.vue');
const Register = () => import('../views/user/Register.vue');
const NotFound = () => import('../views/NotFound.vue');

// 라우트 정의
const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: '홈 - Room Traveler' }
  },
  {
    path: '/accommodation',
    name: 'Accommodation',
    component: Accommodation,
    meta: { title: '숙소 목록 - Room Traveler' }
  },
  {
    path: '/accommodation/:id',
    name: 'AccommodationDetail',
    component: AccommodationDetail,
    props: true,
    meta: { title: '숙소 상세 - Room Traveler' }
  },
  {
    path: '/user',
    name: 'User',
    component: User,
    meta: { title: '사용자 프로필 - Room Traveler', requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '로그인 - Room Traveler' }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '회원가입 - Room Traveler' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { title: '페이지를 찾을 수 없음 - Room Traveler' }
  }
];

// 라우터 인스턴스 생성
const router = createRouter({
  history: createWebHistory(),
  routes
});

// 전역 네비게이션 가드 - 페이지 제목 설정 및 인증 확인
router.beforeEach((to, from, next) => {
  // 페이지 제목 설정
  document.title = to.meta.title || 'Room Traveler';
  
  // 인증이 필요한 페이지 처리 (나중에 구현)
  if (to.meta.requiresAuth) {
    // 여기에 인증 확인 로직 추가
    // const isAuthenticated = store.getters.isAuthenticated;
    const isAuthenticated = localStorage.getItem('user') !== null;
    
    if (!isAuthenticated) {
      next({ name: 'Login', query: { redirect: to.fullPath } });
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router;