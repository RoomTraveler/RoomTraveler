import apiGroup from './index';

//apiGroup.api.get(...);
//apiGroup.apiNoAuth.get(...);

/**
 * 지역 관련 API 서비스
 */
const regionApi = {
  /**
   * 시도 목록 조회
   * @returns {Promise} 시도 목록 데이터
   */
  getSidos() {
    return apiGroup.apiNoAuth.get('/api/region/sidos');
  },

  /**
   * 구군 목록 조회
   * @param {string} sidoCode 시도 코드
   * @returns {Promise} 구군 목록 데이터
   */
  getGuguns(sidoCode) {
    return apiGroup.apiNoAuth.get(`/api/region/guguns?sido=${sidoCode}`);
  }
};

export default regionApi;