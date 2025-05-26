<template>
  <div class="accommodation-form-container p-4 bg-white shadow-sm rounded">
    <h3 class="mb-4">{{ formTitle }}</h3>
    <el-form :model="form" :rules="rules" ref="accommodationFormRef" label-position="top" @submit.prevent="submitForm">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="숙소명" prop="title">
            <el-input v-model="form.title" placeholder="예: 강릉 오션뷰 펜션" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="숙소 유형" prop="accommodationType">
            <el-select v-model="form.accommodationType" placeholder="숙소 유형을 선택하세요" class="w-100">
              <el-option label="모텔" value="MOTEL"></el-option>
              <el-option label="호텔/리조트" value="HOTEL"></el-option>
              <el-option label="펜션/풀빌라" value="PENSION"></el-option>
              <el-option label="프리미엄" value="PREMIUM"></el-option>
              <el-option label="글램핑/캠핑" value="CAMPING"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="숙소 설명" prop="description">
        <el-input type="textarea" :rows="4" v-model="form.description" placeholder="숙소에 대한 상세 설명을 입력해주세요." />
      </el-form-item>

      <el-form-item label="주소" prop="address">
        <el-input v-model="form.address" placeholder="주소 검색 버튼을 클릭하세요" readonly>
          <template #append>
            <el-button @click="openAddressSearch">주소 검색</el-button>
          </template>
        </el-input>
      </el-form-item>
       <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="시/도" prop="sidoCode">
            <el-select v-model="form.sidoCode" placeholder="시/도 선택" class="w-100">
              <el-option v-for="sido in sidos" :key="sido.sidoCode" :label="sido.sidoName" :value="sido.sidoCode" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="시/군/구" prop="gugunCode">
            <el-select v-model="form.gugunCode" placeholder="시/군/구 선택" :disabled="!form.sidoCode" class="w-100">
              <el-option v-for="gugun in guguns" :key="gugun.gugunCode" :label="gugun.gugunName" :value="gugun.gugunCode" />
            </el-select>
          </el-form-item>
        </el-col>
         <el-col :span="8">
            <el-form-item label="상세 주소 (나머지)">
                <el-input v-model="form.detailAddress" placeholder="상세 주소를 입력하세요" />
            </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
         <el-col :span="12">
          <el-form-item label="체크인 시간" prop="checkInTime">
            <el-time-picker v-model="form.checkInTime" placeholder="체크인 시간" format="HH:mm" value-format="HH:mm:ss" class="w-100" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="체크아웃 시간" prop="checkOutTime">
            <el-time-picker v-model="form.checkOutTime" placeholder="체크아웃 시간" format="HH:mm" value-format="HH:mm:ss" class="w-100" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="연락처" prop="phone">
        <el-input v-model="form.phone" placeholder="예: 010-1234-5678" />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="숙소 연락처 이메일" prop="email">
            <el-input v-model="form.email" placeholder="예: help@example.com" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="숙소 웹사이트 (선택)" prop="website">
            <el-input v-model="form.website" placeholder="예: https://example.com" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="편의시설" prop="amenities">
        <el-checkbox-group v-model="form.amenities">
          <el-checkbox v-for="item in form.availableAmenities" :key="item.key" :value="item.key">{{ item.label }}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="숙소 대표 이미지">
        <el-upload 
          action="#" 
          list-type="picture-card" 
          :auto-upload="false"
          :limit="1" 
          :on-change="handleMainImageChange" 
          :on-preview="handlePictureCardPreview" 
          :on-remove="handleImageRemove"
          v-model:file-list="mainImageSingleList" 
        >
            <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>

      <el-form-item label="숙소 추가 이미지 (선택)">
        <el-upload 
          action="#" 
          list-type="picture-card" 
          :auto-upload="false" 
          multiple 
          :on-change="handleAdditionalImageChange" 
          :on-remove="handleImageRemove" 
          :on-preview="handlePictureCardPreview"
          v-model:file-list="form.imageList" 
        >
            <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>

      <el-dialog v-model="dialogVisible">
          <img style="width: 100%" :src="dialogImageUrl" alt="Preview Image" />
      </el-dialog>

      <div class="d-flex justify-content-end mt-5">
        <el-button @click="cancelForm">취소</el-button>
        <el-button type="primary" @click="submitForm" :loading="isSubmitting">
          {{ accommodationId ? '숙소 정보 수정' : '숙소 등록 요청' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElForm, ElFormItem, ElInput, ElButton, ElSelect, ElOption, ElRow, ElCol, ElTimePicker, ElUpload, ElIcon, ElDialog, ElMessage, ElNotification, ElCheckboxGroup, ElCheckbox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { useUserStore } from '@/store/userStore';
import { useCommonStore } from '@/store/common';
import { getAccommodationDetails, createAccommodation, updateAccommodation } from '@/api/hostApi'; // API 함수 임포트

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const commonStore = useCommonStore();

const accommodationId = computed(() => route.params.accommodationId || null);
const isEditMode = computed(() => !!accommodationId.value);
const formTitle = computed(() => isEditMode.value ? '숙소 정보 수정' : '새 숙소 등록');

const accommodationFormRef = ref(null);
const isSubmitting = ref(false);
const isLoadingDetails = ref(false); // 수정 모드 시 데이터 로딩 상태
const tempTargetGugunNameFromDaum = ref(''); // 주소 검색 API에서 받은 시/군/구 이름을 임시 저장

const form = reactive({
  title: '',
  accommodationType: '',
  description: '',
  address: '', // 기본 주소 (주소 검색 결과)
  sidoCode: '',
  gugunCode: '',
  detailAddress: '', // 나머지 상세 주소
  latitude: null,
  longitude: null,
  checkInTime: '15:00:00',
  checkOutTime: '11:00:00',
  phone: '',
  email: '', // 이메일 필드 추가
  website: '', // 웹사이트 필드 추가
  amenities: [], // 배열로 변경 (체크박스 그룹용)
  availableAmenities: [
    { key: 'WIFI', label: '무선 인터넷' },
    { key: 'PARKING', label: '주차 가능' },
    { key: 'AC', label: '에어컨' },
    { key: 'TV', label: '텔레비전' },
    { key: 'KITCHEN', label: '주방/조리시설' },
    { key: 'WASHER', label: '세탁기' },
    { key: 'PET', label: '반려동물 동반 가능' },
    { key: 'POOL', label: '수영장' },
    { key: 'BREAKFAST', label: '조식 제공' },
  ], // 선택 가능한 편의시설 목록
  mainImage: null, 
  imageList: [], // 업로드된 파일 목록 
  existingImageUrls: [], 
  deletedImageIds: [], // 삭제된 기존 이미지 ID 목록 추가
});

const rules = {
  title: [{ required: true, message: '숙소명을 입력해주세요.', trigger: 'blur' }],
  accommodationType: [{ required: true, message: '숙소 유형을 선택해주세요.', trigger: 'change' }],
  address: [{ required: true, message: '주소를 입력해주세요.', trigger: 'blur' }],
  sidoCode: [{ required: true, message: '시/도를 선택해주세요.', trigger: 'change' }],
  gugunCode: [{ required: true, message: '시/군/구를 선택해주세요.', trigger: 'change' }],
  checkInTime: [{ required: true, message: '체크인 시간을 선택해주세요.', trigger: 'change' }],
  checkOutTime: [{ required: true, message: '체크아웃 시간을 선택해주세요.', trigger: 'change' }],
  email: [{ type: 'email', message: '유효한 이메일 주소를 입력해주세요.', trigger: ['blur', 'change'] }],
};

const sidos = computed(() => commonStore.sidos);
const guguns = computed(() => commonStore.guguns);

const dialogImageUrl = ref('');
const dialogVisible = ref(false);
const mainImageSingleList = ref([]); // 대표 이미지 el-upload의 v-model용

// --- 주소 검색 관련 --- 
onMounted(async () => {
  isLoadingDetails.value = true; // 로딩 시작
  try {
    const scriptId = 'daum-postcode-script';
    if (!document.getElementById(scriptId)) {
      const script = document.createElement('script');
      script.id = scriptId;
      script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
      script.async = true;
      document.head.appendChild(script);
      script.onload = () => { console.log('Daum Postcode script loaded.'); };
      script.onerror = () => { console.error('Daum Postcode script failed to load.'); };
    }
    if (Array.isArray(commonStore.sidos) && commonStore.sidos.length === 0) {
      await commonStore.fetchSidos();
    }

    if (isEditMode.value && accommodationId.value) {
      const response = await getAccommodationDetails(accommodationId.value); // 실제 API 호출
      if (response) { // response가 falsy가 아니면 성공으로 간주 (실제로는 response.success 등으로 확인 필요)
        await populateFormWithExistingData(response); // response가 숙소 데이터 객체라고 가정하고 진행
      } else {
        ElMessage.error('숙소 정보를 불러오는 데 실패했습니다.');
        router.push({ name: 'HostAccommodations' });
      }
    }
  } catch (error) {
    console.error('Error during onMounted:', error);
    ElMessage.error('페이지 초기화 중 오류가 발생했습니다.');
  } finally {
    isLoadingDetails.value = false; // 로딩 종료
  }
});

const openAddressSearch = () => {
  if (window.daum && window.daum.Postcode) {
    new window.daum.Postcode({
      oncomplete: async function(data) {
        form.address = data.roadAddress || data.autoRoadAddress || data.jibunAddress || data.autoJibunAddress;
        const matchedSido = sidos.value.find(s => data.sido.startsWith(s.sidoName));

        if (matchedSido) {
          // Daum API에서 받은 시군구 이름을 임시 변수에 저장
          tempTargetGugunNameFromDaum.value = data.sigungu || '';
          // form.sidoCode를 설정하여 Sido watcher를 트리거
          // watcher 내에서 tempTargetGugunNameFromDaum를 사용하여 gugun을 설정하게 됨
          if (form.sidoCode !== matchedSido.sidoCode) {
            form.sidoCode = matchedSido.sidoCode;
          } else {
            // Sido 코드가 변경되지 않은 경우 (예: 같은 시도 내에서 다른 주소 검색)
            // 수동으로 fetchGuguns 후 gugun 설정 로직을 실행해야 할 수 있음.
            // 현재 Sido watcher가 newSidoCode와 oldSidoCode를 비교하지 않으므로,
            // 동일 sidoCode로 재설정해도 watcher가 실행됨. 따라서 이 부분은 특별한 처리 없어도 될 것으로 보임.
            // 만약 watcher가 동일 값 할당시 실행 안된다면, 아래 로직 필요
            // await commonStore.fetchGuguns(form.sidoCode);
            // selectGugunBasedOnDaumTarget(); 
          }
        } else {
          form.sidoCode = ''; // Sido 못찾으면 초기화
          tempTargetGugunNameFromDaum.value = '';
        }
        form.detailAddress = data.buildingName || '';
        ElMessage.success('주소가 선택되었습니다. 상세 주소를 확인하고, 시/도 및 시/군/구를 확인해주세요.');
      }
    }).open();
  } else {
    ElMessage.error("주소 검색 서비스를 사용할 수 없습니다.");
  }
};

watch(() => form.sidoCode, async (newSidoCode) => {
  const targetGugunNameFromDaumApi = tempTargetGugunNameFromDaum.value; // 현재 값 캡처
  tempTargetGugunNameFromDaum.value = ''; // 즉시 초기화하여 이 watcher 실행 시 한 번만 사용되도록 함

  form.gugunCode = ''; // 시/도 변경 시 항상 군/구 초기화 먼저

  if (newSidoCode) {
    await commonStore.fetchGuguns(newSidoCode); // 스토어의 guguns 업데이트

    // fetchGuguns 완료 후, Daum API에서 가져온 targetGugunName이 있었는지 확인
    if (targetGugunNameFromDaumApi && Array.isArray(commonStore.guguns) && commonStore.guguns.length > 0) {
      const matchedGugun = commonStore.guguns.find(g => targetGugunNameFromDaumApi.startsWith(g.gugunName));
      if (matchedGugun) {
        form.gugunCode = matchedGugun.gugunCode;
      }
    }
  } else {
    commonStore.guguns = []; // 시/도 선택이 없어지면 군/구 목록도 비움
  }
});

const handleMainImageChange = (uploadFile, uploadFiles) => {
  if (uploadFile.raw) {
    form.mainImage = uploadFile.raw; 
  }
  // 대표 이미지는 1개로 제한되므로, uploadFiles가 항상 1개 이하인지 확인 (el-upload의 limit 속성으로 제어)
  mainImageSingleList.value = uploadFiles.slice(-1); // 항상 마지막 파일 하나만 유지 (limit=1과 함께 사용)
};

const handleAdditionalImageChange = (uploadFile, uploadFiles) => {
  form.imageList = uploadFiles; 
};

const handleImageRemove = (uploadFile, uploadFiles) => {
  // mainImageSingleList에서 제거된 경우 form.mainImage 초기화
  if (form.mainImage && uploadFile.raw === form.mainImage) {
      form.mainImage = null;
  }
  // imageList에서 제거된 경우 (UploadUserFile 객체와 form.imageList 동기화)
  form.imageList = uploadFiles.filter(f => f.uid !== uploadFile.uid);
  
  // 삭제된 이미지가 기존 이미지(imageId가 있는 경우)인지 확인
  if (uploadFile.status === 'success' && uploadFile.imageId) {
    if (!form.deletedImageIds.includes(uploadFile.imageId)) {
      form.deletedImageIds.push(uploadFile.imageId);
    }
  }
  console.log('Deleted Image IDs:', form.deletedImageIds);
};

const handlePictureCardPreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url || '';
  dialogVisible.value = true;
};

async function populateFormWithExistingData(apiData) { // 파라미터 이름을 apiData로 변경하여 명확화
  const data = apiData; // 백엔드 응답이 { success: true, data: {...} } 형태가 아니라 바로 데이터 객체라고 가정. 아니라면 data = apiData.data;
  form.title = data.title || '';
  form.accommodationType = data.accommodationType || '';
  form.description = data.description || '';
  form.address = data.address || '';
  form.sidoCode = data.sidoCode || '';
  // sidoCode 변경 시 구군 목록 자동 로드 및 설정될 수 있도록 약간의 지연 후 gugunCode 설정
  if (data.sidoCode) {
    await commonStore.fetchGuguns(data.sidoCode); // 구군 목록 먼저 로드
    form.gugunCode = data.gugunCode || ''; 
  }
  form.detailAddress = data.detailAddress || '';
  form.latitude = data.latitude;
  form.longitude = data.longitude;
  form.checkInTime = data.checkInTime || '15:00:00';
  form.checkOutTime = data.checkOutTime || '11:00:00';
  form.phone = data.phone || '';
  form.email = data.email || '';
  form.website = data.website || '';
  form.amenities = Array.isArray(data.amenities) ? data.amenities : (typeof data.amenities === 'string' ? JSON.parse(data.amenities) : []); // amenities가 JSON 문자열로 올 경우 대비

  // 이미지 처리
  form.imageList = [];
  mainImageSingleList.value = [];
  form.existingImageUrls = []; 
  form.deletedImageIds = []; 

  // 백엔드에서 images 배열로 이미지 목록을 준다고 가정 (isMain 플래그로 대표이미지 구분)
  // 예: data.images = [{imageId: 1, url: '...', isMain: true}, {imageId: 2, url: '...', isMain: false}]
  if (Array.isArray(data.images)) {
    data.images.forEach(img => {
      if (img.url) {
        const imageFile = {
          name: img.url.substring(img.url.lastIndexOf('/') + 1),
          url: img.url,
          status: 'success',
          uid: img.imageId || Date.now() + Math.random(), // imageId가 있으면 그것을 uid로 사용
          imageId: img.imageId,
          isMain: img.isMain || false,
        };
        form.existingImageUrls.push({ url: img.url, imageId: img.imageId, isMain: img.isMain || false });
        if (img.isMain) {
          mainImageSingleList.value = [imageFile];
        } else {
          form.imageList.push(imageFile);
        }
      }
    });
  }
  // 이전 mainImage, additionalImages 대신 통합된 images 배열로 처리 (백엔드 응답 형식에 맞춰야 함)
}

const submitForm = async () => {
  if (!accommodationFormRef.value) return;
  await accommodationFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmitting.value = true;
      try {
        const fd = new FormData();
        fd.append('title', form.title);
        fd.append('accommodationType', form.accommodationType);
        fd.append('description', form.description);
        fd.append('address', `${form.address} ${form.detailAddress || ''}`);
        fd.append('sidoCode', form.sidoCode);
        fd.append('gugunCode', form.gugunCode);
        fd.append('checkInTime', form.checkInTime);
        fd.append('checkOutTime', form.checkOutTime);
        fd.append('phone', form.phone);
        fd.append('email', form.email);
        fd.append('website', form.website);
        fd.append('amenities', JSON.stringify(form.amenities));
        
        if (form.latitude) fd.append('latitude', form.latitude.toString());
        if (form.longitude) fd.append('longitude', form.longitude.toString());

        let existingMainImageId = null;
        if (mainImageSingleList.value.length > 0 && mainImageSingleList.value[0].imageId) {
          existingMainImageId = mainImageSingleList.value[0].imageId;
        }

        if (form.mainImage instanceof File) { // 새 대표 이미지 파일이 있으면
          fd.append('mainImageFile', form.mainImage);
          if (isEditMode.value && existingMainImageId && existingMainImageId !== form.mainImage.name) { // 기존 대표 이미지가 있었고, 새 파일로 교체되면
            // 백엔드에서 기존 대표이미지의 isMain을 false로 처리하거나 삭제하는 로직 필요
            // deletedImageIds에 기존 대표 이미지 ID 추가하는 것을 고려할 수 있음 (만약 교체 시 기존 파일 삭제 정책이라면)
            if (!form.deletedImageIds.includes(existingMainImageId)) {
                 // form.deletedImageIds.push(existingMainImageId); // 정책에 따라 추가
            }
          }
        } else if (isEditMode.value && existingMainImageId) { // 새 대표 이미지 파일 없고, 기존 대표 이미지가 있었으면
          fd.append('mainImageId', existingMainImageId.toString()); // 기존 대표 이미지 ID 전송 (유지)
        } // 새 대표 이미지도 없고, 기존 대표 이미지도 없는 경우 (삭제된 경우) -> mainImageId, mainImageFile 둘 다 안보냄
          // (백엔드에서는 이 경우 대표 이미지 없음을 인지하고, additionalImages의 첫번째를 대표로 삼거나 null 처리)

        // 새로 추가된 추가 이미지 파일들 (기존 파일 URL은 보내지 않음)
        form.imageList.forEach((file) => {
          if (file.raw instanceof File) { 
            fd.append('imageFiles', file.raw);
          }
        });

        if (isEditMode.value && form.deletedImageIds.length > 0) {
          fd.append('deletedImageIds', JSON.stringify(form.deletedImageIds));
        }
        
        let response;
        if (isEditMode.value && accommodationId.value) {
          response = await updateAccommodation(accommodationId.value, fd);
        } else {
          response = await createAccommodation(fd);
        }

        // 백엔드 응답이 { success: true, message: '...', data: {...} } 형태라고 가정
        if (response && response.success) {
          ElNotification.success({ title: '성공', message: response.message || (isEditMode.value ? '숙소 정보가 수정되었습니다.' : '숙소 등록 요청이 완료되었습니다.') });
          router.push({ name: 'HostAccommodations' }); // 성공 시 호스트 숙소 목록 페이지로 이동
        } else {
          ElMessage.error(response.message || '작업 중 오류가 발생했습니다.');
        }

      } catch (error) {
        console.error("Form submission error:", error);
        const errorMsg = error.response?.data?.message || error.message || '오류가 발생했습니다.';
        ElMessage.error(errorMsg);
      } finally {
        isSubmitting.value = false;
      }
    } else {
      ElMessage.error('필수 입력 항목을 확인해주세요.');
      return false;
    }
  });
};

const cancelForm = () => {
  router.back();
};

</script>

<style scoped>
.accommodation-form-container {
  max-width: 900px;
  margin: 20px auto;
}
.w-100 {
  width: 100%;
}
</style> 