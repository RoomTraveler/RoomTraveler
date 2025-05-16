<template>
  <div class="container mt-5">
    <!-- 알림 메시지 -->
    <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
      {{ message }}
      <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
    </div>

    <!-- 로딩 표시 -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩 중...</span>
      </div>
      <p class="mt-2">숙소 정보를 불러오는 중입니다...</p>
    </div>

    <!-- 숙소 정보 & 예약 박스 -->
    <div v-if="!loading" class="row mb-5">
      <!-- 이미지 & 썸네일 캐러셀 -->
      <div class="col-lg-7">
        <div class="carousel-wrapper">
          <div class="carousel-inner">
            <div v-for="(img, idx) in images" :key="idx" :class="['carousel-item', { active: idx === activeSlide }]">
              <img :src="img" class="d-block w-100" :alt="accommodation.title || '숙소 이미지'" />
            </div>
          </div>
          <button class="carousel-prev" @click="prevSlide"><i class="bi bi-chevron-left"></i></button>
          <button class="carousel-next" @click="nextSlide"><i class="bi bi-chevron-right"></i></button>
        </div>
        <div class="carousel-thumbs mt-2 d-flex gap-2">
          <img
            v-for="(img, idx) in images"
            :key="idx"
            :src="img"
            class="thumb"
            :class="{ active: idx === activeSlide }"
            @click="goToSlide(idx)"
            alt="썸네일"
          />
        </div>
      </div>

      <!-- 우측 예약 박스 & 기본정보 -->
      <div class="col-lg-5">
        <div class="sticky-box p-4">
          <h4 class="price">
            {{ accommodation.basePrice ? formatPrice(accommodation.basePrice) : "--" }} <small>/박</small>
          </h4>
          <div class="date-picker mb-3">
            <label>체크인</label>
            <input type="date" v-model="checkInDate" class="form-control mb-2" />
            <label>체크아웃</label>
            <input type="date" v-model="checkOutDate" class="form-control" />
          </div>
          <div class="guests mb-3">
            <label>게스트</label>
            <select v-model="guestCount" class="form-select">
              <option v-for="n in accommodation.maxGuests || 1" :key="n" :value="n">{{ n }}명</option>
            </select>
          </div>
          <button class="btn btn-yanolja w-100 mb-3" @click="bookNow">예약하기</button>

          <hr />

          <h2 class="mb-2">{{ accommodation.title || "숙소명 없음" }}</h2>
          <p class="text-muted">{{ accommodation.sidoName || "-" }} {{ accommodation.gugunName || "" }}</p>
          <p><i class="bi bi-geo-alt me-1"></i>{{ accommodation.address || "주소 정보 없음" }}</p>
          <p><i class="bi bi-telephone me-1"></i>{{ accommodation.phone || "-" }}</p>
        </div>
      </div>
    </div>

    <!-- 숙소 설명 & 편의시설 -->
    <div class="row mb-5" v-if="!loading">
      <div class="col-12">
        <div class="card detail-card">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center">
              <h4 class="card-title mb-0">숙소 설명</h4>
              <div v-if="averageRating > 0" class="review-summary-header">
                <i class="bi bi-star-fill text-warning me-1"></i>
                <span class="fw-bold">{{ averageRating.toFixed(1) }}</span>
                <span class="text-muted ms-1">({{ reviewCount }}개의 리뷰)</span>
              </div>
            </div>
            <hr v-if="averageRating > 0" />
            <p class="card-text">{{ accommodation.description || "숙소 설명이 없습니다." }}</p>
            <h5 class="mt-4">편의시설</h5>
            <ul v-if="amenitiesList.length" class="amenities-list">
              <li v-for="(amenity, i) in amenitiesList" :key="i">
                <i class="bi bi-check-circle-fill text-success me-1"></i>{{ amenity }}
              </li>
            </ul>
            <p v-else class="text-muted">등록된 편의시설 정보가 없습니다.</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 객실 목록 -->
    <h3 class="mb-4" v-if="!loading">객실 목록</h3>
    <div class="row mb-5" v-if="!loading">
      <div v-if="rooms.length === 0" class="col-12 text-center py-5">
        <p class="lead">등록된 객실이 없습니다.</p>
      </div>
      <div v-for="room in rooms" :key="room.roomId" class="col-md-4 mb-4">
        <div class="card room-card h-100">
          <img :src="room.mainImageUrl || noImage" class="card-img-top" :alt="room.name || '객실 이미지'" />
          <div class="card-body d-flex flex-column">
            <h5 class="card-title">{{ room.name || "객실명 없음" }}</h5>
            <p class="card-text mb-3">
              <i class="bi bi-people me-1"></i>최대 {{ room.capacity || 1 }}인<br />
              <i class="bi bi-currency-dollar me-1"></i>{{ room.price ? formatPrice(room.price) : "--" }}
            </p>
            <div class="mt-auto d-grid gap-2">
              <router-link :to="`/accommodation/room/${room.roomId}`" class="btn btn-outline-primary btn-sm">
                상세 보기
              </router-link>
              <button class="btn btn-primary btn-sm" @click="showReservationForm(room.roomId)">예약하기</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 리뷰 섹션 -->
    <div class="row mb-5" v-if="!loading">
      <div class="col-12">
        <div class="card detail-card">
          <div class="card-body">
            <h4 class="card-title">리뷰 ({{ reviewCount }})</h4>

            <!-- 리뷰 작성 폼 (로그인 시) -->
            <div v-if="isLoggedIn && !userHasReviewed && canUserReview" class="mb-4 p-3 border rounded bg-light">
              <h5>리뷰 작성하기</h5>
              <div class="mb-2">
                <label for="rating" class="form-label">별점:</label>
                <select
                  v-model="newReview.rating"
                  id="rating"
                  class="form-select form-select-sm w-auto d-inline-block ms-2"
                >
                  <option value="5">★★★★★ (5점)</option>
                  <option value="4">★★★★☆ (4점)</option>
                  <option value="3">★★★☆☆ (3점)</option>
                  <option value="2">★★☆☆☆ (2점)</option>
                  <option value="1">★☆☆☆☆ (1점)</option>
                </select>
              </div>
              <div class="mb-2">
                <label for="reviewContent" class="form-label">내용:</label>
                <textarea
                  v-model="newReview.content"
                  id="reviewContent"
                  class="form-control"
                  rows="3"
                  placeholder="숙소에서의 경험을 공유해주세요."
                ></textarea>
              </div>
              <button @click="submitReview" class="btn btn-primary btn-sm" :disabled="isSubmittingReview">
                <span
                  v-if="isSubmittingReview"
                  class="spinner-border spinner-border-sm"
                  role="status"
                  aria-hidden="true"
                ></span>
                리뷰 등록
              </button>
              <p v-if="reviewError" class="text-danger mt-2">{{ reviewError }}</p>
            </div>
            <div v-else-if="isLoggedIn && userHasReviewed" class="mb-3 p-3 border rounded bg-light-info">
              <p class="mb-0"><i class="bi bi-info-circle-fill me-1"></i> 이미 이 숙소에 대한 리뷰를 작성하셨습니다.</p>
            </div>
            <div
              v-else-if="isLoggedIn && !canUserReview && fetchReviewEligibilityAttempted"
              class="mb-3 p-3 border rounded bg-light-warning"
            >
              <p class="mb-0">
                <i class="bi bi-exclamation-triangle-fill me-1"></i> 이 숙소를 이용한 내역이 없어 리뷰를 작성할 수
                없습니다.
              </p>
            </div>

            <!-- 리뷰 목록 -->
            <div v-if="reviews.length > 0">
              <div v-for="review in reviews" :key="review.reviewId" class="border-bottom py-3">
                <div class="d-flex justify-content-between align-items-start">
                  <div>
                    <strong class="me-2">{{ review.userNickname || "익명 사용자" }}</strong>
                    <small class="text-muted">{{ formatDate(review.createdAt) }}</small>
                  </div>
                  <div>
                    <span v-for="n in 5" :key="n" class="me-0">
                      <i :class="n <= review.rating ? 'bi bi-star-fill text-warning' : 'bi bi-star text-warning'"></i>
                    </span>
                  </div>
                </div>
                <p class="mt-1 mb-2">{{ review.content }}</p>
                <div v-if="review.imageUrl" class="mb-2">
                  <img
                    :src="review.imageUrl"
                    alt="리뷰 이미지"
                    style="max-width: 200px; max-height: 200px; border-radius: 4px"
                  />
                </div>
                <div v-if="isLoggedIn && review.userId === userId">
                  <button @click="openEditReviewModal(review)" class="btn btn-outline-secondary btn-sm me-2">
                    수정
                  </button>
                  <button
                    @click="deleteReview(review.reviewId)"
                    class="btn btn-outline-danger btn-sm"
                    :disabled="isDeletingReview === review.reviewId"
                  >
                    <span
                      v-if="isDeletingReview === review.reviewId"
                      class="spinner-border spinner-border-sm"
                      role="status"
                      aria-hidden="true"
                    ></span>
                    삭제
                  </button>
                </div>
              </div>
            </div>
            <div v-else class="text-center text-muted py-3">
              <p>아직 작성된 리뷰가 없습니다. 첫 리뷰를 남겨주세요!</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 리뷰 수정 모달 -->
    <div
      v-if="showEditReviewModal"
      class="modal fade show d-block"
      tabindex="-1"
      style="background-color: rgba(0, 0, 0, 0.5)"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">리뷰 수정</h5>
            <button type="button" class="btn-close" @click="showEditReviewModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-2">
              <label for="editRating" class="form-label">별점:</label>
              <select
                v-model="editableReview.rating"
                id="editRating"
                class="form-select form-select-sm w-auto d-inline-block ms-2"
              >
                <option value="5">★★★★★ (5점)</option>
                <option value="4">★★★★☆ (4점)</option>
                <option value="3">★★★☆☆ (3점)</option>
                <option value="2">★★☆☆☆ (2점)</option>
                <option value="1">★☆☆☆☆ (1점)</option>
              </select>
            </div>
            <div class="mb-2">
              <label for="editReviewContent" class="form-label">내용:</label>
              <textarea
                v-model="editableReview.content"
                id="editReviewContent"
                class="form-control"
                rows="3"
              ></textarea>
            </div>
            <p v-if="editReviewError" class="text-danger mt-2">{{ editReviewError }}</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="showEditReviewModal = false">취소</button>
            <button type="button" class="btn btn-primary" @click="submitEditReview" :disabled="isSubmittingEditReview">
              <span
                v-if="isSubmittingEditReview"
                class="spinner-border spinner-border-sm"
                role="status"
                aria-hidden="true"
              ></span>
              수정 완료
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";
import noImage from "@/assets/no-image.jpg";
import axios from "axios";

