// router/payment.js
const PaymentForm = () => import("@/views/payment/PaymentForm.vue");
const PaymentResult = () => import("@/views/payment/PaymentResult.vue");
const PaymentHistory = () => import("@/views/payment/PaymentHistory.vue");
const PaymentDetail = () => import("@/views/payment/PaymentDetail.vue"); 
const CancelForm = () => import("@/views/payment/CancelForm.vue");

export const paymentRoutes = [
  {
    path: "/payment/checkout/:reservationId", 
    name: "PaymentForm",
    component: PaymentForm,
    props: true, 
    meta: { title: "결제하기", requiresAuth: true },
  },
  {
    path: "/payment/result", 
    name: "PaymentResult",
    component: PaymentResult,
    meta: { title: "결제 완료", requiresAuth: true }, 
  },
  {
    path: "/payment/history",
    name: "PaymentHistory",
    component: PaymentHistory,
    meta: { title: "결제 내역", requiresAuth: true },
  },
  {
    path: "/payment/detail/:paymentId", 
    name: "PaymentDetail",
    component: PaymentDetail,
    props: true, 
    meta: { title: "결제 상세", requiresAuth: true },
  },
  {
    path: "/payment/cancel/:paymentId", 
    name: "CancelForm",
    component: CancelForm,
    props: true, 
    meta: { title: "결제 취소", requiresAuth: true },
  },
];
