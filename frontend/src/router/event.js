import Plan from "@/views/plan/Plan.vue";

const Event = () => import('@/views/event/EventList.vue');
const EventDetail = () => import('@/views/event/EventDetail.vue');
//둘이 form이 같아서 같은 컴포넌트로 사용
const EventCreate = () => import('@/views/event/EventCreate.vue');
const EventUpdate = () => import('@/views/event/EventCreate.vue');

export const eventRoutes = [
    {
        path: '/event',
        name: 'Event',
        component: Event,
        meta: { title: '이벤트 - Room Traveler' },
    },
    {
        path: '/event/update/:eventId',
        name: 'EventUpdate',
        component: EventUpdate,
        meta: { title: '이벤트 수정 - Room Traveler' , requiresAuth: true, requiresAdmin: true },
    },
    {
        path: "/event/detail/:eventId",
        name: "EventDetail",
        component: EventDetail,
        meta: { title: "이벤트 조회 - Room Traveler" },
    },
    {
        path: '/event/create',
        name: 'EventCreate',
        component: EventCreate,
        meta: { title: '이벤트 생성 - Room Traveler' , requiresAuth: true, requiresAdmin: true },
    }
]