export default {
  name: "AccommodationDetail",
  props: { id: { type: [String, Number], required: true } },
  data() {
    return {
      accommodation: {},
      rooms: [],
      reviews: [],
      loading: true,
      message: "",
      isFavorite: false,
      favoriteId: null,
      averageRating: 0,
      reviewCount: 0,
      toasts: [],
      activeSlide: 0,
      checkInDate: "",
      checkOutDate: "",
      guestCount: 1,
      noImage,
      newReview: {
        rating: 5,
        content: "",
      },
      isSubmittingReview: false,
      reviewError: "",
      userHasReviewed: false,
      canUserReview: false,
      fetchReviewEligibilityAttempted: false,
      showEditReviewModal: false,
      editableReview: {
        reviewId: null,
        rating: 0,
        content: "",
      },
      isSubmittingEditReview: false,
      editReviewError: "",
      isDeletingReview: null,
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: (s) => s.user.isLoggedIn,
      userId: (s) => s.user.user?.id,
    }),
    isHost() {
      return this.accommodation && this.accommodation.hostId && this.userId === this.accommodation.hostId;
    },
    amenitiesList() {
      return this.accommodation.amenities ? this.accommodation.amenities.split(",").map((i) => i.trim()) : [];
    },
    images() {
      if (this.accommodation.images && this.accommodation.images.length) {
        return this.accommodation.images.map((i) => i.url || this.noImage);
      }
      if (this.accommodation.mainImageUrl) {
        return [this.accommodation.mainImageUrl];
      }
      return [this.noImage];
    },
  },
  created() {
    this.fetchData();
  },
  methods: {
    async fetchData() {
      this.loading = true;
      try {
        await this.fetchAccommodation();
        await this.fetchRooms();
        await this.fetchReviews();
        await this.fetchReviewSummary();
        if (this.isLoggedIn) {
          await this.checkUserReviewStatus();
          await this.checkReviewEligibility();
        }
      } catch (e) {
        console.error("데이터를 불러오는데 실패했습니다:", e);
        this.showToast("데이터를 불러오는데 실패했습니다.");
      } finally {
        this.loading = false;
      }
    },
    async fetchAccommodation() {
      try {
        const res = await axios.get(`/api/accommodations/${this.id}`);
        if (res.data) {
          this.accommodation = res.data.accommodation || {};
          this.rooms = res.data.rooms || [];
        } else {
          throw new Error("숙소 정보를 찾을 수 없습니다.");
        }
      } catch (err) {
        this.showToast(err.response?.data?.error || err.message || "숙소 정보 요청 실패");
        console.error("Error fetching accommodation:", err);
        throw err;
      }
    },
    async fetchRooms() {},
    async fetchReviews() {
      try {
        const response = await axios.get(`/api/reviews/accommodation/${this.id}`);
        this.reviews = response.data || [];
      } catch (error) {
        console.error("Error fetching reviews:", error);
        this.showToast("리뷰 목록을 불러오는데 실패했습니다.");
        this.reviews = [];
      }
    },
    async fetchReviewSummary() {
      try {
        const response = await axios.get(`/api/reviews/summary/accommodation/${this.id}`);
        if (response.data) {
          this.averageRating = response.data.averageRating || 0;
          this.reviewCount = response.data.totalReviews || 0;
        }
      } catch (error) {
        console.error("Error fetching review summary:", error);
      }
    },
    async checkUserReviewStatus() {
      this.userHasReviewed = this.reviews.some((review) => review.userId === this.userId);
    },
    async checkReviewEligibility() {
      this.fetchReviewEligibilityAttempted = true;
      if (!this.isLoggedIn) {
        this.canUserReview = false;
        return;
      }
      try {
        this.canUserReview = false;
      } catch (error) {
        console.error("Error checking review eligibility:", error);
        this.canUserReview = false;
      }
    },
    async submitReview() {
      if (!this.newReview.content.trim()) {
        this.reviewError = "리뷰 내용을 입력해주세요.";
        return;
      }
      this.isSubmittingReview = true;
      this.reviewError = "";
      try {
        const payload = {
          accommodationId: parseInt(this.id),
          rating: parseInt(this.newReview.rating),
          content: this.newReview.content,
        };
        await axios.post(`/api/reviews?userId=${this.userId}`, payload);
        this.showToast("리뷰가 성공적으로 등록되었습니다.");
        this.newReview.rating = 5;
        this.newReview.content = "";
        await this.fetchReviews();
        await this.fetchReviewSummary();
        this.userHasReviewed = true;
        this.canUserReview = false;
      } catch (error) {
        console.error("Error submitting review:", error);
        if (error.response && error.response.status === 403) {
          this.reviewError =
            error.response.data || "이 숙소에 대한 리뷰를 작성할 자격이 없습니다. (예: 이용 내역 없음)";
          this.canUserReview = false;
        } else {
          this.reviewError =
            error.response?.data?.message || error.response?.data || error.message || "리뷰 등록에 실패했습니다.";
        }
      } finally {
        this.isSubmittingReview = false;
      }
    },
    openEditReviewModal(review) {
      this.editableReview.reviewId = review.reviewId;
      this.editableReview.rating = review.rating;
      this.editableReview.content = review.content;
      this.editReviewError = "";
      this.showEditReviewModal = true;
    },
    async submitEditReview() {
      if (!this.editableReview.content.trim()) {
        this.editReviewError = "리뷰 내용을 입력해주세요.";
        return;
      }
      this.isSubmittingEditReview = true;
      this.editReviewError = "";
      try {
        const payload = {
          rating: parseInt(this.editableReview.rating),
          content: this.editableReview.content,
        };
        await axios.put(`/api/reviews/${this.editableReview.reviewId}?userId=${this.userId}`, payload);
        this.showToast("리뷰가 성공적으로 수정되었습니다.");
        this.showEditReviewModal = false;
        await this.fetchReviews();
        await this.fetchReviewSummary();
      } catch (error) {
        console.error("Error editing review:", error);
        this.editReviewError = error.response?.data?.message || error.response?.data || "리뷰 수정에 실패했습니다.";
      } finally {
        this.isSubmittingEditReview = false;
      }
    },
    async deleteReview(reviewId) {
      if (!confirm("정말로 이 리뷰를 삭제하시겠습니까?")) return;
      this.isDeletingReview = reviewId;
      try {
        await axios.delete(`/api/reviews/${reviewId}?userId=${this.userId}`);
        this.showToast("리뷰가 성공적으로 삭제되었습니다.");
        await this.fetchReviews();
        await this.fetchReviewSummary();
        const deletedReview = this.reviews.find((r) => r.reviewId === reviewId);
        if (deletedReview && deletedReview.userId === this.userId) {
          this.userHasReviewed = false;
        }
      } catch (error) {
        console.error("Error deleting review:", error);
        this.showToast(error.response?.data?.message || error.response?.data || "리뷰 삭제에 실패했습니다.");
      } finally {
        this.isDeletingReview = null;
      }
    },
    formatDate(dateString) {
      if (!dateString) return "";
      const date = new Date(dateString);
      return date.toLocaleDateString("ko-KR", {
        year: "numeric",
        month: "long",
        day: "numeric",
      });
    },
    prevSlide() {
      this.activeSlide = (this.activeSlide + this.images.length - 1) % this.images.length;
    },
    nextSlide() {
      this.activeSlide = (this.activeSlide + 1) % this.images.length;
    },
    goToSlide(idx) {
      this.activeSlide = idx;
    },
    bookNow() {
      if (!this.isLoggedIn) return this.showToast("로그인이 필요합니다");
      this.$router.push({
        path: "/reservation/form",
        query: {
          accommodationId: this.id,
          checkIn: this.checkInDate,
          checkOut: this.checkOutDate,
          guests: this.guestCount,
        },
      });
    },
    formatPrice(price) {
      return new Intl.NumberFormat("ko-KR", { style: "currency", currency: "KRW" }).format(price);
    },
    showToast(msg) {
      this.toasts.push({ message: msg });
      setTimeout(() => this.toasts.shift(), 3000);
    },
    showReservationForm(roomId) {
      this.$router.push({
        path: "/reservation/form",
        query: {
          roomId,
          accommodationId: this.id,
          checkIn: this.checkInDate,
          checkOut: this.checkOutDate,
          guests: this.guestCount,
        },
      });
    },
  },
};
</script>

