<template>
  <Layout>
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-lg-8">
          <div class="card shadow">
            <div class="card-body">
              <h3 class="mb-4">{{ pageTitle }}</h3>
              <form @submit.prevent="onSubmit">
                <div class="mb-3">
                  <label for="eventTitle" class="form-label">이벤트 제목</label>
                  <input
                    type="text"
                    class="form-control"
                    id="eventTitle"
                    v-model="form.title"
                    required
                    maxlength="100"
                  />
                </div>
                <div class="mb-3">
                  <label class="form-label">이벤트 내용</label>
                  <div ref="editorElement"></div>
                </div>
                <div class="mb-3">
                  <label for="thumbnailImage" class="form-label">썸네일 이미지 (선택)</label>
                  <input
                    type="file"
                    class="form-control"
                    id="thumbnailImage"
                    @change="handleThumbnailUpload"
                    accept="image/*"
                  />
                  <small
                    v-if="isUpdateMode && currentThumbnailUrl && !thumbnailPreview"
                    class="form-text text-muted mt-2"
                  >
                    현재 썸네일: <a :href="currentThumbnailUrl" target="_blank">보기</a> (새 이미지 선택 시 교체)
                  </small>
                  <div v-if="thumbnailPreview" class="mt-2">
                    <img
                      :src="thumbnailPreview"
                      alt="썸네일 미리보기"
                      style="
                        width: 100%;
                        max-height: 300px;
                        object-fit: contain;
                        border-radius: 0.25rem;
                        background-color: #f8f9fa;
                      "
                    />
                  </div>
                </div>
                <div class="mb-3">
                  <label for="eventStatus" class="form-label">상태</label>
                  <select class="form-select" id="eventStatus" v-model="form.status" required>
                    <option value="ONGOING">진행중</option>
                    <option value="ENDED">종료</option>
                    <option value="HIDDEN">숨김</option>
                  </select>
                </div>
                <div class="row mb-3">
                  <div class="col-md-6">
                    <label for="eventStartDate" class="form-label">시작일</label>
                    <input type="date" class="form-control" id="eventStartDate" v-model="form.startDate" />
                  </div>
                  <div class="col-md-6">
                    <label for="eventEndDate" class="form-label">종료일</label>
                    <input type="date" class="form-control" id="eventEndDate" v-model="form.endDate" />
                  </div>
                </div>
                <div class="d-flex gap-2">
                  <button class="btn btn-primary" type="submit" :disabled="isSubmitting">
                    <span
                      v-if="isSubmitting"
                      class="spinner-border spinner-border-sm"
                      role="status"
                      aria-hidden="true"
                    ></span>
                    {{ submitButtonText }}
                  </button>
                  <button
                    class="btn btn-outline-secondary"
                    type="button"
                    @click="$router.back()"
                    :disabled="isSubmitting"
                  >
                    취소
                  </button>
                </div>
                <div
                  v-if="submitMessage"
                  class="alert mt-3"
                  :class="submitStatus === 'success' ? 'alert-success' : 'alert-danger'"
                >
                  {{ submitMessage }}
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from "vue";
import { useRouter, useRoute } from "vue-router";
import Layout from "@/components/layout/Layout.vue";
import apiModule from "@/api"; // @/api/index.js 파일을 import
import { useUserStore } from "@/store/userStore"; // 유저 스토어 import

// Toast UI Editor 코어 임포트
import Editor from "@toast-ui/editor"; // '@toast-ui/vue-editor' 대신 코어 라이브러리 직접 사용
import axios from "axios"; // 이미지 업로드용 axios 직접 사용 (api 인스턴스는 multipart/form-data 헤더 자동 설정 문제 있을 수 있음)

const api = apiModule.api; // instance (인증 필요한 axios) 가져오기

const router = useRouter();
const route = useRoute();
const userStore = useUserStore(); // 유저 스토어 사용

const eventId = route.params.eventId || null;
const isUpdateMode = computed(() => !!eventId);

