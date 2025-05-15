import api from './index';

/**
 * 지역 관련 API 서비스
 */
const regionApi = {
  /**
   * 시도 목록 조회
   * @returns {Promise} 시도 목록 데이터
   */
  getSidos() {
    return api.get('/api/region/sidos');
  },

  /**
   * 구군 목록 조회
   * @param {string} sidoCode 시도 코드
   * @returns {Promise} 구군 목록 데이터
   */
  getGuguns(sidoCode) {
    return api.get(`/api/region/guguns?sido=${sidoCode}`);
  }
};

export default regionApi;