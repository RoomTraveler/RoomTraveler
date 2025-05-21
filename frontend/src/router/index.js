import { createRouter, createWebHistory } from "vue-router";

import { accommodationRoutes } from "./accommodation";
import { paymentRoutes } from "./payment";
import { reviewRoutes } from "./review";
import { notificationRoutes } from "./notification";
import { commonRoutes } from "./common";
import { userRoutes } from "./user";
import { adminRoutes } from "./admin";
import { reservationRoutes } from "./reservation";

const Plan = () => import("../views/plan/Plan.vue");
const PlanAlone = () => import("../views/plan/PlanAlone.vue");
const PlanDetail = () => import("../views/plan/PlanDetail.vue");
const PlanPublic = () => import("../views/plan/PlanPublic.vue");
const Attraction = () => import("../views/attraction/Attraction.vue");

const NotFound = () => import("../views/NotFound.vue");
const ApiTest = () => import("../components/ApiTest.vue");

// 에러 페이지 컴포넌트
const AccessDenied = () => import("../views/error/AccessDenied.vue");

// 라우트 정의
const routes = [
  // 필요한 모든 라우트 배열 합치기
  ...commonRoutes,
  ...accommodationRoutes,
  ...paymentRoutes,
  ...reviewRoutes,
  ...notificationRoutes,
  ...userRoutes,
  ...adminRoutes,
  ...reservationRoutes,

  { path: "/plans/public/:token", component: PlanPublic, meta: { title: "공개 여행 계획 - Room Traveler" } },
  {
    path: "/api-test",
    name: "ApiTest",
    component: ApiTest,
    meta: { title: "API 테스트 - Room Traveler" },
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
