// router/payment.js
const PaymentResult = () => import("../views/payment/PaymentResult.vue");

export const paymentRoutes = [
  { path: "/payment/result", name: "PaymentResult", component: PaymentResult, meta: { title: "결제 완료" } },
  // 결제 관련 라우트 추가 가능
];
