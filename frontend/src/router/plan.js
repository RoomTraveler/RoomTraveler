import MyPlans from "@/views/plan/MyPlans.vue";
import SharedPlans from "@/views/plan/SharedPlans.vue";
import Plan from "@/views/plan/Plan.vue";
import PlanAlone from "@/views/plan/PlanAlone.vue";
import PlanDetail from "@/views/plan/PlanDetail.vue";
import PlanPublic from "@/views/plan/PlanPublic.vue";
import Attraction from "@/views/attraction/Attraction.vue";
import MyAttractions from "@/views/attraction/MyAttractions.vue";
import SearchResult from "@/views/attraction/SearchResult.vue";
import SquadList from "@/views/plan/SquadList.vue";
import PlanTogether from "@/views/plan/PlanTogether.vue";
import PlansMe from '@/views/plan/PlansMe.vue';

export const planRoutes = [
  { path: "/plans/public/:token", component: PlanPublic, meta: { title: "공개 여행 계획 - Room Traveler" } },
  {
    path: "/plans",
    name: "plans",
    component: MyPlans,
    meta: {requiresAuth: true, roles: ["USER", "HOST", "ADMIN"] },
  },
  {
    path: "/plans/me",
    name: "plansMe",
    component: PlansMe,
    meta: {requiresAuth: true, roles: ["USER", "HOST", "ADMIN"] },
  },
  {
    path: "/plan",
    name: "Plan",
    component: Plan,
    meta: { title: "여행 계획 - Room Traveler" },
  },
  { path: "/plan/alone", component: PlanAlone, meta: {requiresAuth: true, roles: ["USER", "HOST", "ADMIN"] },},
  { path: "/plan/room", component: SquadList, meta: {requiresAuth: true, roles: ["USER", "HOST", "ADMIN"] },},
  { path: "/plan/together", component: PlanTogether, meta: {requiresAuth: true, roles: ["USER", "HOST", "ADMIN"] }},
  {
    path: "/attractions/:id",
    name: "AttractionDetail",
    component: Attraction,
    meta: {requiresAuth: true, roles: ["USER", "HOST", "ADMIN"] }
  },
  {
    path: "/attractions",
    name: "attractions",
    component: MyAttractions,
    meta: {requiresAuth: true, roles: ["USER", "HOST", "ADMIN"] }
  },
  {
    path: "/attraction/search",
    name: "attractionSearch",
    component: SearchResult,
    meta: {requiresAuth: true, roles: ["USER", "HOST", "ADMIN"] }
  },
  {
    path: "/plans/:planId",
    name: "planDetail",
    component: PlanDetail,
    meta: {requiresAuth: true, roles: ["USER", "HOST", "ADMIN"] }
  },
  {
    path: "/plans/shared",
    name: "sharedPlans",
    component: SharedPlans,
    meta: {requiresAuth: true, roles: ["USER", "HOST", "ADMIN"] }
  },
];
