const HostIndex = () => import('@/views/host/HostIndex.vue');
const HostDashboard = () => import('@/views/host/HostDashboard.vue');
const HostAccommodations = () => import('@/views/host/HostAccommodations.vue');
const HostAccommodationForm = () => import('@/views/host/HostAccommodationForm.vue');
const HostDetail = () => import('@/views/host/HostDetail.vue');
const HostReservations = () => import('@/views/host/HostReservations.vue');
const HostReviews = () => import('@/views/host/HostReviews.vue');
const HostRegistForm = () => import('@/views/host/HostRegistForm.vue');

// 객실 관리 컴포넌트 임포트
const HostRoomList = () => import('@/views/host/rooms/HostRoomList.vue');
const HostRoomForm = () => import('@/views/host/rooms/HostRoomForm.vue');

export const hostRoutes = [
  {
    path: '/host/register',
    name: 'HostRegister',
    component: HostRegistForm,
    meta: { title: '호스트 등록 신청', requiresAuth: true, roles: ['USER', 'ADMIN'] }
  },
  {
    path: '/host',
    component: HostIndex,
    meta: { requiresAuth: true, roles: ['HOST', 'ADMIN'] },
    children: [
      {
        path: '',
        name: 'HostDashboardRedirect',
        redirect: '/host/dashboard',
      },
      {
        path: 'dashboard',
        name: 'HostDashboard',
        component: HostDashboard,
        meta: { title: '호스트 대시보드' }
      },
      {
        path: 'accommodations',
        name: 'HostAccommodations',
        component: HostAccommodations,
        meta: { title: '내 숙소 관리' }
      },
      {
        path: 'accommodations/new',
        name: 'HostAccommodationNew',
        component: HostAccommodationForm,
        meta: { title: '새 숙소 등록' }
      },
      {
        path: 'accommodations/:accommodationId/edit',
        name: 'HostAccommodationEdit',
        component: HostAccommodationForm,
        props: true,
        meta: { title: '숙소 정보 수정' }
      },
      // --- 객실 관리 라우트 시작 ---
      {
        path: 'accommodations/:accommodationId/rooms',
        name: 'HostRoomList',
        component: HostRoomList,
        props: true,
        meta: { title: '객실 관리' }
      },
      {
        path: 'accommodations/:accommodationId/rooms/new',
        name: 'HostRoomNew',
        component: HostRoomForm,
        props: true,
        meta: { title: '새 객실 등록' }
      },
      {
        path: 'rooms/:roomId/edit',
        name: 'HostRoomEdit',
        component: HostRoomForm,
        props: true,
        meta: { title: '객실 정보 수정' }
      },
      // --- 객실 관리 라우트 종료 ---
      {
        path: 'accommodations/:accommodationId',
        name: 'HostAccommodationDetail',
        component: HostDetail,
        props: true,
        meta: { title: '숙소 상세 정보' }
      },
      {
        path: 'reservations',
        name: 'HostReservations',
        component: HostReservations,
        meta: { title: '예약 관리' }
      },
      {
        path: 'reviews',
        name: 'HostReviews',
        component: HostReviews,
        meta: { title: '리뷰 관리' }
      },
    ]
  }
];
