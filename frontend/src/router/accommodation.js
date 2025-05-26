const AccommodationHome = () => import("../views/accommodation/AccommodationHome.vue");
const AccommodationList = () => import("../views/accommodation/AccommodationList.vue");
const AccommodationDetail = () => import("../views/accommodation/AccommodationDetail.vue");
const RoomDetail = () => import("../views/accommodation/RoomDetail.vue");
const AccommodationCart = () => import("../views/cart/Cart.vue");
const AccommodationFavorite = () => import("../views/accommodation/AccommodationFavoriteList.vue");
const AccommodationCartCheckout = () => import("../views/cart/CartCheckout.vue");

export const accommodationRoutes = [
  { path: "/accommodation", name: "AccommodationHome", component: AccommodationHome, meta: { title: "숙소 홈" } },
  {
    path: "/accommodation/list",
    name: "AccommodationList",
    component: AccommodationList,
    meta: { title: "숙소 찾기" },
  },
  {
    path: "/accommodation/:id",
    name: "AccommodationDetail",
    component: AccommodationDetail,
    props: true,
    meta: { title: "숙소 상세" },
  },
  {
    path: "/room/:roomId",
    name: "RoomDetail",
    component: RoomDetail,
    props: true,
    meta: { title: "객실 상세" },
  },
  {
    path: "/accommodation/cart",
    name: "AccommodationCart",
    component: AccommodationCart,
    meta: { title: "장바구니", requiresAuth: true },
  },
  {
    path: "/accommodation/favorites",
    name: "AccommodationFavorite",
    component: AccommodationFavorite,
    meta: { title: "찜한 숙소", requiresAuth: true },
  },
  {
    path: "/accommodation/cart/checkout",
    name: "AccommodationCartCheckout",
    component: AccommodationCartCheckout,
    meta: { title: "예약 확인", requiresAuth: true },
  },
];
