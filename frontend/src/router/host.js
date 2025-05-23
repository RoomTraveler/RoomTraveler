const HostAccommodationStats = () => import("../views/host/AccommodationStats.vue");
const HostAccommodationList = () => import("../views/host/HostAccommodations.vue");
const HostDashboard = () => import("../views/host/HostDashboard.vue");
const HostIndex = () => import("../views/host/HostIndex.vue");
const HostList = () => import("../views/host/HostList.vue");
const HostRegister = () => import("../views/host/HostRegistForm.vue");
const HostReservations = () => import("../views/host/HostReservations.vue");
const HostReviews = () => import("../views/host/HostReviews.vue");

export const hostRoutes = [
  //호스트의 모든 숙소 정보
  {
    path: "/host/accommodation/stats",
    name: "HostAccommodationStats",
    component: HostAccommodationStats,
    meta: {
      title: "호스트 숙소 정보",
      requiresAuth: true,
      roles: ["HOST", "ADMIN"],
    },
  },
  {
    path: "/host/accommodation/list",
    name: "HostAccommodationList",
    component: HostAccommodationList,
    meta: { title: "호스트 숙소 목록", requiresAuth: true, roles: ["HOST", "ADMIN"] },
  },
  {
    path: "/host/dashboard",
    name: "HostDashboard",
    component: HostDashboard,
    meta: { title: "호스트 대시보드", requiresAuth: true, roles: ["HOST", "ADMIN"] },
  },
  {
    path: "/host/index",
    name: "HostIndex",
    component: HostIndex,
    meta: { title: "호스트 메인" },
  },
  {
    path: "/host/list",
    name: "HostList",
    component: HostList,
    meta: { title: "호스트 목록", requiresAuth: true, roles: ["HOST", "ADMIN"] },
  },
  {
    path: "/host/register",
    name: "HostRegister",
    component: HostRegister,
    meta: { title: "호스트 등록" },
  },
  //모든 숙소의 예약관리.
  //특정 숙소의 예약관리가 필요하면 따로 페이지를 만들어야함
  {
    path: "/host/reservations",
    name: "HostReservations",
    component: HostReservations,
    meta: { title: "호스트 예약 관리", requiresAuth: true, roles: ["HOST", "ADMIN"] },
  },
  //모든 숙소의 리뷰관리.
  //특정 숙소의 리뷰관리가 필요하면 따로 페이지를 만들어야함
  {
    path: "/host/reviews",
    name: "HostReviews",
    component: HostReviews,
    meta: { title: "호스트 리뷰 관리", requiresAuth: true, roles: ["HOST", "ADMIN"] },
  },
];
