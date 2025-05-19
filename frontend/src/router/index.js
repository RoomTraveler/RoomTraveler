import { createRouter, createWebHistory } from "vue-router";
// import accommodation from '@/router/accommodation.js'
// import plan from '@/router/plan.js'

// 홈 페이지 컴포넌트 - 지연 로딩 적용
const Home = () => import("../views/Home.vue");
const Accommodation = () => import("../views/accommodation/AccommodationList.vue");
const AccommodationDetail = () => import("../views/accommodation/AccommodationDetail.vue");
const Plan = () => import("../views/plan/Plan.vue");
const PlanAlone = () => import("../views/plan/PlanAlone.vue");
const PlanDetail = () => import("../views/plan/PlanDetail.vue");
const PlanPublic = () => import("../views/plan/PlanPublic.vue");
const Attraction = () => import("../views/attraction/Attraction.vue");
const User = () => import("../views/user/UserProfile.vue");
const Login = () => import("../views/user/Login.vue");
const Register = () => import("../views/user/Register.vue");
const NotFound = () => import("../views/NotFound.vue");
const ApiTest = () => import("../components/ApiTest.vue");

const RoomDetail = () => import("../views/accommodation/RoomDetail.vue"); // 파일명 맞춰서

// 관리자 페이지 컴포넌트
const AdminDashboard = () => import("../views/admin/Dashboard.vue");
const AdminUsers = () => import("../views/admin/Users.vue");
const AdminAccommodations = () => import("../views/admin/AdminAccommodations.vue");
const AdminRegions = () => import("../views/admin/Regions.vue");
const AdminApiConfig = () => import("../views/admin/ApiConfig.vue");

// 에러 페이지 컴포넌트
const AccessDenied = () => import("../views/error/AccessDenied.vue");

// 라우트 정의
const routes = [
  {
    path: "/",
    redirect: "/accommodation",
    meta: { title: "숙소 홈 - Room Traveler" },
  },
  {
    path: "/api-test",
    name: "ApiTest",
    component: ApiTest,
    meta: { title: "API 테스트 - Room Traveler" },
  },
  {
    path: "/accommodation",
    name: "Accommodation",
    component: Accommodation,
    meta: { title: "숙소 목록 - Room Traveler" },
  },
  {
    path: "/accommodation/list",
    name: "AccommodationList",
    component: Accommodation,
    meta: { title: "숙소 목록 - Room Traveler" },
  },
  {
    path: "/accommodation/:id",
    name: "AccommodationDetail",
    component: AccommodationDetail,
    props: true,
    meta: { title: "숙소 상세 - Room Traveler" },
  },
  {
    path: "/plan",
    name: "Plan",
    component: Plan,
    meta: { title: "여행 계획 - Room Traveler" },
  },
  { path: "/plan/alone", component: PlanAlone },
  // { path: '/plan/together', component: PlanTogetherPage },
  {
    path: "/attractions/:id",
    name: "AttractionDetail",
    component: Attraction,
  },
  {
    path: "/plans/:planId",
    name: "planDetail",
    component: PlanDetail,
  },
  { path: "/plans/public/:token", component: PlanPublic },
  {
    path: "/accommodation/room/:roomId",
    name: "RoomDetail",
    component: RoomDetail,
    props: true,
    meta: { title: "객실 상세 - Room Traveler" },
  },
  {
    path: "/user",
    name: "User",
    component: User,
    meta: { title: "사용자 프로필 - Room Traveler", requiresAuth: true },
  },
  {
    path: "/login",
    name: "Login",
    component: Login,
    meta: { title: "로그인 - Room Traveler" },
  },
  {
    path: "/register",
    name: "Register",
    component: Register,
    meta: { title: "회원가입 - Room Traveler" },
  },
  // 관리자 페이지 라우트
  {
    path: "/admin",
    name: "AdminDashboard",
    component: AdminDashboard,
    meta: { title: "관리자 대시보드 - Room Traveler", requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/admin/users",
    name: "AdminUsers",
    component: AdminUsers,
    meta: { title: "사용자 관리 - Room Traveler", requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/admin/accommodations",
    name: "AdminAccommodations",
    component: AdminAccommodations,
    meta: { title: "숙소 관리 - Room Traveler", requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/admin/regions",
    name: "AdminRegions",
    component: AdminRegions,
    meta: { title: "지역 데이터 관리 - Room Traveler", requiresAuth: true, requiresAdmin: true },
  },
  {
    path: "/admin/api-config",
    name: "AdminApiConfig",
    component: AdminApiConfig,
    meta: { title: "API 설정 관리 - Room Traveler", requiresAuth: true, requiresAdmin: true },
  },

  // 에러 페이지 라우트
  {
    path: "/error/access-denied",
    name: "AccessDenied",
    component: AccessDenied,
    props: (route) => ({ message: route.query.message }),
    meta: { title: "접근 거부 - Room Traveler" },
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: NotFound,
    meta: { title: "페이지를 찾을 수 없음 - Room Traveler" },
  },
];

// 라우터 인스턴스 생성
const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 전역 네비게이션 가드 - 페이지 제목 설정 및 인증 확인
router.beforeEach((to, from, next) => {
  // 페이지 제목 설정
  document.title = to.meta.title || "Room Traveler";

  // 인증이 필요한 페이지 처리
  if (to.meta.requiresAuth) {
    // 인증 확인 로직
    // const isAuthenticated = store.getters.isAuthenticated;
    const isAuthenticated = localStorage.getItem("user") !== null;

    if (!isAuthenticated) {
      next({ name: "Login", query: { redirect: to.fullPath } });
      return;
    }

    // 관리자 권한이 필요한 페이지 처리
    if (to.meta.requiresAdmin) {
      // 관리자 권한 확인 로직
      // const isAdmin = store.getters.isAdmin;
      const user = JSON.parse(localStorage.getItem("user") || "{}");
      const isAdmin = user.role === "ADMIN";

      if (!isAdmin) {
        // 관리자가 아닌 경우 접근 거부 페이지로 리다이렉트
        next({
          path: "/error/access-denied",
          query: { message: "관리자만 접근할 수 있는 페이지입니다." },
        });
        return;
      }
    }

    next();
  } else {
    next();
  }
});

export default router;
