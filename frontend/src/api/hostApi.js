import apiUtils from './index';
const { api } = apiUtils;

// 호스트의 숙소 목록 가져오기
export async function getHostAccommodations(
  hostId,
  status,
  title,
  page = 1,
  size = 10
) {
  try {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (title) params.append('title', title);
    params.append('page', page.toString());
    params.append('size', size.toString());

    const response = await api.get(`/api/host/${hostId}/accommodations?${params.toString()}`);
    return response.data; // API 응답 구조에 따라 Array가 아닐 수 있음 (예: { content: [], totalPages: 3, ... })
  } catch (error) {
    console.error(`Error fetching accommodations for host ${hostId}:`, error);
    throw error;
  }
}

// 호스트 대시보드 요약 정보 가져오기
export async function getHostDashboardSummary(hostId) {
  try {
    const response = await api.get(`/api/host/${hostId}/dashboard/summary`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching dashboard summary for host ${hostId}:`, error);
    throw error;
  }
}

// 호스트의 최근 리뷰 목록 가져오기
export async function getHostRecentReviews(hostId, limit = 5) {
  try {
    const response = await api.get(`/api/host/${hostId}/dashboard/reviews/recent?limit=${limit}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching recent reviews for host ${hostId}:`, error);
    throw error;
  }
}

// 호스트의 숙소별 통계 정보 가져오기
export async function getHostAccommodationStats(hostId) {
  try {
    const response = await api.get(`/api/host/${hostId}/dashboard/accommodations/stats`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching accommodation stats for host ${hostId}:`, error);
    throw error;
  }
}

// --- 숙소 CRUD API 함수들 추가 ---

/**
 * 호스트의 특정 숙소 상세 정보 조회
 * @param {string | number} accommodationId 숙소 ID
 * @returns {Promise<object>} 숙소 상세 정보
 */
export async function getAccommodationDetails(accommodationId) {
  try {
    const response = await api.get(`/api/host/accommodations/${accommodationId}`);
    return response.data; // 백엔드 응답이 { success: true, data: {...} } 형태일 것으로 가정
  } catch (error) {
    console.error(`Error fetching accommodation details for ID ${accommodationId}:`, error);
    throw error;
  }
}

/**
 * 새 숙소 등록
 * @param {FormData} formData 숙소 정보 및 이미지 파일
 * @returns {Promise<object>} 등록 결과
 */
export async function createAccommodation(formData) {
  try {
    const response = await api.post('/api/host/accommodations', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  } catch (error) {
    console.error('Error creating accommodation:', error);
    throw error;
  }
}

/**
 * 기존 숙소 정보 수정
 * @param {string | number} accommodationId 숙소 ID
 * @param {FormData} formData 수정할 숙소 정보 및 이미지 파일
 * @returns {Promise<object>} 수정 결과
 */
export async function updateAccommodation(accommodationId, formData) {
  try {
    const response = await api.put(`/api/host/accommodations/${accommodationId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  } catch (error) {
    console.error(`Error updating accommodation ID ${accommodationId}:`, error);
    throw error;
  }
}

// --- 객실 관리 API 함수들 ---

/**
 * 특정 숙소의 객실 목록 조회 (호스트용)
 * @param {string | number} accommodationId 숙소 ID
 * @returns {Promise<object>} 객실 목록 데이터 (API 응답 구조에 따라)
 */
export async function getRoomsByAccommodationForHost(accommodationId) {
  try {
    const response = await api.get(`/api/host/accommodations/${accommodationId}/rooms`);
    // 백엔드 응답이 { success: true, data: [{...}, {...}] } 형태라고 가정
    if (response.data && response.data.success) {
      return response.data.data;
    }
    throw new Error(response.data.message || '객실 목록을 불러오는 데 실패했습니다.');
  } catch (error) {
    console.error(`Error fetching rooms for accommodation ${accommodationId}:`, error);
    throw error;
  }
}

/**
 * 새 객실 등록
 * @param {string | number} accommodationId 객실을 추가할 숙소 ID
 * @param {FormData} formData 객실 정보 및 이미지 파일
 * @returns {Promise<object>} 등록 결과 (API 응답 구조에 따라)
 */
export async function createRoom(accommodationId, formData) {
  try {
    const response = await api.post(`/api/host/accommodations/${accommodationId}/rooms`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data; // { success: true, message: '...', data: roomDto } 형태 가정
  } catch (error) {
    console.error('Error creating room:', error);
    throw error;
  }
}

/**
 * 특정 객실 상세 정보 조회 (호스트용, 수정용)
 * @param {string | number} roomId 객실 ID
 * @returns {Promise<object>} 객실 상세 정보
 */
export async function getRoomDetailsForHost(roomId) {
  try {
    const response = await api.get(`/api/host/rooms/${roomId}`);
    if (response.data && response.data.success) {
      return response.data.data;
    }
    throw new Error(response.data.message || '객실 상세 정보를 불러오는 데 실패했습니다.');
  } catch (error) {
    console.error(`Error fetching room details for ID ${roomId}:`, error);
    throw error;
  }
}

/**
 * 기존 객실 정보 수정
 * @param {string | number} roomId 객실 ID
 * @param {FormData} formData 수정할 객실 정보 및 이미지 파일
 * @returns {Promise<object>} 수정 결과
 */
export async function updateRoom(roomId, formData) {
  try {
    const response = await api.put(`/api/host/rooms/${roomId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data; // { success: true, message: '...', data: roomDto } 형태 가정
  } catch (error) {
    console.error(`Error updating room ID ${roomId}:`, error);
    throw error;
  }
}

/**
 * 객실 삭제
 * @param {string | number} roomId 삭제할 객실 ID
 * @returns {Promise<object>} 삭제 결과
 */
export async function deleteRoom(roomId) {
  try {
    const response = await api.delete(`/api/host/rooms/${roomId}`);
    return response.data; // { success: true, message: '...' } 형태 가정
  } catch (error) {
    console.error(`Error deleting room ID ${roomId}:`, error);
    throw error;
  }
} 