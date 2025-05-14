<template>
  <div class="profile-container">
    <div v-if="!user" class="loading">
      <p>사용자 정보를 불러오는 중...</p>
    </div>
    
    <div v-else class="profile-content">
      <div class="profile-header">
        <div class="profile-avatar">
          <img :src="user.avatar || 'https://via.placeholder.com/150?text=사용자'" alt="프로필 이미지" />
          <button class="change-avatar-btn">이미지 변경</button>
        </div>
        
        <div class="profile-info">
          <h1>{{ user.name }}님의 프로필</h1>
          <p class="email">{{ user.email }}</p>
          <p class="member-since">가입일: {{ formatDate(user.createdAt) }}</p>
        </div>
      </div>
      
      <div class="profile-tabs">
        <button 
          :class="['tab-button', { active: activeTab === 'info' }]" 
          @click="activeTab = 'info'"
        >
          내 정보
        </button>
        <button 
          :class="['tab-button', { active: activeTab === 'reservations' }]" 
          @click="activeTab = 'reservations'"
        >
          예약 내역
        </button>
        <button 
          :class="['tab-button', { active: activeTab === 'favorites' }]" 
          @click="activeTab = 'favorites'"
        >
          찜한 숙소
        </button>
        <button 
          :class="['tab-button', { active: activeTab === 'reviews' }]" 
          @click="activeTab = 'reviews'"
        >
          내 리뷰
        </button>
      </div>
      
      <!-- 내 정보 탭 -->
      <div v-if="activeTab === 'info'" class="tab-content">
        <h2>내 정보 관리</h2>
        
        <form @submit.prevent="updateProfile">
          <div class="form-group">
            <label for="name">이름</label>
            <input type="text" id="name" v-model="userForm.name" required />
          </div>
          
          <div class="form-group">
            <label for="phone">전화번호</label>
            <input type="tel" id="phone" v-model="userForm.phone" />
          </div>
          
          <div class="form-group">
            <label for="address">주소</label>
            <input type="text" id="address" v-model="userForm.address" />
          </div>
          
          <button type="submit" class="update-button" :disabled="updating">
            {{ updating ? '업데이트 중...' : '정보 업데이트' }}
          </button>
        </form>
        
        <div class="password-section">
          <h3>비밀번호 변경</h3>
          <form @submit.prevent="changePassword">
            <div class="form-group">
              <label for="currentPassword">현재 비밀번호</label>
              <input type="password" id="currentPassword" v-model="passwordForm.currentPassword" required />
            </div>
            
            <div class="form-group">
              <label for="newPassword">새 비밀번호</label>
              <input type="password" id="newPassword" v-model="passwordForm.newPassword" required />
            </div>
            
            <div class="form-group">
              <label for="confirmPassword">비밀번호 확인</label>
              <input type="password" id="confirmPassword" v-model="passwordForm.confirmPassword" required />
            </div>
            
            <button type="submit" class="password-button" :disabled="updatingPassword">
              {{ updatingPassword ? '변경 중...' : '비밀번호 변경' }}
            </button>
          </form>
        </div>
      </div>
      
      <!-- 예약 내역 탭 -->
      <div v-else-if="activeTab === 'reservations'" class="tab-content">
        <h2>예약 내역</h2>
        
        <div v-if="reservations.length === 0" class="empty-state">
          <p>예약 내역이 없습니다.</p>
          <router-link to="/accommodation" class="action-link">숙소 둘러보기</router-link>
        </div>
        
        <div v-else class="reservations-list">
          <div v-for="reservation in reservations" :key="reservation.id" class="reservation-card">
            <img :src="reservation.accommodation.imageUrl" :alt="reservation.accommodation.name" />
            <div class="reservation-details">
              <h3>{{ reservation.accommodation.name }}</h3>
              <p class="reservation-date">
                {{ formatDate(reservation.checkIn) }} ~ {{ formatDate(reservation.checkOut) }}
              </p>
              <p class="reservation-guests">인원: {{ reservation.guestCount }}명</p>
              <p class="reservation-status" :class="reservation.status">
                {{ getStatusText(reservation.status) }}
              </p>
              <div class="reservation-actions">
                <router-link :to="`/accommodation/${reservation.accommodation.id}`" class="view-link">
                  숙소 보기
                </router-link>
                <button 
                  v-if="reservation.status === 'confirmed'" 
                  class="cancel-button"
                  @click="cancelReservation(reservation.id)"
                >
                  예약 취소
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 찜한 숙소 탭 -->
      <div v-else-if="activeTab === 'favorites'" class="tab-content">
        <h2>찜한 숙소</h2>
        
        <div v-if="favorites.length === 0" class="empty-state">
          <p>찜한 숙소가 없습니다.</p>
          <router-link to="/accommodation" class="action-link">숙소 둘러보기</router-link>
        </div>
        
        <div v-else class="favorites-grid">
          <div v-for="favorite in favorites" :key="favorite.id" class="favorite-card">
            <div class="favorite-actions">
              <button class="remove-favorite" @click="removeFavorite(favorite.id)">
                ❤️
              </button>
            </div>
            <img :src="favorite.imageUrl" :alt="favorite.name" />
            <div class="favorite-content">
              <h3>{{ favorite.name }}</h3>
              <p class="location">{{ favorite.location }}</p>
              <p class="price">{{ favorite.price }}원 / 박</p>
              <router-link :to="`/accommodation/${favorite.id}`" class="view-details">
                상세 보기
              </router-link>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 내 리뷰 탭 -->
      <div v-else-if="activeTab === 'reviews'" class="tab-content">
        <h2>내 리뷰</h2>
        
        <div v-if="reviews.length === 0" class="empty-state">
          <p>작성한 리뷰가 없습니다.</p>
        </div>
        
        <div v-else class="reviews-list">
          <div v-for="review in reviews" :key="review.id" class="review-card">
            <div class="review-header">
              <h3>{{ review.accommodation.name }}</h3>
              <div class="review-rating">
                <span class="stars">{{ getStars(review.rating) }}</span>
                <span class="rating-value">{{ review.rating }}/5</span>
              </div>
            </div>
            <p class="review-date">작성일: {{ formatDate(review.createdAt) }}</p>
            <p class="review-content">{{ review.content }}</p>
            <div class="review-actions">
              <button class="edit-button" @click="editReview(review)">수정</button>
              <button class="delete-button" @click="deleteReview(review.id)">삭제</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserProfile',
  data() {
    return {
      user: null,
      activeTab: 'info',
      userForm: {
        name: '',
        phone: '',
        address: ''
      },
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      updating: false,
      updatingPassword: false,
      reservations: [],
      favorites: [],
      reviews: []
    };
  },
  methods: {
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    },
    getStatusText(status) {
      const statusMap = {
        confirmed: '예약 확정',
        pending: '승인 대기 중',
        completed: '이용 완료',
        cancelled: '예약 취소'
      };
      return statusMap[status] || status;
    },
    getStars(rating) {
      return '★'.repeat(rating) + '☆'.repeat(5 - rating);
    },
    updateProfile() {
      this.updating = true;
      
      // 실제 API 호출 대신 임시 로직 사용
      setTimeout(() => {
        this.user = {
          ...this.user,
          name: this.userForm.name,
          phone: this.userForm.phone,
          address: this.userForm.address
        };
        
        alert('프로필 정보가 업데이트되었습니다.');
        this.updating = false;
      }, 1000);
    },
    changePassword() {
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        alert('새 비밀번호와 확인 비밀번호가 일치하지 않습니다.');
        return;
      }
      
      this.updatingPassword = true;
      
      // 실제 API 호출 대신 임시 로직 사용
      setTimeout(() => {
        alert('비밀번호가 성공적으로 변경되었습니다.');
        this.passwordForm = {
          currentPassword: '',
          newPassword: '',
          confirmPassword: ''
        };
        this.updatingPassword = false;
      }, 1000);
    },
    cancelReservation(id) {
      if (confirm('정말로 이 예약을 취소하시겠습니까?')) {
        // 실제 API 호출 대신 임시 로직 사용
        this.reservations = this.reservations.map(res => 
          res.id === id ? { ...res, status: 'cancelled' } : res
        );
        alert('예약이 취소되었습니다.');
      }
    },
    removeFavorite(id) {
      if (confirm('이 숙소를 찜 목록에서 삭제하시겠습니까?')) {
        // 실제 API 호출 대신 임시 로직 사용
        this.favorites = this.favorites.filter(fav => fav.id !== id);
        alert('찜 목록에서 삭제되었습니다.');
      }
    },
    editReview(review) {
      // 리뷰 수정 로직 (나중에 구현)
      alert('리뷰 수정 기능은 아직 구현되지 않았습니다.');
    },
    deleteReview(id) {
      if (confirm('이 리뷰를 삭제하시겠습니까?')) {
        // 실제 API 호출 대신 임시 로직 사용
        this.reviews = this.reviews.filter(review => review.id !== id);
        alert('리뷰가 삭제되었습니다.');
      }
    },
    fetchUserData() {
      // 실제 API 호출 대신 임시 데이터 사용
      setTimeout(() => {
        const userData = JSON.parse(localStorage.getItem('user'));
        
        if (userData) {
          this.user = {
            ...userData,
            phone: '010-1234-5678',
            address: '서울시 강남구',
            createdAt: '2023-01-15T09:30:00'
          };
          
          // 폼 데이터 초기화
          this.userForm = {
            name: this.user.name,
            phone: this.user.phone,
            address: this.user.address
          };
          
          // 예약 내역 샘플 데이터
          this.reservations = [
            {
              id: 1,
              accommodation: {
                id: 1,
                name: '서울 시티 호텔',
                imageUrl: 'https://via.placeholder.com/300x200?text=서울+시티+호텔'
              },
              checkIn: '2023-06-15',
              checkOut: '2023-06-18',
              guestCount: 2,
              status: 'completed'
            },
            {
              id: 2,
              accommodation: {
                id: 2,
                name: '부산 오션 리조트',
                imageUrl: 'https://via.placeholder.com/300x200?text=부산+오션+리조트'
              },
              checkIn: '2023-08-10',
              checkOut: '2023-08-15',
              guestCount: 3,
              status: 'confirmed'
            }
          ];
          
          // 찜한 숙소 샘플 데이터
          this.favorites = [
            {
              id: 3,
              name: '제주 풀빌라',
              location: '제주 서귀포시',
              price: 200000,
              imageUrl: 'https://via.placeholder.com/300x200?text=제주+풀빌라'
            }
          ];
          
          // 리뷰 샘플 데이터
          this.reviews = [
            {
              id: 1,
              accommodation: {
                id: 1,
                name: '서울 시티 호텔'
              },
              rating: 4,
              content: '위치가 좋고 시설이 깨끗했습니다. 다음에도 이용하고 싶어요.',
              createdAt: '2023-06-20T14:30:00'
            }
          ];
        } else {
          // 로그인되지 않은 경우 로그인 페이지로 리디렉션
          this.$router.push('/login');
        }
      }, 1000);
    }
  },
  created() {
    this.fetchUserData();
  }
};
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.loading {
  text-align: center;
  padding: 5rem 0;
  color: #666;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid #eee;
}

