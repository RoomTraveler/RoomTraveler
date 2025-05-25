const AdminDashboard = () => import("../views/admin/AdminDashboard.vue");
const AdminUsers = () => import("../views/admin/AdminUsers.vue");
const AdminAccommodations = () => import("../views/admin/AdminAccommodations.vue");
const AdminHosts = () => import("../views/admin/AdminHosts.vue");
const AdminPendingAccommodations = () => import("../views/admin/AdminPendingAccommodations.vue");
const AdminAllHosts = () => import("../views/admin/AdminAllHosts.vue");

export const adminRoutes = [
  // 관리자 페이지 라우트
  {
    path: "/admin",
    name: "AdminDashboard",
    component: AdminDashboard,
    meta: {
      title: "관리자 대시보드 - Room Traveler",
      requiresAuth: true,
      roles: ["ADMIN"],
    },
  },
  {
    path: "/admin/users",
    name: "AdminUsers",
    component: AdminUsers,
    meta: { title: "사용자 관리 - Room Traveler", requiresAuth: true, roles: ["ADMIN"] },
  },
  {
    path: "/admin/accommodations",
    name: "AdminAccommodations",
    component: AdminAccommodations,
    meta: { title: "숙소 관리 - Room Traveler", requiresAuth: true, roles: ["ADMIN"] },
  },
  {
    path: "/admin/hosts",
    name: "AdminHosts",
    component: AdminHosts,
    meta: { title: "호스트 신청 관리 - Room Traveler", requiresAuth: true, roles: ["ADMIN"] },
  },
  {
    path: "/admin/accommodations/pending",
    name: "AdminPendingAccommodations",
    component: AdminPendingAccommodations,
    meta: { title: "숙소 신청 관리 - Room Traveler", requiresAuth: true, roles: ["ADMIN"] },
  },
  {
    path: "/admin/hosts/all",
    name: "AdminAllHosts",
    component: AdminAllHosts,
    meta: { title: "전체 호스트 관리 - Room Traveler", requiresAuth: true, roles: ["ADMIN"] },
  },
];