<style scoped>
:root {
  --yanolja-red: #f0213b;
}

.carousel-wrapper {
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  height: 450px;
}
.carousel-inner {
  position: relative;
  width: 100%;
  height: 100%;
}
.carousel-item {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 0.5s ease;
}
.carousel-item.active {
  position: relative;
  opacity: 1;
}
.carousel-wrapper img {
  object-fit: cover;
  height: 100%;
}
.carousel-prev {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.4);
  border: none;
  padding: 8px;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  left: 10px;
}
.carousel-next {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.4);
  border: none;
  padding: 8px;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  right: 10px;
}
.carousel-thumbs .thumb {
  width: 80px;
  height: 60px;
  object-fit: cover;
  cursor: pointer;
  border: 2px solid transparent;
  border-radius: 4px;
  transition: border-color 0.3s;
}
.carousel-thumbs .thumb.active {
  border-color: var(--yanolja-red);
}
.sticky-box {
  position: sticky;
  top: 100px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.price {
  font-size: 1.8rem;
  color: var(--yanolja-red);
  margin-bottom: 1rem;
}
.detail-card {
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
.room-card {
  border: none;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}
.room-card:hover {
  transform: translateY(-5px);
}
.room-card .card-img-top {
  height: 180px;
  object-fit: cover;
}
.amenities-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  list-style: none;
  padding: 0;
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
.review-summary-header {
  font-size: 1rem;
}
.bg-light-info {
  background-color: #e7f3ff !important;
  border-color: #b8d6f3 !important;
}
.bg-light-warning {
  background-color: #fff3cd !important;
  border-color: #ffeeba !important;
}
</style>
