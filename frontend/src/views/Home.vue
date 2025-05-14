<template>
  <Layout>
    <div class="mt-5">
    <h2 class="text-center mb-4">환영합니다!</h2>

    <!-- 사용자 계정 섹션 -->
    <div v-if="isLoggedIn" class="row justify-content-center">
      <h4 class="text-center mb-3">내 계정</h4>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">사용자 정보</h5>
            <p class="card-text">사용자 정보를 확인하고 수정할 수 있습니다.</p>
            <router-link to="/user/profile" class="btn btn-primary w-100">사용자 정보 페이지</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">사용자 정보 수정</h5>
            <p class="card-text">개인 정보를 업데이트하세요.</p>
            <router-link to="/user/update" class="btn btn-warning w-100">사용자 정보 수정</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">회원 탈퇴</h5>
            <p class="card-text">계정을 삭제하려면 클릭하세요.</p>
            <router-link to="/user/delete" class="btn btn-danger w-100">회원 탈퇴</router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 일반 사용자 기능 -->
    <div v-if="isLoggedIn" class="row justify-content-center mt-4">
      <h4 class="text-center mb-3">내 여행</h4>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">내 예약</h5>
            <p class="card-text">예약한 숙소를 확인합니다.</p>
            <router-link to="/reservation/my-reservations" class="btn btn-primary w-100">예약 확인</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">찜한 숙소</h5>
            <p class="card-text">관심있는 숙소 목록을 확인합니다.</p>
            <router-link to="/accommodation/favorites" class="btn btn-danger w-100">찜 목록</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">여행 후기</h5>
            <p class="card-text">내가 작성한 리뷰를 관리합니다.</p>
            <router-link to="/review/my-reviews" class="btn btn-success w-100">내 리뷰</router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 여행 계획 섹션 -->
    <div class="row justify-content-center mt-4">
      <h4 class="text-center mb-3">여행 계획</h4>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">관광지 검색</h5>
            <p class="card-text">지역별 관광지를 검색하고 여행을 계획하세요.</p>
            <router-link to="/map/region-trip" class="btn btn-success w-100">관광지 검색</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">인기 여행지</h5>
            <p class="card-text">인기 있는 여행지를 확인하세요.</p>
            <router-link to="/tourapi/popular" class="btn btn-primary w-100">인기 여행지</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">여행 가이드</h5>
            <p class="card-text">여행 계획에 도움이 되는 가이드를 확인하세요.</p>
            <router-link to="/tourapi/guides" class="btn btn-info w-100">여행 가이드</router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 숙소 섹션 -->
    <div class="row justify-content-center mt-4">
      <h4 class="text-center mb-3">숙소</h4>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">숙소 찾기</h5>
            <p class="card-text">다양한 숙소를 검색하고 예약하세요.</p>
            <router-link to="/accommodation/list" class="btn btn-primary w-100">숙소 목록</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">특가 숙소</h5>
            <p class="card-text">할인 중인 특가 숙소를 확인하세요.</p>
            <router-link to="/accommodation/special-offers" class="btn btn-danger w-100">특가 숙소</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">추천 숙소</h5>
            <p class="card-text">높은 평점의 추천 숙소를 확인하세요.</p>
            <router-link to="/accommodation/recommended" class="btn btn-success w-100">추천 숙소</router-link>
          </div>
        </div>
      </div>

      <div v-if="isHost || isAdmin" class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">숙소 등록</h5>
            <p class="card-text">새로운 숙소를 등록합니다.</p>
            <router-link to="/host/register-accommodation" class="btn btn-success w-100">숙소 등록</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">호스트 사이트</h5>
            <p class="card-text">호스트 전용 사이트로 이동합니다.</p>
            <router-link to="/host" class="btn btn-warning w-100">호스트 사이트</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">호스트 신청</h5>
            <p class="card-text">호스트가 되어 숙소를 등록해보세요.</p>
            <router-link v-if="isLoggedIn && !isHost && !isAdmin" to="/host/regist" class="btn btn-outline-warning w-100">호스트 신청하기</router-link>
            <router-link v-if="!isLoggedIn" to="/host/regist-user" class="btn btn-outline-warning w-100">호스트로 가입하기</router-link>
            <button v-if="isHost || isAdmin" class="btn btn-secondary w-100" disabled>이미 호스트입니다</button>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">관리자 페이지</h5>
            <p class="card-text">관리자 전용 대시보드로 이동합니다.</p>
            <router-link v-if="isAdmin" to="/admin/dashboard" class="btn btn-danger w-100">관리자 페이지</router-link>
            <button v-if="!isAdmin" class="btn btn-secondary w-100" disabled>관리자 전용</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 관리자 전용 기능 -->
    <div v-if="isAdmin" class="row justify-content-center mt-4">
      <h4 class="text-center mb-3">관리자 전용 기능</h4>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">사용자 관리</h5>
            <p class="card-text">사용자 목록을 확인하고 관리합니다.</p>
            <router-link to="/admin/users" class="btn btn-primary w-100">사용자 관리</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">숙소 관리</h5>
            <p class="card-text">등록된 모든 숙소를 관리합니다.</p>
            <router-link to="/admin/accommodations" class="btn btn-success w-100">숙소 관리</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">지역 데이터 관리</h5>
            <p class="card-text">지역 데이터를 관리합니다.</p>
            <router-link to="/admin/regions" class="btn btn-info w-100">지역 관리</router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 호스트 전용 기능 -->
    <div v-if="isHost || isAdmin" class="row justify-content-center mt-4">
      <h4 class="text-center mb-3">호스트 전용 기능</h4>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">내 호스트 정보</h5>
            <p class="card-text">호스트 정보를 확인하고 수정합니다.</p>
            <router-link :to="`/host/detail/${userId}`" class="btn btn-primary w-100">호스트 정보 관리</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">내 숙소 관리</h5>
            <p class="card-text">등록한 숙소를 관리합니다.</p>
            <router-link to="/accommodation/my-accommodations" class="btn btn-success w-100">숙소 관리</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card shadow-sm">
          <div class="card-body text-center">
            <h5 class="card-title">예약 관리</h5>
            <p class="card-text">숙소 예약 현황을 확인합니다.</p>
            <router-link to="/reservation/host-reservations" class="btn btn-info w-100">예약 관리</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
  </Layout>
