// router/notification.js
const NotificationList = () => import("../views/notification/NotificationList.vue");

export const notificationRoutes = [
  { path: "/notification/my", name: "NotificationList", component: NotificationList, meta: { title: "알림센터" } },
];
