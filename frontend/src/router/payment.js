// router/payment.js
const PaymentResult = () => import("../views/payment/PaymentResult.vue");
const PaymentHistory = () => import("../views/payment/PaymentHistory.vue");

export const paymentRoutes = [
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
];
