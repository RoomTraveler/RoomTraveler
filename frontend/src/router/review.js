// router/review.js
const ReviewForm = () => import("../views/review/ReviewForm.vue");
const ReviewEditForm = () => import("../views/review/ReviewEditForm.vue");
const MyReviews = () => import("../views/review/MyReviews.vue");

export const reviewRoutes = [
  {
    path: "/review/write/:accommodationId",
    name: "ReviewForm",
    component: ReviewForm,
    props: true,
    meta: { title: "리뷰 작성", requiresAuth: true },
  },
  {
    path: "/review/edit/:reviewId",
    name: "ReviewEditForm",
    component: ReviewEditForm,
    props: true,
    meta: { title: "리뷰 수정", requiresAuth: true },
  },
  {
    path: "/review/my",
    name: "MyReviews",
    component: MyReviews,
    meta: { title: "내 리뷰 관리", requiresAuth: true },
  },
];
