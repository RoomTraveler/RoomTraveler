import MyPlans from "@/views/plan/MyPlans.vue";
import SharedPlans from "@/views/plan/SharedPlans.vue";
import LikedAttractions from "@/views/attraction/LikedAttractions.vue";

export default [
  {
    path: "/plans",
    name: "plans",
    component: MyPlans,
  },
  {
    path: "/plans/shared",
    name: "sharedPlans",
    component: SharedPlans,
  },
  {
    path: "/plans/attractions",
    name: "likedAttractions",
    component: LikedAttractions,
  },
];
