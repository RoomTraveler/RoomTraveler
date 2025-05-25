<template>
  <div class="container mt-5 mb-5">
    <h2 class="text-center mb-4">호스트 등록</h2>
    <div class="form-container">
      <form @submit.prevent="submitForm" enctype="multipart/form-data">
        <!-- 기본 정보 입력란들 -->
        <div class="row">
          <div class="col-md-6 mb-3">
            <label for="businessName" class="form-label required-field">상호명 (사업자명)</label>
            <input type="text" class="form-control" id="businessName" v-model="formData.businessName" required autocomplete="off" />
          </div>
          <div class="col-md-6 mb-3">
            <label for="ceoName" class="form-label required-field">대표자명</label>
            <input type="text" class="form-control" id="ceoName" v-model="formData.ceoName" required autocomplete="off" />
          </div>
        </div>
        <div class="mb-3">
          <label for="businessNumber" class="form-label required-field">사업자 등록번호</label>
          <input type="text" class="form-control" id="businessNumber" v-model="formData.businessNumber" placeholder="000-00-00000" @blur="validateBusinessNumber" required autocomplete="off" />
          <div class="form-text">하이픈(-)을 포함하여 입력해주세요.</div>
        </div>
        <div class="mb-3">
          <label for="postcode" class="form-label">우편번호</label>
          <div class="input-group">
            <input type="text" class="form-control" id="postcode" v-model="formData.postcode" placeholder="우편번호" readonly>
            <button class="btn btn-outline-secondary" type="button" @click="openDaumPostcode">우편번호 찾기</button>
          </div>
        </div>
        <div class="mb-3">
          <label for="businessAddress" class="form-label required-field">사업장 주소</label>
          <input type="text" class="form-control" id="businessAddress" v-model="formData.roadAddress" placeholder="도로명 주소" readonly required />
        </div>
        <div class="mb-3">
          <label for="businessAddressDetail" class="form-label">상세 주소</label>
          <input type="text" class="form-control" id="businessAddressDetail" v-model="formData.detailAddress" placeholder="상세 주소를 입력하세요" autocomplete="off" />
        </div>
        <div class="row">
          <div class="col-md-6 mb-3">
            <label for="businessPhone" class="form-label">사업장 전화번호</label>
            <input type="tel" class="form-control" id="businessPhone" v-model="formData.businessPhone" placeholder="02-123-4567" autocomplete="off" />
          </div>
          <div class="col-md-6 mb-3">
            <label for="businessType" class="form-label">사업자 업종</label>
            <input type="text" class="form-control" id="businessType" v-model="formData.businessType" placeholder="예: 호텔, 펜션, 게스트하우스" autocomplete="off" />
          </div>
        </div>
        <div class="row">
          <div class="col-md-6 mb-3">
            <label for="businessLicenseFile" class="form-label required-field">사업자 등록증 (이미지 파일)</label>
            <input type="file" class="form-control" id="businessLicenseFile" @change="handleFileChange" accept="image/*" required />
          </div>
          <div class="col-md-6 mb-3">
            <label for="businessLicenseExpire" class="form-label">사업자 등록증 만료일 (선택)</label>
            <input type="date" class="form-control" id="businessLicenseExpire" v-model="formData.businessLicenseExpire" />
          </div>
        </div>
        <h5 class="mt-4 mb-3">정산 정보</h5>
        <div class="row">
          <div class="col-md-4 mb-3">
            <label for="bankName" class="form-label required-field">은행명</label>
            <select class="form-select" id="bankName" v-model="formData.bankName" required>
              <option value="">은행을 선택하세요</option>
              <option value="KB국민은행">KB국민은행</option>
              <option value="신한은행">신한은행</option>
              <option value="우리은행">우리은행</option>
              <option value="하나은행">하나은행</option>
              <option value="농협은행">농협은행</option>
              <option value="기업은행">기업은행</option>
              <option value="SC제일은행">SC제일은행</option>
              <option value="카카오뱅크">카카오뱅크</option>
              <option value="토스뱅크">토스뱅크</option>
            </select>
          </div>
          <div class="col-md-4 mb-3">
            <label for="bankAccount" class="form-label required-field">계좌번호</label>
            <input type="text" class="form-control" id="bankAccount" v-model="formData.bankAccount" placeholder="하이픈(-) 없이 입력해주세요" required autocomplete="off" />
          </div>
          <div class="col-md-4 mb-3">
            <label for="bankOwner" class="form-label required-field">예금주명</label>
            <input type="text" class="form-control" id="bankOwner" v-model="formData.bankOwner" required autocomplete="off" />
          </div>
        </div>
        <div class="mb-3">
          <label for="description" class="form-label">호스트 소개</label>
          <textarea class="form-control" id="description" v-model="formData.description" rows="4" placeholder="숙소 운영 경험, 특별한 서비스 등 호스트로서 자신을 자유롭게 소개해주세요. (선택사항)"></textarea>
        </div>
        <div class="alert alert-info mt-4">
          <p class="mb-1"><strong><i class="bi bi-info-circle-fill"></i> 호스트 등록 안내:</strong></p>
          <ul>
            <li>호스트 등록 신청 후 관리자의 심사를 거쳐 최종 승인됩니다.</li>
            <li>제출하신 정보는 정확해야 하며, 사업자 정보는 국세청 등록 정보와 일치해야 합니다.</li>
            <li>정보가 불일치하거나 허위 사실이 발견될 경우, 호스트 자격이 제한될 수 있습니다.</li>
          </ul>
        </div>
        <div v-if="hostStore.error" class="alert alert-danger mt-3" role="alert">
          {{ hostStore.error }}
        </div>
        <div class="d-grid gap-2 mt-4">
          <button type="submit" class="btn btn-primary btn-lg" :disabled="hostStore.isLoading">
            <span v-if="hostStore.isLoading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
            {{ hostStore.isLoading ? ' 처리 중...' : '호스트 등록 신청' }}
          </button>
          <RouterLink to="/" class="btn btn-secondary">취소</RouterLink>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, nextTick, onMounted, onUnmounted } from 'vue';