.profile-avatar {
  position: relative;
  margin-right: 2rem;
}

.profile-avatar img {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.change-avatar-btn {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0.5rem 1rem;
  font-size: 0.8rem;
  cursor: pointer;
}

.profile-info h1 {
  margin-bottom: 0.5rem;
  color: #333;
}

.email {
  color: #666;
  margin-bottom: 0.5rem;
}

.member-since {
  color: #888;
  font-size: 0.9rem;
}

.profile-tabs {
  display: flex;
  border-bottom: 1px solid #eee;
  margin-bottom: 2rem;
}

.tab-button {
  padding: 1rem 1.5rem;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  font-size: 1rem;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-button:hover {
  color: #42b983;
}

.tab-button.active {
  color: #42b983;
  border-bottom-color: #42b983;
  font-weight: 500;
}

.tab-content {
  padding: 1rem 0;
}

h2 {
  margin-bottom: 1.5rem;
  color: #333;
}

h3 {
  margin-bottom: 1rem;
  color: #444;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
}

input[type="text"],
input[type="tel"],
input[type="password"] {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.update-button,
.password-button {
  padding: 12px 24px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.update-button:hover,
.password-button:hover {
  background-color: #3aa876;
}

.update-button:disabled,
.password-button:disabled {
  background-color: #a8d5c2;
  cursor: not-allowed;
}

.password-section {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 1px solid #eee;
}

.empty-state {
  text-align: center;
  padding: 3rem 0;
  color: #666;
}

.action-link {
  display: inline-block;
  margin-top: 1rem;
  padding: 0.8rem 1.5rem;
  background-color: #42b983;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.action-link:hover {
  background-color: #3aa876;
}

.reservations-list,
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.reservation-card,
.review-card {
  display: flex;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.reservation-card img {
  width: 200px;
  height: 150px;
  object-fit: cover;
}

.reservation-details,
.review-card {
  padding: 1.5rem;
  flex: 1;
}

.reservation-date,
.review-date {
  color: #666;
  margin-bottom: 0.5rem;
}

.reservation-guests {
  color: #666;
  margin-bottom: 0.5rem;
}

.reservation-status {
  font-weight: 500;
  margin-bottom: 1rem;
}

.reservation-status.confirmed {
  color: #4caf50;
}

.reservation-status.pending {
  color: #ff9800;
}

.reservation-status.completed {
  color: #2196f3;
}

.reservation-status.cancelled {
  color: #f44336;
}

.reservation-actions,
.review-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.view-link {
  padding: 0.5rem 1rem;
  background-color: #42b983;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  font-size: 0.9rem;
}

.cancel-button,
.delete-button {
  padding: 0.5rem 1rem;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
}

.edit-button {
  padding: 0.5rem 1rem;
  background-color: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

.favorite-card {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.favorite-card:hover {
  transform: translateY(-5px);
}

.favorite-actions {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1;
}

.remove-favorite {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  transition: transform 0.3s;
}

.remove-favorite:hover {
  transform: scale(1.2);
}

.favorite-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.favorite-content {
  padding: 1rem;
}

.favorite-content h3 {
  margin-bottom: 0.5rem;
  font-size: 1.2rem;
}

.location {
  color: #666;
  margin-bottom: 0.5rem;
}

.price {
  font-weight: bold;
  color: #42b983;
  margin-bottom: 1rem;
}

.view-details {
  display: inline-block;
  padding: 8px 16px;
  background-color: #42b983;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  font-size: 0.9rem;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.review-rating {
  display: flex;
  align-items: center;
}

.stars {
  color: #ffc107;
  margin-right: 0.5rem;
}

.rating-value {
  color: #666;
}

.review-content {
  margin: 1rem 0;
  line-height: 1.6;
  color: #444;
}
</style>