const form = ref({
  title: "",
  content: "",
  status: "ONGOING",
  startDate: "",
  endDate: "",
  writer: userStore.userInfo?.name || "관리자",
});

const thumbnailFile = ref(null); // 선택된 썸네일 파일 객체
const thumbnailPreview = ref(null); // 썸네일 미리보기 URL
const currentThumbnailUrl = ref(null); // 수정 모드 시 기존 썸네일 URL

const editorElement = ref(null); // 에디터 DOM 요소를 위한 ref
let editorInstance = null; // 에디터 인스턴스를 저장할 변수

const editorOptions = {
  initialEditType: "wysiwyg",
  height: "400px",
  previewStyle: "vertical", // 또는 'tab'
  hooks: {
    addImageBlobHook: async (blob, callback) => {
      const formData = new FormData();
      formData.append("image", blob);

      const userStore = useUserStore();
      // 토큰 가져오는 로직 수정: userStore._tokens 객체에서 직접 access_token을 가져옴
      const token = userStore._tokens?.access_token;

      console.log("[EventCreate.vue] Token for editor image upload:", token);

      if (!token) {
        console.error("[EventCreate.vue] No token found in userStore for editor image upload!");
        alert("인증 토큰이 없어 이미지를 업로드할 수 없습니다. 다시 로그인해주세요.");
        return;
      }

      try {
        const response = await axios.post("/api/events/upload-editor-image", formData, {
          headers: {
            "Content-Type": "multipart/form-data",
            Authorization: `Bearer ${token}`, // 스토어에서 직접 가져온 토큰 사용
          },
        });
        // BaseResponse<Map<String, String>> 형태의 응답을 가정
        if (response.data && response.data.success && response.data.result?.imageUrl) {
          callback(response.data.result.imageUrl, "image"); // 에디터에 이미지 삽입
        } else {
          console.error("Image upload failed:", response.data?.message || "Unknown error");
          alert(`이미지 업로드 실패: ${response.data?.message || "알 수 없는 오류"}`);
        }
      } catch (error) {
        console.error("Error uploading image:", error);
        alert("이미지 업로드 중 오류가 발생했습니다.");
      }
    },
  },
};

const isSubmitting = ref(false);
const submitMessage = ref("");
const submitStatus = ref(""); // 'success' or 'error'

const pageTitle = computed(() => (isUpdateMode.value ? "이벤트 수정" : "이벤트 등록"));
const submitButtonText = computed(() => (isUpdateMode.value ? "수정 완료" : "등록하기"));

const initializeEditor = (initialContent = "") => {
  if (editorElement.value && !editorInstance) {
    editorInstance = new Editor({
      ...editorOptions,
      el: editorElement.value,
      initialValue: initialContent, // 초기 내용 설정
    });
  }
};

const fetchEventDataForUpdate = async () => {
  if (isUpdateMode.value) {
    try {
      const response = await api.get(`/api/events/${eventId}`);
      if (response.data && response.data.success) {
        const eventData = response.data.result;
        console.log("Received event data:", eventData); // 데이터 전체 확인
        console.log("Start Date from API:", eventData.startDate);
        console.log("End Date from API:", eventData.endDate);

        form.value.title = eventData.title || "";
        form.value.status = eventData.status || "ONGOING";

        // 날짜 값 처리 수정: 배열을 YYYY-MM-DD 형식의 문자열로 변환
        const formatDateArrayToString = (dateArray) => {
          if (Array.isArray(dateArray) && dateArray.length === 3) {
            const [year, month, day] = dateArray;
            // 월과 일이 한 자리 수일 경우 앞에 0을 붙여 두 자리로 만듦
            const formattedMonth = month < 10 ? `0${month}` : month;
            const formattedDay = day < 10 ? `0${day}` : day;
            return `${year}-${formattedMonth}-${formattedDay}`;
          }
          return ""; // 유효하지 않은 형식이면 빈 문자열 반환
        };

        form.value.startDate = formatDateArrayToString(eventData.startDate);
        form.value.endDate = formatDateArrayToString(eventData.endDate);

        form.value.writer = eventData.writer || userStore.userInfo?.name || "관리자";

        // 에디터가 이미 초기화되었으면 내용 설정, 아니면 초기화 시 설정
        if (editorInstance) {
          editorInstance.setHTML(eventData.content || "");
        } else {
          // onMounted에서 initializeEditor를 호출할 때 이 내용이 사용됨
          form.value.content = eventData.content || "";
        }

        // 기존 썸네일 URL 설정
        currentThumbnailUrl.value = eventData.mainImageUrl; // getMainImageUrl() 통해 제공되는 URL
      } else {
        throw new Error(response.data?.message || "이벤트 정보를 불러오지 못했습니다.");
      }
    } catch (error) {
      console.error("Error fetching event data for update:", error);
      submitMessage.value = `이벤트 정보를 불러오는데 실패했습니다: ${error.message}`;
      submitStatus.value = "error";
    }
  }
};

