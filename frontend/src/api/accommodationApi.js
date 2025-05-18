import api from './index';

/**
 * 숙박 및 관광지 관련 API 서비스
 */
const accommodationApi = {
  /**
   * 숙박 시설 목록 조회
   * @param {Object} params 검색 파라미터
   * @param {string} [params.areaCode] 지역 코드
   * @param {string} [params.sigunguCode] 시군구 코드
   * @param {number} [params.pageNo] 페이지 번호
   * @param {number} [params.numOfRows] 한 페이지 결과 수
   * @returns {Promise} 숙박 시설 목록 데이터
   */
  getAccommodations(params = {}) {
    return api.get('/api/accommodations/filter', { params });
  },

  /**
   * 숙박 시설 상세 정보 및 객실 정보 조회
   * @param {string} contentId 콘텐츠 ID
   * @returns {Promise} 숙박 시설 상세 정보 및 객실 정보
   */
  getRoomInfo(contentId) {
    return api.get(`/accommodation/api/rooms`, { params: { contentId } });
  },

  /**
   * 관광지 목록 조회
   * @param {Object} params 검색 파라미터
   * @param {string} [params.areaCode] 지역 코드
   * @param {string} [params.sigunguCode] 시군구 코드
   * @param {string} [params.contentTypeId] 관광타입 ID
   * @param {number} [params.pageNo] 페이지 번호
   * @param {number} [params.numOfRows] 한 페이지 결과 수
   * @returns {Promise} 관광지 목록 데이터
   */
  getAttractions(params = {}) {
    return api.get('/accommodation/api/attractions', { params });
  },

  /**
   * 관광지 상세 정보 조회
   * @param {string} contentId 콘텐츠 ID
   * @param {string} [contentTypeId] 관광타입 ID
   * @returns {Promise} 관광지 상세 정보
   */
  getAttractionDetail(contentId, contentTypeId) {
    const params = { contentId };
    if (contentTypeId) {
      params.contentTypeId = contentTypeId;
    }
    return api.get('/accommodation/api/attraction/detail', { params });
  },

  /**
   * 키워드로 관광지 검색
   * @param {string} keyword 검색 키워드
   * @param {Object} params 추가 검색 파라미터
   * @param {string} [params.areaCode] 지역 코드
   * @param {string} [params.sigunguCode] 시군구 코드
   * @param {string} [params.contentTypeId] 관광타입 ID
   * @param {number} [params.pageNo] 페이지 번호
   * @param {number} [params.numOfRows] 한 페이지 결과 수
   * @returns {Promise} 검색 결과 데이터
   */
  searchByKeyword(keyword, params = {}) {
    return api.get('/accommodation/api/search', { 
      params: { 
        keyword,
        ...params 
      } 
    });
  }
};

export default accommodationApi;