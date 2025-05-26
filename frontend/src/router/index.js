import { createRouter, createWebHistory } from "vue-router";

import { accommodationRoutes } from "./accommodation";
import { paymentRoutes } from "./payment";
import { reviewRoutes } from "./review";
import { notificationRoutes } from "./notification";
import { commonRoutes } from "./common";
import { userRoutes } from "./user";
import { adminRoutes } from "./admin";
import { reservationRoutes } from "./reservation";
import { planRoutes } from "./plan";
import { eventRoutes } from "./event";
import { hostRoutes } from "./host";

const NotFound = () => import("../views/NotFound.vue");
// 에러 페이지 컴포넌트
const AccessDenied = () => import("../views/error/AccessDenied.vue");

// 라우트 정의
export const routes = [
  // 필요한 모든 라우트 배열 합치기
  ...commonRoutes,
  ...accommodationRoutes,
  ...paymentRoutes,
  ...reviewRoutes,
  ...notificationRoutes,
  ...userRoutes,
  ...adminRoutes,
  ...reservationRoutes,
  ...planRoutes,
  ...eventRoutes,
  ...hostRoutes,

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
// 전역 네비게이션 가드 - 페이지 제목 설정 및 인증/권한 확인
router.beforeEach((to, from, next) => {
  // 1. 페이지 제목 설정
  document.title = to.meta.title || "Room Traveler";

  // 2. 인증이 필요한 페이지 처리
  if (to.meta.requiresAuth) {
    const userStr = sessionStorage.getItem("user");

    // 로그인 정보 없음 -> 로그인 페이지로 리다이렉트
    if (!userStr) {
      next({ name: "Login", query: { redirect: to.fullPath } });
      return;
    }

    // 로그인 정보에서 role 추출
    const user = JSON.parse(userStr);
    const role = user.user?.role;

    // 3. 권한(role) 체크 (roles 배열이 존재하면)
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
      if (!to.meta.roles.includes(role)) {
        next({
          path: "/error/access-denied",
          query: { message: "접근 권한이 없는 페이지입니다." },
        });
        return;
      }
    }

    // 권한/인증 통과
    next();
  } else {
    // 인증 필요 없는 페이지
    next();
  }
});

export default router;