onMounted(async () => {
  if (isUpdateMode.value) {
    await fetchEventDataForUpdate(); // 데이터 먼저 로드
    initializeEditor(form.value.content); // 로드된 content로 에디터 초기화
  } else {
    initializeEditor(); // 새 글 작성 시 빈 에디터 초기화
  }
});

// 컴포넌트 파괴 전 에디터 인스턴스 정리
onBeforeUnmount(() => {
  if (editorInstance) {
    editorInstance.destroy();
    editorInstance = null;
  }
});

const handleThumbnailUpload = (event) => {
  const file = event.target.files[0];
  if (file) {
    thumbnailFile.value = file;
    const reader = new FileReader();
    reader.onload = (e) => {
      thumbnailPreview.value = e.target.result;
    };
    reader.readAsDataURL(file);
    currentThumbnailUrl.value = null; // 새 이미지 선택 시 기존 URL은 더 이상 유효하지 않음 (화면 표시용)
  } else {
    thumbnailFile.value = null;
    thumbnailPreview.value = null;
  }
};

async function onSubmit() {
  isSubmitting.value = true;
  submitMessage.value = "";
  submitStatus.value = "";

  if (!editorInstance) {
    alert("에디터를 초기화할 수 없습니다.");
    isSubmitting.value = false;
    return;
  }

  const contentHTML = editorInstance.getHTML();

  const formData = new FormData();
  const eventBoardPayload = {
    ...form.value,
    content: contentHTML,
  };
  formData.append("eventBoard", new Blob([JSON.stringify(eventBoardPayload)], { type: "application/json" }));

  if (thumbnailFile.value) {
    formData.append("thumbnailImage", thumbnailFile.value);
  }

  try {
    let response;
    const config = {
      headers: { "Content-Type": "multipart/form-data" },
    };

    if (isUpdateMode.value) {
      response = await api.put(`/api/events/${eventId}`, formData, config);
    } else {
      response = await api.post("/api/events", formData, config);
    }

    if (response.data && response.data.success) {
      submitMessage.value = isUpdateMode.value
        ? "이벤트가 성공적으로 수정되었습니다!"
        : "이벤트가 성공적으로 등록되었습니다!";
      submitStatus.value = "success";
      setTimeout(() => {
        router.push({ name: "Event" });
      }, 1500);
    } else {
      throw new Error(response.data?.message || "이벤트 처리 중 오류 발생");
    }
  } catch (error) {
    console.error("Error submitting event:", error);
    submitMessage.value = `이벤트 처리 중 오류가 발생했습니다: ${error.message || ""}`;
    submitStatus.value = "error";
  }
  isSubmitting.value = false;
}
</script>

<style scoped>
/* 필요한 경우 여기에 스타일 추가 */
.form-label {
  font-weight: 500;
}
/* 에디터 UI 깨짐 방지 (필요시 테마나 z-index 조정) */
:deep(.toastui-editor-popup) {
  z-index: 10000; /* 다른 요소에 가려지지 않도록 */
}
</style>
