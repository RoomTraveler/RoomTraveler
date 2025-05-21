const User = () => import("../views/user/UserProfile.vue");
const Login = () => import("../views/user/Login.vue");
const Register = () => import("../views/user/Register.vue");

export const userRoutes = [
  {
    path: "/user/profile",
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
];
