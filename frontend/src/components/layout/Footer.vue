<template>
  <footer class="yanolja-footer">
    <div class="container">
      <!-- 푸터 상단 영역 - 주요 링크 -->
      <div class="footer-top">
        <div class="footer-section">
          <h3 class="footer-title">Room Traveler</h3>
          <ul class="footer-links">
            <li><router-link to="/about">회사소개</router-link></li>
            <li><router-link to="/terms">이용약관</router-link></li>
            <li><router-link to="/privacy">개인정보처리방침</router-link></li>
            <li><router-link to="/contact">고객센터</router-link></li>
          </ul>
        </div>

        <div class="footer-section">
          <h3 class="footer-title">숙소</h3>
          <ul class="footer-links">
            <li><router-link to="/accommodation/list">모든 숙소</router-link></li>
            <li><router-link to="/accommodation/special-offers">특가 숙소</router-link></li>
            <li><router-link to="/accommodation/recommended">추천 숙소</router-link></li>
            <li><router-link to="/accommodation/new">신규 숙소</router-link></li>
          </ul>
        </div>

        <div class="footer-section">
          <h3 class="footer-title">여행</h3>
          <ul class="footer-links">
            <li><router-link to="/map/region-trip">지역별 여행</router-link></li>
            <li><router-link to="/tourapi/popular">인기 여행지</router-link></li>
            <li><router-link to="/tourapi/guides">여행 가이드</router-link></li>
            <li><router-link to="/trip/plan">여행 계획</router-link></li>
          </ul>
        </div>

        <div class="footer-section">
          <h3 class="footer-title">호스트</h3>
          <ul class="footer-links">
            <li><router-link to="/host">호스트 사이트</router-link></li>
            <li><router-link to="/host/regist">호스트 신청</router-link></li>
            <li><router-link to="/host/guide">호스트 가이드</router-link></li>
            <li><router-link to="/host/faq">자주 묻는 질문</router-link></li>
          </ul>
        </div>
      </div>

      <!-- 푸터 중간 영역 - 고객센터 정보 -->
      <div class="footer-middle">
        <div class="customer-service">
          <h3 class="cs-title">고객센터</h3>
          <p class="cs-phone">1544-0000</p>
          <p class="cs-time">평일 09:00 ~ 18:00 (주말, 공휴일 휴무)</p>
          <p class="cs-email">이메일: support@roomtraveler.com</p>
        </div>
      </div>

      <!-- 푸터 하단 영역 - 회사 정보 및 저작권 -->
      <div class="footer-bottom">
        <div class="company-info">
          <p>상호명: (주)룸트래블러 | 대표: 홍길동 | 사업자등록번호: 123-45-67890</p>
          <p>주소: 서울특별시 강남구 테헤란로 123, 4층 | 통신판매업신고: 제2025-서울강남-0000호</p>
          <p>© 2025 Room Traveler. All rights reserved.</p>
        </div>
      </div>
    </div>
  </footer>
</template>

<script>
/**
 * 푸터 컴포넌트
 * 
 * 이 컴포넌트는 웹사이트의 공통 푸터 부분을 담당합니다.
 * 회사 정보, 주요 링크, 고객센터 정보 등을 표시합니다.
 */
export default {
  name: 'Footer'
}
</script>

<style scoped>
:root {
  --yanolja-red: #f0213b;
  --yanolja-pink: #ff3478;
  --yanolja-light-gray: #f5f5f5;
  --yanolja-dark-gray: #666;
}

.yanolja-footer {
  background-color: #f8f9fa;
  padding: 40px 0 20px;
  margin-top: 50px;
  border-top: 1px solid #eee;
  color: #666;
}

.yanolja-footer .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
}

.footer-top {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  margin-bottom: 30px;
}

.footer-section {
  flex: 1;
  min-width: 200px;
  margin-bottom: 20px;
  padding-right: 20px;
}

.footer-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links li {
  margin-bottom: 10px;
}

.footer-links a {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: var(--yanolja-red);
}

.footer-middle {
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
  padding: 20px 0;
  margin-bottom: 20px;
}

.customer-service {
  max-width: 400px;
}

.cs-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.cs-phone {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
  color: var(--yanolja-red);
}

.cs-time, .cs-email {
  font-size: 14px;
  margin-bottom: 5px;
}

.footer-bottom {
  text-align: center;
}

.company-info p {
  font-size: 12px;
  margin-bottom: 5px;
  color: #999;
}

@media (max-width: 768px) {
  .footer-top {
    flex-direction: column;
  }

  .footer-section {
    width: 100%;
    margin-bottom: 20px;
  }

  .yanolja-footer .container {
    padding: 0 15px;
  }
}
</style>
