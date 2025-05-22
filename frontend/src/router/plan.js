import MyPlans from "@/views/plan/MyPlans.vue";
import SharedPlans from "@/views/plan/SharedPlans.vue";
import LikedAttractions from "@/views/attraction/LikedAttractions.vue";
import Plan from "@/views/plan/Plan.vue";
import PlanAlone from "@/views/plan/PlanAlone.vue";
import PlanDetail from "@/views/plan/PlanDetail.vue";
import PlanPublic from "@/views/plan/PlanPublic.vue";
import Attraction from "@/views/attraction/Attraction.vue";
import MyAttractions from "@/views/attraction/MyAttractions.vue";
import SearchResult from "@/views/attraction/SearchResult.vue";
import SquadList from "@/views/plan/SquadList.vue";
import PlanTogether from "@/views/plan/PlanTogether.vue";

export const planRoutes = [
  { path: "/plans/public/:token", component: PlanPublic, meta: { title: "공개 여행 계획 - Room Traveler" } },
  {
    path: "/plans",
    name: "plans",
    component: MyPlans,
  },
  {
    path: "/plan",
    name: "Plan",
    component: Plan,
    meta: { title: "여행 계획 - Room Traveler" },
  },
  { path: "/plan/alone", component: PlanAlone },
  { path: "/plan/room", component: SquadList },
  { path: "/plan/together", component: PlanTogether },
  {
    path: "/attractions/:id",
    name: "AttractionDetail",
    component: Attraction,
  },
  {
    path: "/attractions",
    name: "attractions",
    component: MyAttractions,
  },
  {
    path: "/attraction/search",
    name: "attractionSearch",
    component: SearchResult,
  },
  {
    path: "/plans/:planId",
    name: "planDetail",
    component: PlanDetail,
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