</template>

<script>
import Layout from '@/components/layout/Layout.vue';
import { useUserStore } from '@/store/userStore';

export default {
  name: 'HomeView',
  components: {
    Layout
  },
  setup() {
    const userStore = useUserStore();
    return { userStore };
  },
  computed: {
    isLoggedIn() {
      return this.userStore.isAuthenticated;
    },
    user() {
      return this.userStore.user;
    },
    userId() {
      return this.userStore.user?.id;
    },
    isAdmin() {
      return this.userStore.userRole === 'ADMIN';
    },
    isHost() {
      return this.userStore.userRole === 'HOST';
    }
  }
};
</script>

<style scoped>
:root {
  --yanolja-red: #f0213b;
  --yanolja-pink: #ff3478;
  --yanolja-light-gray: #f5f5f5;
  --yanolja-dark-gray: #666;
}

body {
  font-family: 'Noto Sans KR', sans-serif;
  color: #333;
  background-color: #f9f9f9;
}

.card {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  background-color: white;
  transition: transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-body {
  padding: 15px;
}

.card-title {
  font-weight: bold;
  margin-bottom: 5px;
  font-size: 1.1rem;
}

.card-text {
  font-size: 0.9rem;
  color: var(--yanolja-dark-gray);
  margin-bottom: 10px;
}

.btn {
  margin-top: 10px;
}

.btn-yanolja {
  background-color: var(--yanolja-red);
  color: white;
  border: none;
}

.btn-yanolja:hover {
  background-color: #d01c33;
  color: white;
}

.section-title {
  font-weight: bold;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading {
  text-align: center;
  padding: 20px;
}
</style>
