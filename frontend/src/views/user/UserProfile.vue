<template>
  <div class="profile-container">
    <div v-if="isLoadingUser || (!currentUser && !userError)" class="loading">
      <p>사용자 정보를 불러오는 중...</p>
    </div>

    <div v-else-if="userError && !currentUser" class="loading">
      <p>{{ userError }}. 다시 로그인해주세요.</p>
      <router-link to="/login" class="action-link">로그인 페이지로</router-link>
    </div>

    <div v-else-if="currentUser && !isLoadingUser" class="profile-content">
      <div class="profile-header">
        <div class="profile-avatar">
          <img :src="currentUser.profileImage || defaultProfileImage" alt="프로필 이미지" @error="handleImageError" />
          <input type="file" ref="fileInputRef" @change="onFileSelected" accept="image/*" style="display: none" />
          <button @click="triggerFileInput" class="change-avatar-btn" :disabled="isUploadingImage">
            {{ isUploadingImage ? "업로드 중..." : "이미지 변경" }}
          </button>
        </div>

        <div class="profile-info">
          <h1>{{ currentUsername }}님의 프로필</h1>
          <p class="email">{{ currentUserEmail }}</p>
          <p class="member-since">가입일: {{ formatDate(currentUser.createdAt) }}</p>
        </div>
      </div>

      <div v-if="message" :class="['alert', messageType === 'success' ? 'alert-success' : 'alert-danger']" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="clearMessage" aria-label="Close"></button>
      </div>

      <div class="profile-tabs">
        <button :class="['tab-button', { active: activeTab === 'info' }]" @click="activeTab = 'info'">내 정보</button>
        <button :class="['tab-button', { active: activeTab === 'reservations' }]" @click="activeTab = 'reservations'">
          예약 내역
        </button>
        <button :class="['tab-button', { active: activeTab === 'favorites' }]" @click="activeTab = 'favorites'">
          찜한 숙소
        </button>
        <button :class="['tab-button', { active: activeTab === 'reviews' }]" @click="activeTab = 'reviews'">
          내 리뷰
        </button>
      </div>

      <!-- 내 정보 탭 -->
      <div v-if="activeTab === 'info'" class="tab-content">
        <h2>내 정보 관리</h2>

        <form @submit.prevent="handleUpdateProfile">
          <div class="form-group">
            <label for="username">이름 (닉네임)</label>
            <input type="text" id="username" v-model="profileForm.username" required />
          </div>

          <div class="form-group">
            <label for="phone">전화번호</label>
            <input type="tel" id="phone" v-model="profileForm.phone" placeholder="010-xxxx-xxxx" />
          </div>

          <button type="submit" class="update-button" :disabled="isUpdatingProfile">
            {{ isUpdatingProfile ? "업데이트 중..." : "정보 업데이트" }}
          </button>
        </form>

        <div class="password-section">
          <h3>비밀번호 변경</h3>
          <form @submit.prevent="handleChangePassword">
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

            <button type="submit" class="password-button" :disabled="isChangingPassword">
              {{ isChangingPassword ? "변경 중..." : "비밀번호 변경" }}
            </button>
          </form>
        </div>

        <!-- 호스트 등록 제안 섹션 -->
        <div v-if="userStore.userRole === 'USER'" class="host-registration-section">
          <p class="host-prompt-message">호스트로 등록하시고 싶으신가요?</p>
          <router-link :to="{ name: 'HostRegister' }" class="action-link">
            호스트 등록하기!
          </router-link>
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
              <button class="remove-favorite" @click="removeFavorite(favorite.id)">❤️</button>
            </div>
            <img :src="favorite.imageUrl" :alt="favorite.name" />
            <div class="favorite-content">
              <h3>{{ favorite.name }}</h3>
              <p class="location">{{ favorite.location }}</p>
              <p class="price">{{ favorite.price }}원 / 박</p>
              <router-link :to="`/accommodation/${favorite.id}`" class="view-details"> 상세 보기 </router-link>
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

<script setup>
import { ref, onMounted, reactive, computed, watch, nextTick } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/userStore";
import defaultProfileImage from "@/assets/no-image.jpg";

const router = useRouter();
const userStore = useUserStore();

const currentUserKey = ref(0);

