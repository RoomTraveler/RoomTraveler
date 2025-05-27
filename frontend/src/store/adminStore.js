import { defineStore } from 'pinia';
import api from '@/api/index';

export const useAdminStore = defineStore('admin', {
    state: () => ({
        // Dashboard/통계
        typeCounts: {},  // <<< 유형별 숙소 카운트! (추가)
        dashboardStats: {},
        recentAccommodations: [],
        recentUsers: [],
        systemStatus: {},
        monthlyChartData: { labels: [], reservations: [], revenue: [] },
        accommodationTypeChartData: { labels: [], data: [] },

        // Host Applications
        pendingHosts: [],

        // Accommodations
        accommodations: [],
        accommodationTotalPages: 0,
        accommodationCurrentPage: 1,

        // Pending Accommodations (숙소 신청 관리용)
        pendingAccommodations: [],
        pendingAccommodationsTotalPages: 0,
        pendingAccommodationsCurrentPage: 0, // API 응답이 0-based index면 0으로 시작
        pendingAccommodationsTotalElements: 0,

        // All Hosts Management
        allHosts: [],
        allHostsTotalPages: 0,
        allHostsCurrentPage: 0,
        allHostsTotalElements: 0,

        // Users
        users: [],
        userTotalPages: 0,
        userCurrentPage: 1,

        // Regions
        sidos: [],
        guguns: [],
        lastSyncInfo: { date: null, status: '' },
        syncProgress: 0,

        // Common
        loading: false,
        error: null,
        message: null,
    }),
    getters: {
        userRole: (state) => 'ADMIN', // 실제 인증 연동 시 수정
    },
    actions: {
        _setLoading(loading) {
            this.loading = loading;
        },
        _setError(error) {
            this.error = error;
            this.message = null;
        },
        _setMessage(message) {
            this.message = message;
            this.error = null;
        },
        _clearMessages() {
            this.message = null;
            this.error = null;
        },

        // ====== 유형별 숙소 카운트(대시보드) ======
        async fetchAccommodationTypeCounts() {
            this._setLoading(true);
            this._clearMessages();
            try {
                const response = await api.api.get('/api/admin/dashboard/type-counts');
                this.typeCounts = response.data || {};
            } catch (err) {
                this._setError(err.response?.data?.message || '숙소 유형별 통계 조회 실패');
                this.typeCounts = {};
            } finally {
                this._setLoading(false);
            }
        },

        // Host Applications Management
        async fetchPendingHosts() {
            this._setLoading(true);
            this._clearMessages();
            try {
                const response = await api.api.get('/api/admin/hosts/pending');
                this.pendingHosts = response.data;
            } catch (err) {
                this._setError(err.response?.data?.message || '승인 대기 호스트 목록 조회 실패');
                this.pendingHosts = [];
            } finally {
                this._setLoading(false);
            }
        },
        async approveHostApplication(hostUserId) {
            this._setLoading(true);
            this._clearMessages();
            try {
                // ⬇️ POST → PUT 으로 변경!
                const response = await api.api.put(`/api/admin/approve/${hostUserId}`);
                this._setMessage(response.data.message || '호스트 신청 승인 완료');
                await this.fetchPendingHosts();
            } catch (err) {
                this._setError(err.response?.data?.message || '호스트 신청 승인 실패');
                throw err;
            } finally {
                this._setLoading(false);
            }
        },
        async rejectHostApplication({ hostUserId, reason }) {
            this._setLoading(true);
            this._clearMessages();
            try {
                const response = await api.api.put(`/api/admin/reject/${hostUserId}`, { reason });
                this._setMessage(response.data.message || '호스트 신청 거절 완료');
                await this.fetchPendingHosts();
            } catch (err) {
                this._setError(err.response?.data?.message || '호스트 신청 거절 실패');
                throw err;
            } finally {
                this._setLoading(false);
            }
        },

        // Dashboard 전체 집계 (대시보드에서 필요시)
        async fetchDashboardData() {
            this._setLoading(true);
            this._clearMessages();
            try {
                const response = await api.api.get('/api/admin/dashboard');
                const data = response.data;
                this.dashboardStats = data.stats || {};
                this.recentAccommodations = data.recentAccommodations || [];
                this.recentUsers = data.recentUsers || [];
                this.systemStatus = data.systemStatus || {};
                this.monthlyChartData = data.monthlyData || { labels: [], reservations: [], revenue: [] };
                this.accommodationTypeChartData = data.accommodationTypes || { labels: [], data: [] };
            } catch (err) {
                this._setError(err.response?.data?.message || '대시보드 데이터 로딩 실패');
            } finally {
                this._setLoading(false);
            }
        },

        // ====== 숙소 신청 관리 (Pending Accommodations) ======
        async fetchPendingAccommodations(params = { page: 0, size: 10, sortBy: 'createdAt', sortDirection: 'DESC' }) {
            this._setLoading(true);
            this._clearMessages();
            try {
                const response = await api.api.get('/api/admin/accommodations/pending', { params });
                this.pendingAccommodations = response.data.content;
                this.pendingAccommodationsTotalPages = response.data.totalPages;
                this.pendingAccommodationsCurrentPage = response.data.number + 1; 
                this.pendingAccommodationsTotalElements = response.data.totalElements;
                return response.data;
            } catch (err) {
                this._setError(err.response?.data?.message || '승인 대기 숙소 목록 조회 실패');
                this.pendingAccommodations = [];
                this.pendingAccommodationsTotalPages = 0;
                this.pendingAccommodationsCurrentPage = 0;
                this.pendingAccommodationsTotalElements = 0;
            } finally {
                this._setLoading(false);
            }
        },

        async approvePendingAccommodation(accommodationId) {
            this._setLoading(true);
            this._clearMessages();
            try {
                const response = await api.api.post(`/api/admin/accommodations/${accommodationId}/approve`);
                this._setMessage(response.data.message || '숙소 신청이 승인되었습니다.');
                await this.fetchPendingAccommodations({ 
                    page: this.pendingAccommodationsCurrentPage > 0 ? this.pendingAccommodationsCurrentPage - 1 : 0, 
                    size: 10 
                }); 
            } catch (err) {
                this._setError(err.response?.data?.message || '숙소 신청 승인 실패');
                throw err;
            } finally {
                this._setLoading(false);
            }
        },

        async rejectPendingAccommodation({ accommodationId, reason }) {
            this._setLoading(true);
            this._clearMessages();
            if (!reason || reason.trim() === '') {
                this._setError('거절 사유를 입력해야 합니다.');
                this._setLoading(false);
                throw new Error('거절 사유 필요');
            }
            try {
                const response = await api.api.post(`/api/admin/accommodations/${accommodationId}/reject`, { reason });
                this._setMessage(response.data.message || '숙소 신청이 거절되었습니다.');
                await this.fetchPendingAccommodations({ 
                    page: this.pendingAccommodationsCurrentPage > 0 ? this.pendingAccommodationsCurrentPage - 1 : 0, 
                    size: 10 
                });
            } catch (err) {
                this._setError(err.response?.data?.message || '숙소 신청 거절 실패');
                throw err; 
            } finally {
                this._setLoading(false);
            }
        },

        // Accommodations Management
        async fetchAdminAccommodations(params) {
            this._setLoading(true);
            this._clearMessages();
            try {
                const response = await api.api.get('/api/admin/accommodations', { params });
                this.accommodations = response.data.content;
                this.accommodationTotalPages = response.data.totalPages;
                this.accommodationCurrentPage = response.data.number + 1;
                this.accommodationTotalElements = response.data.totalElements;
                return response.data;
            } catch (err) {
                this._setError(err.response?.data?.message || '숙소 목록(관리자) 조회 실패');
                this.accommodations = [];
                throw err;
            } finally {
                this._setLoading(false);
            }
        },
        async updateAccommodationStatus({ accommodationId, status, reason }) {
            this._setLoading(true);
            this._clearMessages();
            try {
                let url = `/api/admin/accommodations/${accommodationId}/status`;
                let payload = { status };
                const response = await api.api.post(url, payload);
                this._setMessage(response.data.message || '숙소 상태 변경 완료');
            } catch (err) {
                this._setError(err.response?.data?.message || '숙소 상태 변경 실패');
                throw err;
            } finally {
                this._setLoading(false);
            }
        },
        async deleteAdminAccommodation(accommodationId) {
            this._setLoading(true);
            this._clearMessages();
            try {
                const response = await api.api.delete(`/api/admin/accommodations/${accommodationId}`);
                this._setMessage(response.data.message || '숙소 삭제 완료');
            } catch (err) {
                this._setError(err.response?.data?.message || '숙소 삭제 실패');
                throw err;
            } finally {
                this._setLoading(false);
            }
        },

        // User Management
        async fetchAdminUsers(params) {
            this._setLoading(true);
            this._clearMessages();
            try {
                const response = await api.api.get('/api/admin/users', { params });
                this.users = response.data.content;
                this.userTotalPages = response.data.totalPages;
                this.userCurrentPage = response.data.number + 1;
                return response.data;
            } catch (err) {
                this._setError(err.response?.data?.message || '사용자 목록 조회 실패');
                this.users = [];
                throw err;
            } finally {
                this._setLoading(false);
            }
        },
        async updateAdminUser(userData) {
            this._setLoading(true);
            this._clearMessages();
            try {
                const response = await api.api.put(`/api/admin/users/${userData.userId}`, userData);
                this._setMessage(response.data.message || '사용자 정보 수정 완료');
            } catch (err) {
                this._setError(err.response?.data?.message || '사용자 정보 수정 실패');
                throw err;
            } finally {
                this._setLoading(false);
            }
        },
        async resetAdminUserPassword(userId) {
            this._setMessage('비밀번호 초기화 요청됨 (API 구현 필요)');
        },
        async deleteAdminUser(userId) {
            this._setLoading(true);
            this._clearMessages();
            try {
                await api.api.delete(`/api/admin/users/${userId}`);
                this._setMessage('사용자가 성공적으로 삭제되었습니다.');
            } catch (err) {
                const errorMessage = err.response?.data?.message || err.message || '사용자 삭제 중 오류 발생';
                this._setError(errorMessage);
                console.error('Error deleting user:', err);
                throw err;
            } finally {
                this._setLoading(false);
            }
        },
        async updateAdminUserRole(userId, newRole) {
            this._setLoading(true);
            this._clearMessages();
            try {
                await api.api.put(`/api/admin/users/${userId}/role`, { role: newRole });
            } catch (err) {
                const errorMessage = err.response?.data?.message || err.message || '사용자 역할 변경 중 오류가 발생했습니다.';
                this._setError(errorMessage);
                console.error('Error updating user role:', err);
                throw err;
            } finally {
                this._setLoading(false);
            }
        },
        async updateAdminUserStatus({ userId, status, reason }) {
            this._setLoading(true);
            this._clearMessages();
            try {
                const payload = { status };
                const response = await api.api.post(`/api/admin/users/${userId}/status`, payload);
                this._setMessage(response.data.message || '사용자 상태 변경 완료');
            } catch (err) {
                this._setError(err.response?.data?.message || '사용자 상태 변경 실패');
                throw err;
            } finally {
                this._setLoading(false);
            }
        },

        // ====== All Hosts Management ======
        async fetchAllHosts(params = { page: 0, size: 10, sortBy: 'createdAt', sortDirection: 'DESC', status: '' }) {
            this._setLoading(true);
            this._clearMessages();
            try {
                const response = await api.api.get('/api/admin/hosts/all', { params }); 
                this.allHosts = response.data.content;
                this.allHostsTotalPages = response.data.totalPages;
                this.allHostsCurrentPage = response.data.number + 1;
                this.allHostsTotalElements = response.data.totalElements;
                return response.data;
            } catch (err) {
                this._setError(err.response?.data?.message || '전체 호스트 목록 조회 실패');
                this.allHosts = [];
                this.allHostsTotalPages = 0;
                this.allHostsCurrentPage = 0;
                this.allHostsTotalElements = 0;
            } finally {
                this._setLoading(false);
            }
        },

        async updateHostStatusByAdmin({ hostId, status, reason = null }) {
            this._setLoading(true);
            this._clearMessages();
            try {
                const payload = { status };
                if (reason) payload.reason = reason;
                const response = await api.api.post(`/api/admin/hosts/${hostId}/status-update`, payload); 
                this._setMessage(response.data.message || '호스트 상태가 성공적으로 변경되었습니다.');
                await this.fetchAllHosts({ 
                    page: this.allHostsCurrentPage > 0 ? this.allHostsCurrentPage - 1 : 0, 
                    size: 10, 
                    // TODO: 현재 필터/정렬 조건 유지 필요
                }); 
            } catch (err) {
                this._setError(err.response?.data?.message || '호스트 상태 변경 실패');
                throw err;
            } finally {
                this._setLoading(false);
            }
        },

        // ====== All Hosts Management - 계정 자체의 상태 변경 및 삭제 액션 추가 ======
        async updateHostUserAccountStatus({ hostUserId, status, reason }) {
            this._setLoading(true);
            this._clearMessages();
            try {
                const payload = { status };
                if (reason) payload.reason = reason;
                // 실제 API 엔드포인트는 백엔드에 따라 달라질 수 있습니다.
                const response = await api.api.post(`/api/admin/hosts/${hostUserId}/user-account-status`, payload);
                this._setMessage(response.data.message || '호스트 계정 상태가 성공적으로 변경되었습니다.');
                await this.fetchAllHosts({ 
                    page: this.allHostsCurrentPage > 0 ? this.allHostsCurrentPage - 1 : 0, 
                    size: 10 
                });
            } catch (err) {
                this._setError(err.response?.data?.message || '호스트 계정 상태 변경 실패');
                throw err;
            } finally {
                this._setLoading(false);
            }
        },
    
        async deleteHostUserAccount(hostUserId) {
            this._setLoading(true);
            this._clearMessages();
            try {
                // 실제 API 엔드포인트는 백엔드에 따라 달라질 수 있습니다.
                await api.api.delete(`/api/admin/hosts/${hostUserId}/user-account`);
                this._setMessage('호스트 계정이 성공적으로 삭제되었습니다.');
                await this.fetchAllHosts({ 
                    page: this.allHostsCurrentPage > 0 ? this.allHostsCurrentPage - 1 : 0, 
                    size: 10 
                });
            } catch (err) {
                this._setError(err.response?.data?.message || '호스트 계정 삭제 실패');
                throw err;
            } finally {
                this._setLoading(false);
            }
        },

        // Region Management
        async fetchSidos() {
            this._setLoading(true);
            this._clearMessages();
            try {
                const response = await api.api.get('/api/admin/regions/sidos');
                this.sidos = response.data;
                return this.sidos;
            } catch (err) {
                this._setError(err.response?.data?.message || '시도 목록 조회 실패');
                this.sidos = [];
                throw err;
            } finally {
                this._setLoading(false);
            }
        },
        async fetchGuguns(sidoCode) {
            this._setLoading(true);
            this._clearMessages();
            try {
                const params = sidoCode ? { sidoCode } : {};
                const response = await api.api.get('/api/admin/regions/guguns', { params });
                this.guguns = response.data;
                return this.guguns;
            } catch (err) {
                this._setError(err.response?.data?.message || '구군 목록 조회 실패');
                this.guguns = [];
                throw err;
            } finally {
                this._setLoading(false);
            }
        },
        async createSido(sidoData) {
            this._setLoading(true); this._clearMessages();
            try {
                const response = await api.api.post('/api/admin/regions/sidos', sidoData);
                this._setMessage(response.data.message || '시도 추가 완료');
                await this.fetchSidos();
            } catch (e) { this._setError(e.response?.data?.message || '시도 추가 실패'); throw e; }
            finally { this._setLoading(false); }
        },
        async updateSido(sidoData) {
            this._setLoading(true); this._clearMessages();
            try {
                const response = await api.api.put(`/api/admin/regions/sidos/${sidoData.sidoCode}`, sidoData);
                this._setMessage(response.data.message || '시도 수정 완료');
                await this.fetchSidos();
            } catch (e) { this._setError(e.response?.data?.message || '시도 수정 실패'); throw e; }
            finally { this._setLoading(false); }
        },
        async deleteSido(sidoCode) {
            this._setLoading(true); this._clearMessages();
            try {
                const response = await api.api.delete(`/api/admin/regions/sidos/${sidoCode}`);
                this._setMessage(response.data.message || '시도 삭제 완료');
                await this.fetchSidos();
                await this.fetchGuguns();
            } catch (e) { this._setError(e.response?.data?.message || '시도 삭제 실패'); throw e; }
            finally { this._setLoading(false); }
        },
        async createGugun(gugunData) {
            this._setLoading(true); this._clearMessages();
            try {
                const response = await api.api.post('/api/admin/regions/guguns', gugunData);
                this._setMessage(response.data.message || '구군 추가 완료');
                await this.fetchGuguns(gugunData.sidoCode);
                await this.fetchSidos();
            } catch (e) { this._setError(e.response?.data?.message || '구군 추가 실패'); throw e; }
            finally { this._setLoading(false); }
        },
        async updateGugun(gugunData) {
            this._setLoading(true); this._clearMessages();
            try {
                const response = await api.api.put(`/api/admin/regions/guguns/${gugunData.gugunCode}`, gugunData);
                this._setMessage(response.data.message || '구군 수정 완료');
                await this.fetchGuguns(gugunData.sidoCode);
            } catch (e) { this._setError(e.response?.data?.message || '구군 수정 실패'); throw e; }
            finally { this._setLoading(false); }
        },
        async deleteGugun(gugunCode) {
            this._setLoading(true); this._clearMessages();
            try {
                const response = await api.api.delete(`/api/admin/regions/guguns/${gugunCode}`);
                this._setMessage(response.data.message || '구군 삭제 완료');
                await this.fetchGuguns();
                await this.fetchSidos();
            } catch (e) { this._setError(e.response?.data?.message || '구군 삭제 실패'); throw e; }
            finally { this._setLoading(false); }
        },
        async syncRegions() {
            this._setLoading(true); this._clearMessages();
            this.syncProgress = 0;
            try {
                const response = await api.api.post('/api/admin/regions/sync-start');
                this._setMessage(response.data.message || '지역 데이터 동기화 시작됨');
                this.lastSyncInfo.status = '동기화 진행 중...';
            } catch (e) { this._setError(e.response?.data?.message || '지역 데이터 동기화 시작 실패'); throw e; }
            finally { this._setLoading(false); }
        },
        async fetchLastSyncInfo() {
            try {
                const response = await api.api.get('/api/admin/regions/last-sync');
                this.lastSyncInfo = response.data;
            } catch (e) {
                console.error('마지막 동기화 정보 로딩 실패', e);
                this.lastSyncInfo = { date: null, status: '정보 없음' };
            }
        }
    }
});
