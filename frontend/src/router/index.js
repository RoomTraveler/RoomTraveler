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

const NotFound = () => import("../views/NotFound.vue");
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
  ...planRoutes,
  ...eventRoutes,

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
    const userStr = sessionStorage.getItem("user");

    if (!userStr) {
      next({ name: "Login", query: { redirect: to.fullPath } });
      return;
    }

    const user = JSON.parse(sessionStorage.getItem("user") || "{}");
    const role = user.user?.role;

    //1.관리자 권한만 필요한 경우
    if (to.meta.requiresAdmin) {
      if (role !== 'ADMIN') {
        next({
          path: "/error/access-denied",
          query: { message: "관리자만 접근할 수 있는 페이지입니다." },
        });
        return;
      }
    }


    // 호스트나 어드민 권한 필요
    if (to.meta.requiresHost) {
      if (!(role === 'HOST' || role === 'ADMIN')) {
        next({
          path: "/error/access-denied",
          query: { message: "호스트 또는 관리자만 접근할 수 있는 페이지입니다." },
        });
        return;
      }
    }

    // 호스트만 (ONLY)
    if (to.meta.requiresHostOnly) {
      if (role !== 'HOST') {
        next({
          path: "/error/access-denied",
          query: { message: "호스트만 접근할 수 있는 페이지입니다." },
        });
        return;
      }
    }

    //인증 ,권한 다 통과한 경우
    next();
  } else {
    //인증 필요 없는 페이지
    next();
  }
});

export default router;
