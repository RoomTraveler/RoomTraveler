import ReservationForm from "../views/reservation/ReservationForm.vue";
import ReservationDetail from "../views/reservation/ReservationDetail.vue";
import MyReservations from "../views/reservation/MyReservations.vue";

export const reservationRoutes = [
  {
    path: "/reservation/form",
    component: ReservationForm,
    meta: { title: "예약 페이지", requiresAuth: true },
  },
  {
    path: "/reservation/detail/:id",
    component: ReservationDetail,
    meta: { title: "예약 상세 페이지", requiresAuth: true },
  },
  {
    path: "/reservation/my-reservations",
    component: MyReservations,
    meta: { title: "나의 예약", requiresAuth: true },
  },
];
