// router/common.js
const Home = () => import("../views/HomeView.vue");

export const commonRoutes = [{ path: "/", name: "Home", component: Home, meta: { title: "홈 | 숙소예약" } }];
