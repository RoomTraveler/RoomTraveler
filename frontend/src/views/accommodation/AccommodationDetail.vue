<template>
  <div class="bg-gray-50 min-h-screen">
    <div class="max-w-[768px] mx-auto">
      <!-- MOVED MAIN WRAPPER TO INCLUDE HEADER -->
      <AccommodationHeader :accommodationTitle="accommodation.title" />

      <!-- 로딩 상태 표시 -->
      <div v-if="loading && !fetchError" class="py-20 flex flex-col items-center justify-center">
        <div class="w-12 h-12 border-4 border-pink-200 border-t-pink-500 rounded-full animate-spin mb-4"></div>
        <p class="text-lg text-gray-600">숙소 정보를 불러오는 중입니다...</p>
      </div>

      <!-- 전체 데이터 로딩 오류 상태 표시 -->
      <div v-else-if="fetchError && !accommodation.accommodationId" class="py-20 px-4 text-center">
        <i class="bi bi-exclamation-triangle-fill text-5xl text-red-400 mb-4"></i>
        <p class="text-xl text-red-600 mb-2">숙소 정보를 불러오지 못했습니다</p>
        <p class="text-gray-600 mb-4">{{ fetchError }}</p>
        <button
          @click="fetchData"
          class="mt-6 px-6 py-2 bg-pink-500 text-white rounded-md hover:bg-pink-600 transition"
        >
          다시 시도
        </button>
      </div>

      <!-- 콘텐츠 영역 (이제 max-w는 상위에서 처리) -->
      <div v-else class="pb-16">
        <!-- 메인 이미지 -->
        <div class="mt-4">
          <img
            :src="accommodation.mainImageUrl || noImage"
            alt="숙소 메인 이미지"
            class="w-full h-[507px] object-cover rounded-lg shadow-md"
          />
        </div>

        <!-- 숙소 정보 (제목, 찜/공유, 위치, 리뷰 요약 캐러셀) -->
        <div class="mt-6 bg-white p-6 rounded-lg shadow">
          <div class="flex justify-between items-start mb-3">
            <h1 class="text-3xl font-bold text-gray-800 flex-1 mr-4 leading-tight">
              {{ accommodation.title || "숙소명 없음" }}
            </h1>
            <div class="flex items-center gap-x-3 flex-shrink-0">
              <button @click="toggleWishlist" class="text-gray-500 hover:text-pink-500 transition">
                <i class="bi text-2xl" :class="isFavorite ? 'bi-heart-fill text-pink-500' : 'bi-heart'"></i>
              </button>
              <button @click="shareAccommodation" class="text-gray-500 hover:text-blue-500 transition">
                <i class="bi bi-share-fill text-2xl"></i>
              </button>
            </div>
          </div>
          <div class="text-gray-600 mb-1 flex items-center">
            <i class="bi bi-geo-alt-fill mr-2 text-gray-500"></i>
            <span
              >{{ accommodation.address || accommodation.sidoName || "위치 정보 없음" }}
              {{ accommodation.gugunName || "" }}</span
            >
          </div>
          <div
            v-if="reviewCount > 0 && !loadingReviews && !fetchReviewsError"
            class="flex items-center text-sm text-gray-600 mb-1"
          >
            <i class="bi bi-star-fill text-yellow-400 mr-1"></i>
            <span class="font-semibold">{{ averageRating.toFixed(1) }}</span>
            <span class="ml-1">({{ reviewCount }}개의 리뷰)</span>
          </div>
          <p v-else-if="!loadingReviews && !fetchReviewsError && reviewCount === 0" class="text-sm text-gray-500 mb-1">
            첫 리뷰를 작성해주세요!
          </p>
          <p v-else-if="loadingReviews" class="text-sm text-gray-500 mb-1">리뷰를 불러오는 중...</p>
          <p v-else-if="fetchReviewsError" class="text-sm text-red-500 mb-1">리뷰를 불러오는데 실패했습니다.</p>

          <hr class="my-4" />
        </div>

        <!-- 리뷰 전체 섹션 -->
        <div class="mt-8 bg-white p-6 rounded-lg shadow border border-gray-200" id="reviews-section">
          <h2 class="text-2xl font-bold text-gray-800 mb-4">방문자 리뷰 (총 {{ reviewCount }}개)</h2>

          <!-- New Review Carousel Section - STARTS HERE -->
          <template v-if="reviewCarouselItems.length > 0">
            <div class="w-full md:w-[728px] mx-auto relative mb-6" style="height: 174.5px">
              <div class="overflow-hidden h-full rounded-md border border-gray-200">
                <div
                  class="flex transition-transform duration-300 ease-in-out h-full"
                  :style="{ transform: `translateX(-${currentReviewSlideIndex * 100}%)` }"
                >
                  <div
                    v-for="(item, index) in reviewCarouselItems"
                    :key="item.type === 'review' ? item.review.reviewId : 'see-all'"
                    class="w-full flex-shrink-0 h-full p-4 box-border bg-white flex flex-col justify-between"
                  >
                    <template v-if="item.type === 'review'">
                      <div>
                        <div class="flex justify-between items-center mb-1">
                          <span class="font-semibold text-sm text-gray-800">{{
                            item.review.userNickname || "익명"
                          }}</span>
                          <div class="flex items-center">
                            <span
                              v-for="n in 5"
                              :key="n + 'crs_star'"
                              class="mr-0.5 text-xs"
                              :class="n <= item.review.rating ? 'text-yellow-400' : 'text-gray-300'"
                              >★</span
                            >
                          </div>
                        </div>
                        <p class="text-xs text-gray-600 leading-relaxed line-clamp-3 mb-1">{{ item.review.content }}</p>
                      </div>
                      <span class="text-xxs text-gray-400 mt-auto self-start">{{
                        formatDate(item.review.createdAt)
                      }}</span>
                    </template>
                    <template v-else-if="item.type === 'see-all'">
                      <div class="flex flex-col items-center justify-center h-full text-center">
                        <i class="bi bi-card-text text-3xl text-pink-500 mb-2"></i>
                        <p class="font-semibold text-pink-600 mb-1">모든 리뷰 보기</p>
                        <p class="text-xs text-gray-500">총 {{ reviewCount }}개의 리뷰를 확인하세요.</p>
                        <button
                          @click="scrollToReviews"
                          class="mt-3 px-4 py-1.5 text-xs bg-pink-500 text-white rounded-md hover:bg-pink-600 transition"
                        >
                          전체 리뷰 보기
                        </button>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
              <button
                v-if="canPrevReviewSlide"
                @click="prevReviewSlide"
                class="absolute top-1/2 left-1 transform -translate-y-1/2 bg-white/80 hover:bg-white rounded-full p-1.5 shadow-md z-10 focus:outline-none focus:ring-2 focus:ring-pink-300"
              >
                <i class="bi bi-chevron-left text-gray-700 text-lg"></i>
              </button>
              <button
                v-if="canNextReviewSlide"
                @click="nextReviewSlide"
                class="absolute top-1/2 right-1 transform -translate-y-1/2 bg-white/80 hover:bg-white rounded-full p-1.5 shadow-md z-10 focus:outline-none focus:ring-2 focus:ring-pink-300"
              >
                <i class="bi bi-chevron-right text-gray-700 text-lg"></i>
              </button>
            </div>
          </template>
          <!-- New Review Carousel Section - ENDS HERE -->

          <div
            v-if="isLoggedIn && !userHasReviewed && canUserReview"
            class="mb-6 p-4 border border-gray-200 rounded-lg bg-gray-50"
          >
            <h5 class="text-lg font-semibold text-gray-700 mb-3">리뷰 작성하기</h5>
            <div class="mb-3">
              <label for="rating" class="block text-sm font-medium text-gray-700 mb-1">별점:</label>
              <select
                v-model="newReview.rating"
                id="rating"
                class="block w-full sm:w-auto p-2 border border-gray-300 rounded-md shadow-sm focus:ring-pink-500 focus:border-pink-500 text-sm"
              >
                <option value="5">★★★★★ (5점)</option>
                <option value="4">★★★★☆ (4점)</option>
                <option value="3">★★★☆☆ (3점)</option>
                <option value="2">★★☆☆☆ (2점)</option>
                <option value="1">★☆☆☆☆ (1점)</option>
              </select>
            </div>
            <div class="mb-3">
              <label for="reviewContent" class="block text-sm font-medium text-gray-700 mb-1">내용:</label>
              <textarea
                v-model="newReview.content"
                id="reviewContent"
                class="block w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-pink-500 focus:border-pink-500 text-sm"
                rows="4"
                placeholder="숙소에서의 경험을 공유해주세요."
              ></textarea>
            </div>
            <button
              @click="submitReview"
              class="w-full sm:w-auto px-4 py-2 bg-pink-500 text-white rounded-md hover:bg-pink-600 transition font-medium text-sm"
              :disabled="isSubmittingReview"
            >
              <span
                v-if="isSubmittingReview"
                class="spinner-border spinner-border-sm mr-1"
                role="status"
                aria-hidden="true"
              ></span>
              리뷰 등록
            </button>
            <p v-if="reviewError" class="text-red-500 text-sm mt-2">{{ reviewError }}</p>
          </div>

          <div v-if="reviewCount > 0 && reviews.length > 0" class="space-y-4">
            <div
              v-for="review in reviews"
              :key="review.reviewId + '-full'"
              class="border border-gray-200 p-4 rounded-lg bg-white hover:shadow-md transition-shadow"
            >
              <div class="flex justify-between items-start mb-1">
                <div>
                  <strong class="text-gray-800 font-semibold">{{ review.userNickname || "익명 사용자" }}</strong>
                  <div class="flex items-center mt-0.5">
                    <span
                      v-for="n in 5"
                      :key="n + 'star_full_list'"
                      class="mr-0.5 text-sm"
                      :class="n <= review.rating ? 'text-yellow-400' : 'text-gray-300'"
                      >★</span
                    >
                  </div>
                </div>
                <small class="text-gray-500 text-xs">{{ formatDate(review.createdAt) }}</small>
              </div>
              <p class="text-gray-700 text-sm leading-relaxed mt-1 mb-2">{{ review.content }}</p>
              <div v-if="review.imageUrl" class="my-2">
                <img
                  :src="review.imageUrl"
                  alt="리뷰 이미지"
                  class="max-w-xs max-h-48 rounded-md border border-gray-200"
                />
              </div>
              <div v-if="isLoggedIn && review.userId === userId" class="mt-2 flex gap-x-2">
                <button
                  @click="openEditReviewModal(review)"
                  class="px-3 py-1 text-xs font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-md border border-gray-300 transition"
                >
                  수정
                </button>
                <button
                  @click="deleteReview(review.reviewId)"
                  class="px-3 py-1 text-xs font-medium text-red-600 bg-red-50 hover:bg-red-100 rounded-md border border-red-200 transition"
                  :disabled="isDeletingReview === review.reviewId"
                >
                  <span
                    v-if="isDeletingReview === review.reviewId"
                    class="spinner-border spinner-border-sm mr-1"
                    role="status"
                    aria-hidden="true"
                  ></span>
                  삭제
                </button>
              </div>
            </div>
          </div>
          <div
            v-else
            class="w-full min-h-[100px] flex flex-col items-center justify-center text-center p-4 border-t border-gray-100 mt-4"
          >
            <template v-if="loadingReviews">
              <i class="bi bi-arrow-clockwise text-3xl text-pink-500 mb-3 animate-spin"></i>
              <p class="text-gray-600 font-medium">리뷰를 불러오는 중...</p>
            </template>
            <template v-else-if="fetchReviewsError">
              <i class="bi bi-exclamation-circle text-3xl text-red-500 mb-3"></i>
              <p class="text-gray-600 font-medium">리뷰를 불러오지 못했습니다.</p>
              <button
                @click="fetchReviewsAndSummary"
                class="mt-3 px-4 py-1.5 text-sm bg-pink-500 text-white rounded-md hover:bg-pink-600 transition"
              >
                다시 시도
              </button>
            </template>
            <template v-else-if="!loadingReviews && reviewCount === 0">
              <i class="bi bi-chat-square-text text-3xl text-gray-400 mb-3"></i>
              <p class="text-gray-600 font-medium">아직 등록된 리뷰가 없습니다.</p>
            </template>
          </div>
        </div>

        <!-- 객실 선택 UI (id 추가) -->
        <div class="mt-8 bg-white p-6 rounded-lg shadow border border-gray-200" id="booking-options-section">
          <h2 class="text-2xl font-bold text-gray-800 mb-5">객실 선택</h2>
          <div class="w-full flex justify-center mb-2">
            <div class="flex w-full max-w-lg justify-between gap-8">
              <!-- 날짜 선택 버튼 -->
              <button
                @click="openDateSelectionModal"
                class="flex-1 p-4 border border-gray-300 rounded-md cursor-pointer hover:border-pink-500 transition focus:outline-none focus:ring-2 focus:ring-pink-300 text-left min-w-[140px] max-w-[230px]"
                aria-label="날짜 선택하기"
              >
                <p class="text-xs text-gray-500 mb-0.5">체크인 - 체크아웃</p>
                <p class="font-semibold text-gray-700">{{ selectedDateRangeDisplay }}</p>
              </button>
              <!-- 인원 선택 버튼 -->
              <button
                @click="openGuestSelectionModal"
                class="flex-1 p-4 border border-gray-300 rounded-md cursor-pointer hover:border-pink-500 transition focus:outline-none focus:ring-2 focus:ring-pink-300 text-left min-w-[90px] max-w-[150px]"
                aria-label="인원 선택하기"
              >
                <p class="text-xs text-gray-500 mb-0.5">인원</p>
                <p class="font-semibold text-gray-700">{{ selectedGuestCountDisplay }}</p>
              </button>
            </div>
          </div>
        </div>

        <!-- 객실 목록 (id 추가) -->
        <div class="mt-8" id="room-list-section">
          <div v-if="rooms && rooms.length > 0" class="space-y-6">
            <div
              v-for="room in rooms"
              :key="room.roomId"
              class="w-full bg-white p-0 rounded-lg shadow flex flex-col sm:flex-row overflow-hidden min-h-[301px] border border-gray-200 hover:shadow-lg transition-shadow duration-300"
            >
              <div class="w-full sm:w-1/2 h-48 sm:h-full flex-shrink-0">
                <img
                  :src="room.mainImageUrl || noImage"
                  :alt="room.name || '객실 이미지'"
                  class="w-full h-full object-cover"
                />
              </div>
              <div class="w-full sm:w-1/2 p-5 flex flex-col justify-between">
                <div>
                  <h3 class="text-xl font-semibold text-gray-800 mb-2">{{ room.name || "객실명 없음" }}</h3>
                  <p class="text-sm text-gray-600 mb-1">
                    <i class="bi bi-people-fill mr-1.5"></i>수용 인원: {{ room.capacity || "정보 없음" }}명
                  </p>
                  <p class="text-sm text-gray-600 mb-3">
                    <i class="bi bi-aspect-ratio-fill mr-1.5"></i>침대 종류: {{ room.bedType || "정보 없음" }}
                  </p>
                </div>
                <div class="mt-auto">
                  <p class="text-lg font-bold text-pink-600 mb-2">{{ formatPrice(room.price) }} / 박</p>
                  <p class="text-xs text-gray-500 mb-1">체크인: {{ room.checkInTime || "15:00" }}</p>
                  <p class="text-xs text-gray-500 mb-3">체크아웃: {{ room.checkOutTime || "11:00" }}</p>
                  <button
                    @click="goToRoomDetail(room.roomId)"
                    class="w-full bg-pink-500 text-white py-2.5 rounded-md hover:bg-pink-600 transition font-medium"
                  >
                    객실 상세보기
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div v-else-if="!loading" class="text-center py-10 bg-white p-6 rounded-lg shadow border border-gray-200">
            <i class="bi bi-door-closed text-4xl text-gray-400 mb-3"></i>
            <p class="text-gray-500">등록된 객실 정보가 없습니다.</p>
          </div>
        </div>

        <!-- 호텔(숙소) 소개 -->
        <div class="mt-8 bg-white p-6 rounded-lg shadow border border-gray-200" id="accommodation-description-section">
          <h2 class="text-2xl font-bold text-gray-800 mb-4">숙소 소개</h2>
          <p class="text-gray-700 leading-relaxed whitespace-pre-line mb-6">
            {{ accommodation.description || "등록된 숙소 설명이 없습니다." }}
          </p>
          <h3 class="text-xl font-semibold text-gray-800 mb-3">편의시설</h3>
          <ul v-if="amenitiesArray.length > 0" class="grid grid-cols-2 sm:grid-cols-3 gap-x-4 gap-y-2 text-gray-700">
            <li v-for="(amenity, index) in amenitiesArray" :key="index" class="flex items-center">
              <i class="bi bi-check-circle-fill text-green-500 mr-2"></i>
              <span>{{ amenity }}</span>
            </li>
          </ul>
          <p v-else class="text-gray-500">등록된 편의시설 정보가 없습니다.</p>
        </div>
      </div>
      <!-- END OF MOVED MAIN WRAPPER CONTENT -->
    </div>

    <!-- 날짜 선택 모달 -->
    <el-dialog v-model="showDateModal" title="날짜 선택" width="90%" top="5vh" custom-class="date-selection-dialog">
      <div class="dialog-content px-2 sm:px-4">
        <button
          @click="showDateModal = false"
          class="absolute top-4 right-4 text-gray-500 hover:text-gray-700 text-2xl z-10"
        >
          <i class="bi bi-x"></i>
        </button>
        <p class="text-sm text-gray-600 bg-gray-100 p-3 rounded-md mb-4">
          <i class="bi bi-info-circle-fill mr-1.5"></i>이 숙소는 최대 9박까지 예약할 수 있어요.
        </p>

        <el-calendar v-model="calendarDate">
          <template #header="{ date }">
            <div class="flex justify-between items-center w-full">
              <el-button link @click="prevMonth">&lt; 이전 달</el-button>
              <span class="text-lg font-semibold">{{ date }}</span>
              <el-button link @click="nextMonth">다음 달 &gt;</el-button>
            </div>
          </template>
          <template #date-cell="{ data }">
            <div
              class="date-cell-content w-full h-full flex flex-col items-center justify-center"
              :class="{ 'is-selected': isDateSelected(data.day), 'is-disabled': isDateDisabled(data.day) }"
              @click="handleDateClick(data.day)"
            >
              <p class="day-number" :class="{ 'text-red-500': [0, 6].includes(new Date(data.day).getDay()) }">
                {{ data.day.split("-").slice(2).join("") }}
              </p>
              <p v-if="getDatePrice(data.day)" class="text-xs text-gray-500 mt-1">{{ getDatePrice(data.day) }}</p>
            </div>
          </template>
        </el-calendar>

        <p class="text-xs text-gray-500 mt-4 text-center">가격: 1박 기준 (단위: 만원)</p>
      </div>
      <template #footer>
        <div class="flex justify-between items-center w-full px-2 sm:px-4 pb-2">
          <el-button @click="resetDateSelection" link class="text-gray-600 hover:text-pink-500">초기화</el-button>
          <el-button
            type="primary"
            @click="confirmDateSelectionAndCloseModal"
            class="bg-pink-500 hover:bg-pink-600 border-pink-500 flex-grow sm:flex-grow-0 min-w-[150px]"
          >
            {{ selectedRangeFooterDisplay }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 인원 선택 모달 -->
    <el-dialog
      v-model="showGuestModal"
      title="인원 선택"
      width="90%"
      :max-width="'400px'"
      top="15vh"
      custom-class="guest-selection-dialog"
      :center="true"
    >
      <div class="dialog-content px-2 sm:px-4">
        <button
          @click="showGuestModal = false"
          class="absolute top-4 right-4 text-gray-500 hover:text-gray-700 text-2xl z-10"
        >
          <i class="bi bi-x"></i>
        </button>
        <div class="py-4">
          <div class="flex justify-between items-center mb-6">
            <div class="flex flex-col">
              <span class="text-lg font-medium text-gray-800">성인</span>
              <span class="text-xs text-gray-500">만 13세 이상</span>
            </div>
            <div class="flex items-center gap-x-3">
              <button
                @click="decrementAdults"
                :disabled="tempSelectedAdults <= 1"
                class="p-2 w-8 h-8 rounded-full border border-gray-300 text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
              >
                <i class="bi bi-dash-lg"></i>
              </button>
              <span class="text-lg font-medium w-6 text-center">{{ tempSelectedAdults }}</span>
              <button
                @click="incrementAdults"
                :disabled="tempSelectedAdults >= 10"
                class="p-2 w-8 h-8 rounded-full border border-gray-300 text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
              >
                <i class="bi bi-plus-lg"></i>
              </button>
            </div>
          </div>

          <div class="flex justify-between items-center">
            <div class="flex flex-col">
              <span class="text-lg font-medium text-gray-800">아동</span>
              <span class="text-xs text-gray-500">만 12세 이하</span>
            </div>
            <div class="flex items-center gap-x-3">
              <button
                @click="decrementChildren"
                :disabled="tempSelectedChildren <= 0"
                class="p-2 w-8 h-8 rounded-full border border-gray-300 text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
              >
                <i class="bi bi-dash-lg"></i>
              </button>
              <span class="text-lg font-medium w-6 text-center">{{ tempSelectedChildren }}</span>
              <button
                @click="incrementChildren"
                :disabled="tempSelectedChildren >= 5"
                class="p-2 w-8 h-8 rounded-full border border-gray-300 text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
              >
                <i class="bi bi-plus-lg"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="w-full px-2 sm:px-4 pb-2">
          <el-button
            type="primary"
            @click="confirmGuestSelectionAndCloseModal"
            class="w-full bg-pink-500 hover:bg-pink-600 border-pink-500 py-3 text-base"
          >
            확인
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 리뷰 수정 모달 -->
    <div
      v-if="showEditReviewModal"
      class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-[100]"
      @click.self="showEditReviewModal = false"
    >
      <div
        class="relative top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 p-5 border w-full max-w-md shadow-lg rounded-md bg-white"
      >
        <div class="flex justify-between items-center pb-3 border-b">
          <h5 class="text-lg font-semibold text-gray-900">리뷰 수정</h5>
          <button @click="showEditReviewModal = false" class="text-gray-400 hover:text-gray-600 p-1 rounded-full">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="mt-3">
          <div class="mb-4">
            <label for="editRating" class="block text-sm font-medium text-gray-700 mb-1">별점:</label>
            <select
              v-model="editableReview.rating"
              id="editRating"
              class="block w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-pink-500 focus:border-pink-500 text-sm"
            >
              <option value="5">★★★★★ (5점)</option>
              <option value="4">★★★★☆ (4점)</option>
              <option value="3">★★★☆☆ (3점)</option>
              <option value="2">★★☆☆☆ (2점)</option>
              <option value="1">★☆☆☆☆ (1점)</option>
            </select>
          </div>
          <div class="mb-4">
            <label for="editReviewContent" class="block text-sm font-medium text-gray-700 mb-1">내용:</label>
            <textarea
              v-model="editableReview.content"
              id="editReviewContent"
              class="block w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-pink-500 focus:border-pink-500 text-sm"
              rows="4"
            ></textarea>
          </div>
          <p v-if="editReviewError" class="text-red-500 text-sm mt-2">{{ editReviewError }}</p>
        </div>
        <div class="mt-4 pt-3 border-t flex justify-end gap-x-2">
          <button
            type="button"
            class="px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 text-sm font-medium transition"
            @click="showEditReviewModal = false"
          >
            취소
          </button>
          <button
            type="button"
            class="px-4 py-2 bg-pink-500 text-white rounded-md hover:bg-pink-600 text-sm font-medium transition"
            @click="submitEditReview"
            :disabled="isSubmittingEditReview"
          >
            <span
              v-if="isSubmittingEditReview"
              class="spinner-border spinner-border-sm mr-1"
              role="status"
              aria-hidden="true"
            ></span>
            수정 완료
          </button>
        </div>
      </div>
    </div>

    <!-- Toast Messages -->
    <div v-if="toasts.length > 0" class="fixed bottom-5 right-5 space-y-3 z-[200] md:max-w-xs w-11/12">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="bg-gray-800 text-white text-sm px-4 py-3 rounded-md shadow-lg animate-toast-in-out w-full"
        role="alert"
      >
        {{ toast.message }}
      </div>
    </div>
  </div>
</template>

<script>
import { useUserStore } from "@/store/userStore";
import AccommodationHeader from "./AccommodationHeader.vue";
import noImage from "@/assets/no-image.jpg";
import axios from "axios";
import { ElDialog, ElButton, ElCalendar } from "element-plus";

const KOREAN_DAYS = ["일", "월", "화", "수", "목", "금", "토"];
const MAX_REVIEWS_IN_CAROUSEL = 10;

export default {
  name: "AccommodationDetail",
  components: {
    AccommodationHeader,
    ElDialog,
    ElButton,
    ElCalendar,
  },
  props: { id: { type: [String, Number], required: true } },
  setup() {
    const userStore = useUserStore();
    if (!userStore.isAuthenticated) {
      userStore.loadUserFromStorage();
    }
    return {};
  },
  data() {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);

    return {
      accommodation: {},
      rooms: [],
      reviews: [], // All fetched reviews
      loading: true,
      loadingReviews: false,
      fetchError: null,
      fetchReviewsError: null,
      isFavorite: false,
      noImage,
      newReview: { rating: 5, content: "" },
      isSubmittingReview: false,
      reviewError: "",
      userHasReviewed: false,
      canUserReview: false,
      fetchReviewEligibilityAttempted: false,
      showEditReviewModal: false,
      editableReview: { reviewId: null, rating: 0, content: "" },
      isSubmittingEditReview: false,
      editReviewError: "",
      isDeletingReview: null,
      averageRating: 0,
      reviewCount: 0,
      toasts: [],
      selectedCheckInDate: today,
      selectedCheckOutDate: tomorrow,
      selectedAdults: 2,
      selectedChildren: 0,
      currentReviewSlideIndex: 0,
      showDateModal: false,
      showGuestModal: false,
      calendarDate: new Date(),
      tempSelectedCheckInDate: null,
      tempSelectedCheckOutDate: null,
      tempSelectedAdults: 2,
      tempSelectedChildren: 0,
    };
  },
  computed: {
    headerTitle() {
      return this.accommodation.title || "숙소 상세정보";
    },
    isLoggedIn() {
      const userStore = useUserStore();
      return userStore.isAuthenticated;
    },
    userId() {
      const userStore = useUserStore();
      return userStore.user?.id;
    },
    amenitiesArray() {
      if (this.accommodation && typeof this.accommodation.amenities === "string") {
        return this.accommodation.amenities
          .split(",")
          .map((amenity) => amenity.trim())
          .filter(Boolean);
      }
      return [];
    },
    selectedDateRangeDisplay() {
      if (!this.selectedCheckInDate || !this.selectedCheckOutDate) return "날짜를 선택하세요";
      const checkInStr = this.formatDateWithDay(this.selectedCheckInDate);
      const checkOutStr = this.formatDateWithDay(this.selectedCheckOutDate);
      const nights = this.calculateNights(this.selectedCheckInDate, this.selectedCheckOutDate);
      return `${checkInStr} ~ ${checkOutStr} • ${nights}박`;
    },
    selectedGuestCountDisplay() {
      let displayText = `성인 ${this.selectedAdults}`;
      if (this.selectedChildren > 0) {
        displayText += `, 아동 ${this.selectedChildren}`;
      }
      return displayText;
    },
    reviewCarouselItems() {
      if (this.loadingReviews || this.fetchReviewsError || this.reviewCount === 0) {
        return [];
      }
      const carouselReviews = this.reviews
        .slice(0, MAX_REVIEWS_IN_CAROUSEL)
        .map((review) => ({ type: "review", review }));
      if (this.reviewCount > 0) {
        // Always add see-all if there are any reviews
        carouselReviews.push({ type: "see-all" });
      }
      return carouselReviews;
    },
    canPrevReviewSlide() {
      return this.currentReviewSlideIndex > 0;
    },
    canNextReviewSlide() {
      return this.currentReviewSlideIndex < this.reviewCarouselItems.length - 1;
    },
    selectedRangeFooterDisplay() {
      if (!this.tempSelectedCheckInDate) return "날짜를 선택해주세요";
      if (!this.tempSelectedCheckOutDate) return `${this.formatDateWithDay(this.tempSelectedCheckInDate)} 선택됨`;
      const nights = this.calculateNights(this.tempSelectedCheckInDate, this.tempSelectedCheckOutDate);
      return `지금부터 ~ ${this.formatDateWithDay(this.tempSelectedCheckOutDate)} • ${nights}박`;
    },
  },
  watch: {
    reviews() {
      this.currentReviewSlideIndex = 0; // Reset slide index when reviews change
    },
  },
  created() {
    this.fetchData();
  },
  methods: {
    async fetchData() {
      this.loading = true;
      this.fetchError = null;
      this.currentReviewSlideIndex = 0;
      try {
        await this.fetchAccommodationAndRooms();
        await this.fetchReviewsAndSummary(); // This will set reviews and reviewCount
        if (this.isLoggedIn) {
          await this.checkUserReviewStatus();
          await this.checkReviewEligibility();
        }
      } catch (e) {
        console.error("데이터를 불러오는데 실패했습니다:", e);
        this.fetchError = e.message || "정보를 불러오는 중 오류가 발생했습니다.";
      } finally {
        this.loading = false;
      }
    },
    async fetchAccommodationAndRooms() {
      try {
        const res = await axios.get(`/api/accommodations/${this.id}`);
        if (res.data && res.data.accommodation) {
          this.accommodation = res.data.accommodation;
          this.rooms = res.data.rooms || (Array.isArray(this.accommodation.rooms) ? this.accommodation.rooms : []);
        } else {
          throw new Error("숙소 정보를 찾을 수 없습니다.");
        }
      } catch (err) {
        console.error("Error fetching accommodation details:", err);
        this.fetchError = err.response?.data?.message || err.message || "숙소 정보를 가져오는데 실패했습니다.";
        if (!this.accommodation.accommodationId) {
          this.accommodation = {};
          this.rooms = [];
        }
        throw err;
      }
    },
    async fetchReviewsAndSummary() {
      this.loadingReviews = true;
      this.fetchReviewsError = null;
      try {
        const [reviewsRes, summaryRes] = await Promise.all([
          axios.get(`/api/reviews/accommodation/${this.id}`),
          axios.get(`/api/reviews/summary/accommodation/${this.id}`),
        ]);
        this.reviews = reviewsRes.data || []; // This updates the reviews data property
        if (summaryRes.data) {
          this.averageRating = summaryRes.data.averageRating || 0;
          this.reviewCount = summaryRes.data.totalReviews || 0;
        } else {
          this.averageRating = 0;
          this.reviewCount = 0;
        }
      } catch (error) {
        console.error("Error fetching reviews or summary:", error);
        this.fetchReviewsError = error.message || "리뷰 정보를 가져오는데 실패했습니다.";
        this.reviews = [];
        this.averageRating = 0;
        this.reviewCount = 0;
      } finally {
        this.loadingReviews = false;
      }
    },
    checkUserReviewStatus() {
      this.userHasReviewed = this.reviews.some((review) => review.userId === this.userId);
    },
    checkReviewEligibility() {
      this.fetchReviewEligibilityAttempted = true;
      this.canUserReview = !this.userHasReviewed;
    },
    async submitReview() {
      if (!this.newReview.content.trim() || !this.newReview.rating) {
        this.reviewError = "별점과 리뷰 내용을 모두 입력해주세요.";
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
        await axios.post(`/api/reviews`, payload);
        this.showToast("리뷰가 성공적으로 등록되었습니다.");
        this.newReview.rating = 5;
        this.newReview.content = "";
        await this.fetchReviewsAndSummary();
        this.userHasReviewed = true;
        this.canUserReview = false;
      } catch (error) {
        console.error("Error submitting review:", error);
        this.reviewError = error.response?.data?.message || "리뷰 등록 중 오류가 발생했습니다.";
      } finally {
        this.isSubmittingReview = false;
      }
    },
    openEditReviewModal(review) {
      this.editableReview = { ...review, rating: review.rating.toString() };
      this.editReviewError = "";
      this.showEditReviewModal = true;
    },
    async submitEditReview() {
      if (!this.editableReview.content.trim() || !this.editableReview.rating) {
        this.editReviewError = "별점과 리뷰 내용을 모두 입력해주세요.";
        return;
      }
      this.isSubmittingEditReview = true;
      this.editReviewError = "";
      try {
        const payload = {
          rating: parseInt(this.editableReview.rating),
          content: this.editableReview.content,
        };
        await axios.put(`/api/reviews/${this.editableReview.reviewId}`, payload);
        this.showToast("리뷰가 성공적으로 수정되었습니다.");
        this.showEditReviewModal = false;
        await this.fetchReviewsAndSummary();
      } catch (error) {
        console.error("Error editing review:", error);
        this.editReviewError = error.response?.data?.message || "리뷰 수정 중 오류가 발생했습니다.";
      } finally {
        this.isSubmittingEditReview = false;
      }
    },
    async deleteReview(reviewId) {
      if (!confirm("정말로 이 리뷰를 삭제하시겠습니까?")) return;
      this.isDeletingReview = reviewId;
      try {
        await axios.delete(`/api/reviews/${reviewId}`);
        this.showToast("리뷰가 성공적으로 삭제되었습니다.");
        await this.fetchReviewsAndSummary();
        this.userHasReviewed = this.reviews.some((r) => r.userId === this.userId && r.reviewId !== reviewId);
        if (!this.userHasReviewed) {
          this.canUserReview = true;
        }
      } catch (error) {
        console.error("Error deleting review:", error);
        this.showToast(error.response?.data?.message || "리뷰 삭제 중 오류가 발생했습니다.");
      } finally {
        this.isDeletingReview = null;
      }
    },
    toggleWishlist() {
      this.isFavorite = !this.isFavorite;
      this.showToast(this.isFavorite ? "찜 목록에 추가되었습니다." : "찜 목록에서 삭제되었습니다.");
      // TODO: API 연동 (찜 추가/삭제)
    },
    shareAccommodation() {
      if (navigator.share) {
        navigator
          .share({
            title: this.accommodation.title,
            text: `${this.accommodation.title} 숙소 정보를 확인해보세요!`,
            url: window.location.href,
          })
          .then(() => console.log("Successful share"))
          .catch((error) => {
            console.log("Error sharing", error);
            this.showToast("공유에 실패했습니다. 링크를 복사해주세요.");
          });
      } else {
        navigator.clipboard
          .writeText(window.location.href)
          .then(() => this.showToast("링크가 클립보드에 복사되었습니다."))
          .catch(() => this.showToast("링크 복사에 실패했습니다."));
      }
    },
    formatDate(dateString) {
      if (!dateString) return "";
      const options = { year: "numeric", month: "short", day: "numeric" }; // short month for carousel
      return new Date(dateString).toLocaleDateString("ko-KR", options);
    },
    formatPrice(price) {
      if (price === undefined || price === null || isNaN(price)) return "가격 문의";
      return new Intl.NumberFormat("ko-KR").format(price);
    },
    goToRoomDetail(roomId) {
      this.$router.push({ name: "RoomDetail", params: { roomId: roomId } });
    },
    scrollToReviews() {
      const reviewsSection = document.getElementById("reviews-section");
      if (reviewsSection) {
        reviewsSection.scrollIntoView({ behavior: "smooth", block: "start" });
      }
    },
    showToast(msg, duration = 3000) {
      const toastId = Date.now();
      this.toasts.push({ id: toastId, message: msg });
      setTimeout(() => {
        this.toasts = this.toasts.filter((t) => t.id !== toastId);
      }, duration);
    },
    formatDateWithDay(date) {
      if (!date) return "";
      const month = (date.getMonth() + 1).toString().padStart(2, "0");
      const day = date.getDate().toString().padStart(2, "0");
      const dayOfWeek = KOREAN_DAYS[date.getDay()];
      return `${month}.${day}(${dayOfWeek})`;
    },
    calculateNights(startDate, endDate) {
      if (!startDate || !endDate) return 0;
      const diffTime = Math.abs(endDate.getTime() - startDate.getTime());
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return diffDays > 0 ? diffDays : 0;
    },
    openDateSelectionModal() {
      this.showDateModal = true;
    },
    confirmDateSelection() {
      this.showToast("날짜가 선택되었습니다. (구현 필요)");
    },
    openGuestSelectionModal() {
      this.tempSelectedAdults = this.selectedAdults;
      this.tempSelectedChildren = this.selectedChildren;
      this.showGuestModal = true;
    },
    incrementAdults() {
      if (this.tempSelectedAdults < 10) this.tempSelectedAdults++;
    },
    decrementAdults() {
      if (this.tempSelectedAdults > 1) this.tempSelectedAdults--;
    },
    incrementChildren() {
      if (this.tempSelectedChildren < 5) this.tempSelectedChildren++;
    },
    decrementChildren() {
      if (this.tempSelectedChildren > 0) this.tempSelectedChildren--;
    },
    confirmGuestSelectionAndCloseModal() {
      this.selectedAdults = this.tempSelectedAdults;
      this.selectedChildren = this.tempSelectedChildren;
      this.showToast("인원이 선택되었습니다.");
      this.showGuestModal = false;
    },
    nextReviewSlide() {
      if (this.canNextReviewSlide) {
        this.currentReviewSlideIndex++;
      }
    },
    prevReviewSlide() {
      if (this.canPrevReviewSlide) {
        this.currentReviewSlideIndex--;
      }
    },
    prevMonth() {
      const currentDate = new Date(this.calendarDate);
      currentDate.setMonth(currentDate.getMonth() - 1);
      this.calendarDate = currentDate;
    },
    nextMonth() {
      const currentDate = new Date(this.calendarDate);
      currentDate.setMonth(currentDate.getMonth() + 1);
      this.calendarDate = currentDate;
    },
    isDateSelected(dateStr) {
      const date = new Date(dateStr);
      if (this.tempSelectedCheckInDate && this.tempSelectedCheckOutDate) {
        return date >= this.tempSelectedCheckInDate && date <= this.tempSelectedCheckOutDate;
      }
      return this.tempSelectedCheckInDate && date.toDateString() === this.tempSelectedCheckInDate.toDateString();
    },
    isDateDisabled(dateStr) {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return new Date(dateStr) < today;
    },
    handleDateClick(dateStr) {
      const clickedDate = new Date(dateStr);
      if (this.isDateDisabled(dateStr)) return;

      if (!this.tempSelectedCheckInDate || (this.tempSelectedCheckInDate && this.tempSelectedCheckOutDate)) {
        this.tempSelectedCheckInDate = clickedDate;
        this.tempSelectedCheckOutDate = null;
      } else if (clickedDate < this.tempSelectedCheckInDate) {
        this.tempSelectedCheckInDate = clickedDate;
      } else {
        const nights = this.calculateNights(this.tempSelectedCheckInDate, clickedDate);
        if (nights > 9) {
          this.showToast("최대 9박까지 선택 가능합니다.");
          this.tempSelectedCheckOutDate = new Date(this.tempSelectedCheckInDate);
          this.tempSelectedCheckOutDate.setDate(this.tempSelectedCheckInDate.getDate() + 9);
        } else {
          this.tempSelectedCheckOutDate = clickedDate;
        }
      }
    },
    getDatePrice(dateStr) {
      const day = new Date(dateStr).getDate();
      if (day % 7 === 0) return "15.0";
      if (day % 5 === 0) return "18.7";
      if (day % 3 === 0) return "13.7";
      return "16.7";
    },
    resetDateSelection() {
      this.tempSelectedCheckInDate = null;
      this.tempSelectedCheckOutDate = null;
      this.calendarDate = new Date();
    },
    confirmDateSelectionAndCloseModal() {
      if (this.tempSelectedCheckInDate && this.tempSelectedCheckOutDate) {
        this.selectedCheckInDate = new Date(this.tempSelectedCheckInDate);
        this.selectedCheckOutDate = new Date(this.tempSelectedCheckOutDate);
        this.showToast("날짜가 선택되었습니다.");
      } else if (this.tempSelectedCheckInDate) {
        this.showToast("체크아웃 날짜를 선택해주세요.");
        return;
      }
      this.showDateModal = false;
    },
  },
};
</script>

<style scoped>
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.text-xxs {
  font-size: 0.65rem; /* 10.4px approx */
  line-height: 0.9rem;
}

.animate-toast-in-out {
  animation:
    toast-in 0.5s ease-out forwards,
    toast-out 0.5s ease-in 2.5s forwards;
}

@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes toast-out {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(20px);
  }
}

.date-selection-dialog .el-dialog__header {
  display: none;
}
.date-selection-dialog .el-dialog__body {
  padding: 0;
}
.date-cell-content {
  cursor: pointer;
  border-radius: 4px;
}
.date-cell-content.is-selected {
  background-color: #fde2ec;
  color: #c12860;
  font-weight: bold;
}
.date-cell-content.is-disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}
.date-cell-content:not(.is-disabled):hover {
  background-color: #fef7f9;
}
.guest-selection-dialog .el-dialog__header {
  display: none;
}
.guest-selection-dialog .el-dialog__body {
  padding: 0 !important;
}
.guest-selection-dialog .el-dialog__footer {
  padding: 0 1rem 1rem 1rem;
}
</style>
