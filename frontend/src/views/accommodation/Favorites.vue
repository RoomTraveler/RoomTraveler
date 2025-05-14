<template>
  <Layout>
    <div class="container mt-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>내 찜 목록</h2>
        <router-link to="/accommodation/list" class="btn btn-outline-primary">숙소 더 찾아보기</router-link>
      </div>

      <!-- 알림 메시지 표시 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>
      
      <!-- 에러 메시지 표시 -->
      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
      </div>

      <!-- 로딩 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">찜 목록을 불러오는 중입니다...</p>
      </div>

      <!-- 찜 목록 -->
      <template v-else>
        <div v-if="favorites.length === 0" class="empty-favorites">
          <h3>아직 찜한 숙소가 없습니다.</h3>
          <p>마음에 드는 숙소를 찾아 하트 아이콘을 클릭하여 찜 목록에 추가해보세요!</p>
          <router-link to="/accommodation/list" class="btn btn-primary mt-3">숙소 찾아보기</router-link>
        </div>
        <div v-else class="row">
          <div v-for="favorite in favorites" :key="favorite.favoriteId" class="col-md-4 mb-4">
            <div class="card favorite-card">
              <img 
                :src="favorite.mainImageUrl || require('@/assets/no-image.jpg')" 
                class="card-img-top" 
                :alt="favorite.accommodationTitle"
              >
              <div class="card-body d-flex flex-column">
                <h5 class="card-title">{{ favorite.accommodationTitle }}</h5>
                <p class="card-text">{{ favorite.accommodationAddress }}</p>
                <p class="card-text text-muted">
                  <small>찜한 날짜: {{ formatDate(favorite.createdAt) }}</small>
                </p>
                <div class="favorite-actions mt-auto">
                  <router-link 
                    :to="`/accommodation/detail/${favorite.accommodationId}`" 
                    class="btn btn-primary"
                  >
                    상세 보기
                  </router-link>
                  <button 
                    @click="removeFavorite(favorite.favoriteId)" 
                    class="btn btn-outline-danger"
                  >
                    삭제
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </Layout>
</template>

<script>
/**
 * 찜 목록 컴포넌트
 * 
 * 이 컴포넌트는 사용자가 찜한 숙소 목록을 표시합니다.
 * 찜 목록 조회, 삭제 기능을 제공합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'Favorites',
  components: {
    Layout
  },
  data() {
    return {
      favorites: [],
      loading: false,
      message: '',
      error: ''
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: state => state.user.isLoggedIn
    })
  },
  created() {
    // 로그인 상태 확인
    if (!this.isLoggedIn) {
      this.$router.push('/user/login');
      return;
    }
    
    // 찜 목록 로드
    this.loadFavorites();
    
    // URL 쿼리 파라미터에서 메시지 또는 에러 확인
    if (this.$route.query.message) {
      this.message = this.$route.query.message;
    }
    if (this.$route.query.error) {
      this.error = this.$route.query.error;
    }
  },
  methods: {
    ...mapActions('accommodation', ['fetchFavorites', 'removeFavoriteItem']),
    
    /**
     * 찜 목록 로드
     */
    async loadFavorites() {
      this.loading = true;
      try {
        this.favorites = await this.fetchFavorites();
      } catch (error) {
        console.error('찜 목록을 불러오는 중 오류가 발생했습니다:', error);
        this.error = '찜 목록을 불러올 수 없습니다. 다시 시도해주세요.';
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 찜 항목 삭제
     * @param {number} favoriteId - 삭제할 찜 항목 ID
     */
    async removeFavorite(favoriteId) {
      if (confirm('정말로 찜 목록에서 삭제하시겠습니까?')) {
        try {
          await this.removeFavoriteItem(favoriteId);
          this.message = '찜 목록에서 삭제되었습니다.';
          // 찜 목록 새로고침
          this.loadFavorites();
        } catch (error) {
          console.error('찜 항목 삭제 중 오류가 발생했습니다:', error);
          this.error = '찜 항목을 삭제할 수 없습니다. 다시 시도해주세요.';
        }
      }
    },
    
    /**
     * 날짜 포맷팅 (YYYY-MM-DD)
     * @param {string|Date} date - 포맷팅할 날짜
     * @returns {string} 포맷팅된 날짜 문자열
     */
    formatDate(date) {
      if (!date) return '';
      
      const d = new Date(date);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      
      return `${year}-${month}-${day}`;
    }
  }
};
</script>

<style scoped>
.favorite-card {
  transition: transform 0.3s;
  margin-bottom: 20px;
  height: 100%;
}

.favorite-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}

.card-img-top {
  height: 200px;
  object-fit: cover;
}

.empty-favorites {
  text-align: center;
  padding: 50px 0;
}

.favorite-actions {
  display: flex;
  justify-content: space-between;
}
</style>