// userStore state 및 getters 올바르게 참조
const currentUser = computed(() => userStore.user);
const currentUsername = computed(() => userStore.userName);
const currentUserEmail = computed(() => userStore.userEmail);
const isLoadingUser = computed(() => userStore.loading);
const userError = computed(() => userStore.error);
const tokens = computed(() => userStore.tokens);

// UserProfile.vue 내 로컬 로딩 상태 (userStore에 없는 경우)
const isUploadingImage = ref(false);
const isUpdatingProfile = ref(false);
const isChangingPassword = ref(false);

const activeTab = ref("info");
const fileInputRef = ref(null);

const profileForm = reactive({
  username: "",
  phone: "",
});

const passwordForm = reactive({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const message = ref("");
const messageType = ref("");

const reservations = ref([]);
const favorites = ref([]);
const reviews = ref([]);

function clearMessage() {
  message.value = "";
  messageType.value = "";
}

function showMessage(text, type = "danger") {
  message.value = text;
  messageType.value = type;
}

watch(
  currentUser,
  (newUser) => {
    if (newUser) {
      profileForm.username = newUser.name || newUser.username || "";
      profileForm.phone = newUser.phone || "";
      // currentUser가 변경될 때도 키를 업데이트하여 프로필 이미지, 가입일 등의 즉각적인 반영을 시도
      // 단, 이 watch는 profileForm 업데이트 외에 다른 사이드 이펙트를 유발할 수 있으므로 주의
      // currentUserKey.value++; // <<< 이 라인을 주석 처리합니다.
    } else {
      profileForm.username = "";
      profileForm.phone = "";
    }
  },
  { immediate: true, deep: true }
);

onMounted(async () => {
  console.log("UserProfile.vue: Mounted. Attempting to load user data.");
  userStore.loadUserFromStorage(); // 항상 스토리지에서 먼저 로드 시도

  if (!tokens.value?.access_token) {
    console.log("UserProfile.vue: No access token found, redirecting to login.");
    router.push("/login");
    return;
  }

  const userFromStorage = currentUser.value;
  // 스토리지에 사용자 정보가 없거나, id가 없거나, createdAt 또는 phone 정보가 없을 경우 서버에서 fetch
  if (!userFromStorage || !userFromStorage.id || !userFromStorage.createdAt || userFromStorage.phone === undefined) {
    console.log("UserProfile.vue: User data from storage is incomplete (e.g., missing createdAt or phone) or user ID missing. Fetching from server.");
    const result = await userStore.fetchCurrentUser();
    if (!result.success) {
      console.error("UserProfile.vue: fetchCurrentUser failed.", result.error);
      showMessage(result.error || "사용자 정보를 불러오는데 실패했습니다.", "danger");
      if (result.error?.includes("401") || result.error?.includes("403")) {
        console.log("UserProfile.vue: Auth error during fetch, logging out and redirecting to login.");
        await userStore.logout();
        router.push("/login");
      }
    } else if (userStore.user) {
      console.log("UserProfile.vue: fetchCurrentUser successful after finding incomplete/missing data in storage.");
      // currentUser가 업데이트되면 watch 콜백이 profileForm을 갱신하고, Vue가 UI를 업데이트합니다.
    }
  } else {
    console.log("UserProfile.vue: User data already available in store and seems complete (includes ID, createdAt, and phone defined).");
    // 이미 완전한 정보가 있으므로 profileForm은 watch에 의해 채워져 있을 것임
  }
});

const triggerFileInput = () => {
  fileInputRef.value?.click();
};

const onFileSelected = async (event) => {
  const file = event.target.files?.[0];
  if (!file) return;
  clearMessage();
  isUploadingImage.value = true;

  const formData = new FormData();
  formData.append("profileImageFile", file);

  try {
    const result = await userStore.updateProfileImage(formData);
    if (result.success && result.imageUrl) {
      showMessage("프로필 이미지가 성공적으로 변경되었습니다.", "success");
      currentUserKey.value++;
    } else {
      showMessage(result.error || "프로필 이미지 변경에 실패했습니다.", "danger");
    }
  } catch (error) {
    console.error("UserProfile.vue - Error uploading profile image:", error);
    showMessage("프로필 이미지 업로드 중 예기치 않은 오류가 발생했습니다.", "danger");
  } finally {
    isUploadingImage.value = false;
    if (fileInputRef.value) fileInputRef.value.value = "";
  }
};

const handleUpdateProfile = async () => {
  clearMessage();
  isUpdatingProfile.value = true;
  const result = await userStore.updateProfile({
    username: profileForm.username,
    phone: profileForm.phone,
  });
  isUpdatingProfile.value = false;
  if (result.success) {
    showMessage("프로필 정보가 성공적으로 업데이트되었습니다.", "success");
    currentUserKey.value++;
  } else {
    showMessage(userStore.error || "프로필 정보 업데이트에 실패했습니다.");
  }
};

const handleChangePassword = async () => {
  clearMessage();
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    showMessage("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
    return;
  }
  if (!passwordForm.currentPassword || !passwordForm.newPassword) {
    showMessage("모든 비밀번호 필드를 입력해주세요.");
    return;
  }
  isChangingPassword.value = true;
  const result = await userStore.changePassword(passwordForm.currentPassword, passwordForm.newPassword);
  isChangingPassword.value = false;
  if (result.success) {
    showMessage(result.message || "비밀번호가 성공적으로 변경되었습니다.", "success");
    passwordForm.currentPassword = "";
    passwordForm.newPassword = "";
    passwordForm.confirmPassword = "";
  } else {
    showMessage(userStore.error || "비밀번호 변경에 실패했습니다.");
  }
};

const formatDate = (dateString) => {
  if (!dateString) return "정보 없음";
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return "유효하지 않은 날짜";
    return date.toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  } catch (e) {
    return "날짜 형식 오류";
  }
};

const handleImageError = (event) => {
  event.target.src = defaultProfileImage;
};

const getStatusText = (status) => {
  const statusMap = {
    confirmed: "예약 확정",
    pending: "승인 대기 중",
    completed: "이용 완료",
    cancelled: "예약 취소",
  };
  return statusMap[status] || status;
};

const getStars = (rating) => {
  return "★".repeat(rating) + "☆".repeat(5 - rating);
};

// --- Mock 기능 또는 추후 구현될 기능 ---
const cancelReservation = (id) => {
  console.log("Cancel reservation:", id);
  showMessage("예약 취소 기능 구현 필요", "info");
};

const removeFavorite = (id) => {
  console.log("Remove favorite:", id);
  showMessage("찜 삭제 기능 구현 필요", "info");
};

const editReview = (review) => {
  console.log("Edit review:", review);
  showMessage("리뷰 수정 기능 구현 필요", "info");
};

const deleteReview = (id) => {
  console.log("Delete review:", id);
  showMessage("리뷰 삭제 기능 구현 필요", "info");
};

// --------- 오류 상태 변경 감지 및 메시지 표시 --------- //
watch(userError, (newError) => {
  if (newError && typeof newError === "string") {
    showMessage(newError, "danger");
  } else if (newError && typeof newError === "object" && newError.message) {
    showMessage(newError.message, "danger");
  }
});
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
  bottom: 5px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0.3rem 0.8rem;
  font-size: 0.75rem;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.profile-avatar:hover .change-avatar-btn {
  opacity: 1;
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
  font-size: 0.9em;
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
  color: #ff6b6b;
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

.review-header h3 {
  font-size: 1.1em;
  margin-bottom: 0.2em;
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

.alert {
  padding: 1rem;
  margin-bottom: 1rem;
  border: 1px solid transparent;
  border-radius: 0.25rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.alert-success {
  color: #0f5132;
  background-color: #d1e7dd;
  border-color: #badbcc;
}
.alert-danger {
  color: #842029;
  background-color: #f8d7da;
  border-color: #f5c2c7;
}
.btn-close {
  background: transparent;
  border: 0;
  font-size: 1.2rem;
  cursor: pointer;
}

.host-registration-section {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 1px solid #eee;
  text-align: center;
}

.host-prompt-message {
  color: #555;
  margin-bottom: 1.5rem;
  font-size: 1.1rem;
  font-weight: 500;
}

/* action-link 스타일은 이미 정의되어 있으므로 대부분 재사용됩니다. */
/* 필요에 따라 아래에서 크기나 마진 등을 조정할 수 있습니다. */
.host-registration-section .action-link {
  padding: 0.9rem 1.8rem; /* 버튼 크기를 약간 키움 (선택 사항) */
  font-size: 1rem; /* 버튼 내 텍스트 크기 (선택 사항) */
}
</style>
