const AdminDashboard = () => import("../views/admin/Dashboard.vue");
const AdminUsers = () => import("../views/admin/Users.vue");
const AdminAccommodations = () => import("../views/admin/AdminAccommodations.vue");

export const adminRoutes = [
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
];