import { useRouter, RouterLink } from 'vue-router';
import { useUserStore } from '../../store/userStore';
import { useHostStore } from '../../store/hostStore';

const userStore = useUserStore();
const hostStore = useHostStore();
const router = useRouter();

const formData = reactive({
  businessName: "",
  ceoName: "",
  businessNumber: "",
  businessType: "",
  roadAddress: "",
  postcode: "",
  detailAddress: "",
  businessPhone: "",
  businessLicenseExpire: "",
  bankName: "",
  bankAccount: "",
  bankOwner: "",
  description: "",
  businessRegistrationFile: null,
});

function loadDaumPostcodeScript() {
  return new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
    script.onload = resolve;
    script.onerror = reject;
    document.head.appendChild(script);
  });
}

onMounted(async () => {
  try {
    await loadDaumPostcodeScript();
  } catch (e) {
    console.error("Daum 우편번호 서비스 로드 실패:", e);
  }
});

function openDaumPostcode() {
  if (window.daum && window.daum.Postcode) {
    new window.daum.Postcode({
      oncomplete: function(data) {
        formData.postcode = data.zonecode;
        formData.roadAddress = data.roadAddress;
        nextTick(() => {
          const detailAddressInput = document.getElementById('businessAddressDetail');
          if (detailAddressInput) detailAddressInput.focus();
        });
      }
    }).open();
  } else {
    alert("우편번호 검색 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요.");
  }
}

function handleFileChange(event) {
  const file = event.target.files[0];
  formData.businessRegistrationFile = file || null;
}

function validateBusinessNumber() {
  const pattern = /^\d{3}-\d{2}-\d{5}$/;
  if (formData.businessNumber && !pattern.test(formData.businessNumber)) {
    alert("사업자 등록번호 형식이 올바르지 않습니다. (000-00-00000)");
  }
}

async function submitForm() {
  if (!userStore.isAuthenticated || !userStore.user?.id) {
    alert("호스트 등록을 위해서는 로그인이 필요합니다.");
    router.push("/login");
    return;
  }
  const hostPayload = {
    userId: userStore.user.id,
    businessNumber: formData.businessNumber,
    businessName: formData.businessName,
    ceoName: formData.ceoName,
    businessAddress: formData.roadAddress + (formData.detailAddress ? " " + formData.detailAddress : ""),
    businessPhone: formData.businessPhone,
    businessLicenseExpire: formData.businessLicenseExpire || null,
    bankName: formData.bankName,
    bankAccount: formData.bankAccount,
    bankOwner: formData.bankOwner,
    description: formData.description,
    businessType: formData.businessType,
  };
  const fd = new FormData();
  fd.append("host", new Blob([JSON.stringify(hostPayload)], { type: "application/json" }));
  if (formData.businessRegistrationFile) {
    fd.append("businessLicenseFile", formData.businessRegistrationFile);
  } else {
    alert("사업자 등록증 파일을 첨부해주세요.");
    return;
  }
  const result = await hostStore.registerHost(fd);
  if (result.success) {
    alert("호스트 등록 신청이 완료되었습니다! 관리자 승인 후 이용 가능합니다.");
    router.push("/");
  } else {
    window.scrollTo({ top: 0, behavior: "smooth" });
  }
}
onUnmounted(() => {
  hostStore.resetHostState();
});
</script>

<style scoped>
.form-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.required-field::after {
  content: " *";
  color: red;
}
.form-label { font-weight: 500; }
.form-text { font-size: 0.875rem; }
.alert-info ul { margin-bottom: 0; }
</style>
