<template>
  <div>
    <!-- 헤더 영역 -->
    <HeaderComponent />

    <div class="container mt-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>내 찜 목록</h2>
        <router-link to="/accommodation/list" class="btn btn-outline-primary">숙소 더 찾아보기</router-link>
      </div>

      <!-- 알림 메시지 표시 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''"></button>
      </div>
      <!-- 에러 메시지 표시 -->
      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''"></button>
      </div>

      <!-- 찜 목록 없을 때 -->
      <div v-if="favorites.length === 0">
        <div class="empty-favorites">
          <h3>아직 찜한 숙소가 없습니다.</h3>
          <p>마음에 드는 숙소를 찾아 하트 아이콘을 클릭하여 찜 목록에 추가해보세요!</p>
          <router-link to="/accommodation/list" class="btn btn-primary mt-3">숙소 찾아보기</router-link>
        </div>
      </div>

      <!-- 찜 목록 있을 때 -->
      <div v-else>
        <div class="row">
          <div v-for="favorite in favorites" :key="favorite.favoriteId" class="col-md-4 mb-4">
            <div class="card favorite-card h-100">
              <img
                :src="favorite.mainImageUrl || 'https://via.placeholder.com/300x200?text=No+Image'"
                class="card-img-top"
                :alt="favorite.accommodationTitle || 'No Image'"
              />
              <div class="card-body d-flex flex-column">
                <h5 class="card-title">{{ favorite.accommodationTitle }}</h5>
                <p class="card-text">{{ favorite.accommodationAddress }}</p>
                <p class="card-text text-muted">
                  <small>찜한 날짜: {{ formatDate(favorite.createdAt) }}</small>
                </p>
                <div class="favorite-actions mt-auto">
                  <router-link :to="`/accommodation/detail/${favorite.accommodationId}`" class="btn btn-primary"
                    >상세 보기</router-link
                  >
                  <button class="btn btn-outline-danger" @click="removeFavorite(favorite.favoriteId)">삭제</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 푸터 영역 -->
    <FooterComponent />
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";
import dayjs from "dayjs";
import HeaderComponent from "@/views/accommodation/AccommodationHeader.vue";
import FooterComponent from "@/components/layout/Footer.vue";

const favorites = ref([]);
const message = ref("");
const error = ref("");

function formatDate(date) {
  // date: ISO or timestamp
  return date ? dayjs(date).format("YYYY-MM-DD") : "";
}

async function fetchFavorites() {
  try {
    const { data } = await axios.get("/api/favorites");
    if (data && data.success) {
      favorites.value = data.favorites || [];
    } else {
      error.value = data.message || "찜 목록을 불러올 수 없습니다.";
    }
  } catch (e) {
    error.value = e.response?.data?.message || "찜 목록을 불러올 수 없습니다.";
  }
}

async function removeFavorite(favoriteId) {
  if (!window.confirm("정말로 찜 목록에서 삭제하시겠습니까?")) return;
  try {
    const res = await axios.delete(`/api/favorites/${favoriteId}`);
    if (res.data && res.data.success) {
      message.value = res.data.message || "삭제되었습니다.";
      favorites.value = favorites.value.filter((f) => f.favoriteId !== favoriteId);
    } else {
      error.value = res.data.message || "삭제에 실패했습니다.";
    }
  } catch (e) {
    error.value = e.response?.data?.message || "삭제 중 오류가 발생했습니다.";
  }
}

onMounted(fetchFavorites);
</script>

<style scoped>
.favorite-card {
  transition: transform 0.3s;
  margin-bottom: 20px;
  height: 100%;
}
.favorite-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
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
