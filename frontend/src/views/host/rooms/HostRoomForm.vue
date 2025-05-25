<template>
  <div class="room-form-container p-4 bg-white shadow-sm rounded">
    <h3 class="mb-4">{{ formTitle }}</h3>
    <el-form :model="form" :rules="rules" ref="roomFormRef" label-position="top" @submit.prevent="submitForm">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="객실명" prop="name">
            <el-input v-model="form.name" placeholder="예: 디럭스 오션뷰 트윈룸" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="객실 타입 (내부 분류용)" prop="roomType">
            <el-input v-model="form.roomType" placeholder="예: 더블룸, 가족룸" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="객실 설명" prop="description">
        <el-input type="textarea" :rows="3" v-model="form.description" placeholder="객실의 특징, 침대 타입 등을 상세히 설명해주세요." />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="1박당 가격" prop="price">
            <el-input-number v-model="form.price" :min="0" :step="1000" placeholder="숫자만 입력" class="w-100" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="기준 인원" prop="capacity">
            <el-input-number v-model="form.capacity" :min="1" placeholder="숫자만 입력" class="w-100" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="최대 인원" prop="maxCapacity">
            <el-input-number v-model="form.maxCapacity" :min="form.capacity || 1" placeholder="숫자만 입력" class="w-100" />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
         <el-col :span="12">
          <el-form-item label="객실 수 (이 타입의)" prop="roomCount">
            <el-input-number v-model="form.roomCount" :min="1" placeholder="동일 타입 객실 수" class="w-100" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="객실 상태" prop="status">
            <el-select v-model="form.status" placeholder="객실 상태 선택" class="w-100">
              <el-option label="판매중 (AVAILABLE)" value="AVAILABLE"></el-option>
              <el-option label="판매 중지 (UNAVAILABLE)" value="UNAVAILABLE"></el-option>
              <el-option label="점검중 (UNDER_MAINTENANCE)" value="UNDER_MAINTENANCE"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="편의시설 (객실 내)" prop="amenities">
        <el-checkbox-group v-model="form.amenities">
          <el-checkbox 
            v-for="item in availableAmenities" 
            :key="item.key" 
            :value="item.key" 
            :label="item.label" >{{ item.label }}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="객실 대표 이미지 (필수, 1개)">
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

      <el-form-item label="객실 추가 이미지 (선택)">
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
        <el-button type="primary" @click="submitFormAction" :loading="isSubmitting">
          {{ isEditMode ? '객실 정보 수정' : '객실 등록' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElForm, ElFormItem, ElInput, ElButton, ElSelect, ElOption, ElRow, ElCol, ElInputNumber, ElUpload, ElIcon, ElDialog, ElMessage, ElNotification, ElCheckboxGroup, ElCheckbox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getRoomDetailsForHost, createRoom, updateRoom } from '@/api/hostApi';
import { amenities } from '@/utils/staticData'; // 수정: commonAmenities -> amenities

const router = useRouter();
const route = useRoute();

const props = defineProps({
  accommodationId: { // 새 객실 등록 시 부모 숙소 ID
    type: [String, Number],
    required: false, 
  },
  roomId: { // 객실 수정 시 객실 ID
    type: [String, Number],
    required: false,
  },
});

const isEditMode = computed(() => !!props.roomId);
const formTitle = computed(() => isEditMode.value ? '객실 정보 수정' : '새 객실 등록');

const roomFormRef = ref(null);
const isSubmitting = ref(false);
const isLoadingDetails = ref(false);

const form = reactive({
  name: '',
  roomType: '',
  description: '',
  price: 0,
  capacity: 1,
  maxCapacity: 1,
  roomCount: 1,
  status: 'AVAILABLE',
  amenities: [],
  mainImageFile: null,      // 실제 파일 객체 (대표 이미지)
  imageList: [],          // 업로드된 파일 목록 (el-upload v-model 용, 추가 이미지)
  imageFilesToUpload: [], // 실제 파일 객체들 (추가 이미지)
  deletedImageIds: [],    // 삭제될 기존 이미지 ID 목록 (JSON 문자열로 보내야 함)
  mainImageIdToKeep: null, // 수정 시, 파일 변경 없이 기존 대표 이미지를 유지할 경우 그 ID
  parentAccommodationId: null, // 추가: 수정 모드에서 객실의 부모 숙소 ID 저장용
});

const availableAmenities = ref(amenities.room); // 수정: commonAmenities.room -> amenities.room

const rules = {
  name: [{ required: true, message: '객실명을 입력해주세요.', trigger: 'blur' }],
  price: [{ required: true, type: 'number', message: '가격을 입력해주세요.', trigger: 'blur' }],
  capacity: [{ required: true, type: 'integer', min: 1, message: '기준 인원은 1명 이상이어야 합니다.', trigger: 'blur' }],
  maxCapacity: [{ required: true, type: 'integer', message: '최대 인원을 입력해주세요.', trigger: 'blur' },
                { validator: (rule, value, callback) => {
                    if (value < form.capacity) {
                        callback(new Error('최대 인원은 기준 인원보다 적을 수 없습니다.'));
                    } else {
                        callback();
                    }
                  }, trigger: 'blur'}
               ],
  roomCount: [{ required: true, type: 'integer', min: 1, message: '객실 수는 1개 이상이어야 합니다.', trigger: 'blur' }],
  status: [{ required: true, message: '객실 상태를 선택해주세요.', trigger: 'change' }],
};

const dialogImageUrl = ref('');
const dialogVisible = ref(false);
const mainImageSingleList = ref([]); // 대표 이미지 el-upload의 v-model용

watch(() => form.capacity, (newVal) => {
  if (form.maxCapacity < newVal) {
    form.maxCapacity = newVal;
  }
});

onMounted(async () => {
  if (isEditMode.value && props.roomId) {
    isLoadingDetails.value = true;
    try {
      const roomData = await getRoomDetailsForHost(props.roomId);
      if (roomData) {
        populateForm(roomData);
      }
    } catch (error) {
      ElMessage.error('객실 정보를 불러오는 데 실패했습니다.');
      console.error('Error fetching room details:', error);
      router.back(); // 이전 페이지로 이동 또는 목록으로 이동
    } finally {
      isLoadingDetails.value = false;
    }
  }
});

function populateForm(data) {
  form.name = data.name || '';
  form.roomType = data.roomType || '';
  form.description = data.description || '';
  form.price = data.price ? Number(data.price) : 0;
  form.capacity = data.capacity || 1;
  form.maxCapacity = data.maxCapacity || 1;
  form.roomCount = data.roomCount || 1;
  form.status = data.status || 'AVAILABLE';
  form.amenities = Array.isArray(data.amenities) ? data.amenities : [];
  form.parentAccommodationId = data.accommodationId || null; // 추가
  
  mainImageSingleList.value = [];
  form.imageList = [];
  form.mainImageIdToKeep = null;

  if (Array.isArray(data.images)) {
    data.images.forEach(img => {
      const imageFile = {
        name: img.imageUrl.substring(img.imageUrl.lastIndexOf('/') + 1),
        url: img.imageUrl,
        status: 'success',
        uid: img.imageId || Date.now() + Math.random(),
        imageId: img.imageId,
        isMain: img.isMain || false,
      };
      if (img.isMain) {
        mainImageSingleList.value = [imageFile];
        form.mainImageIdToKeep = img.imageId; // 기존 대표 이미지 ID 저장
      } else {
        form.imageList.push(imageFile);
      }
    });
  }
}

const handleMainImageChange = (uploadFile, uploadFiles) => {
  if (uploadFile.raw) {
    form.mainImageFile = uploadFile.raw;
    form.mainImageIdToKeep = null; // 새 파일 업로드 시 기존 ID 유지 안 함
  }
  mainImageSingleList.value = uploadFiles.slice(-1);
};

const handleAdditionalImageChange = (uploadFile, uploadFiles) => {
  // uploadFiles는 UploadUserFile[] 형태. 여기서 raw 파일만 추출하거나, submit 시점에 처리
  form.imageList = uploadFiles; // el-upload v-model과 동기화
  // 실제 업로드할 파일 목록을 따로 관리하는 것이 더 명확할 수 있음
  form.imageFilesToUpload = uploadFiles.map(f => f.raw).filter(f => f instanceof File);
};

const handleImageRemove = (uploadFile, uploadFiles) => {
  if (form.mainImageFile && uploadFile.raw === form.mainImageFile) {
    form.mainImageFile = null;
  }
  mainImageSingleList.value = mainImageSingleList.value.filter(f => f.uid !== uploadFile.uid);

  form.imageList = uploadFiles.filter(f => f.uid !== uploadFile.uid);
  form.imageFilesToUpload = form.imageList.map(f => f.raw).filter(f => f instanceof File);
  
  if (uploadFile.status === 'success' && uploadFile.imageId) {
    if (!form.deletedImageIds.includes(uploadFile.imageId)) {
      form.deletedImageIds.push(uploadFile.imageId);
    }
    if (form.mainImageIdToKeep === uploadFile.imageId) {
        form.mainImageIdToKeep = null; // 삭제된 이미지가 유지하려던 대표 이미지면 초기화
    }
  }
};

const handlePictureCardPreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url || '';
  dialogVisible.value = true;
};

const submitFormAction = async () => {
  if (!roomFormRef.value) return;
  await roomFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmitting.value = true;
      const fd = new FormData();
      fd.append('name', form.name);
      fd.append('description', form.description);
      fd.append('price', form.price.toString());
      fd.append('capacity', form.capacity.toString());
      fd.append('maxCapacity', form.maxCapacity.toString());
      if (form.roomCount) fd.append('roomCount', form.roomCount.toString());
      if (form.roomType) fd.append('roomType', form.roomType);
      fd.append('status', form.status);
      fd.append('amenities', JSON.stringify(form.amenities));

      if (form.mainImageFile instanceof File) {
        fd.append('mainImageFile', form.mainImageFile);
      } else if (isEditMode.value && form.mainImageIdToKeep) {
        fd.append('mainImageId', form.mainImageIdToKeep.toString());
      }
      
      form.imageList.forEach(file => {
          if (file.raw instanceof File) { // 새로 추가된 파일만 전송
              fd.append('imageFiles', file.raw);
          }
      });

      if (form.deletedImageIds.length > 0) {
        fd.append('deletedImageIds', JSON.stringify(form.deletedImageIds));
      }
      
      try {
        let response;
        if (isEditMode.value && props.roomId) {
          response = await updateRoom(props.roomId, fd);
        } else if (props.accommodationId) {
          response = await createRoom(props.accommodationId, fd);
        }

        if (response && response.success) {
          ElNotification.success({ title: '성공', message: response.message || (isEditMode.value ? '객실 정보가 수정되었습니다.' : '객실이 등록되었습니다.') });
          
          let targetAccommodationId = null;
          if (isEditMode.value) {
            // 수정 모드에서는 populateForm에서 설정된 parentAccommodationId를 사용
            // 또는 API 응답(response.data)에 accommodationId가 있다면 그것을 우선 사용
            targetAccommodationId = response.data?.accommodationId || form.parentAccommodationId;
          } else {
            targetAccommodationId = props.accommodationId; // 신규 등록 모드
          }

          if (targetAccommodationId) {
            router.push({ name: 'HostRoomList', params: { accommodationId: targetAccommodationId } });
          } else {
            // targetAccommodationId를 알 수 없는 경우 (이런 경우는 거의 없어야 함)
            ElMessage.warn('숙소 ID를 찾을 수 없어 이전 페이지로 돌아갑니다.');
            router.back(); 
          }
        } else {
          ElMessage.error(response.message || '작업 중 오류가 발생했습니다.');
        }
      } catch (error) {
        console.error("Room form submission error:", error);
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
.room-form-container {
  max-width: 800px;
  margin: 20px auto;
}
.w-100 {
  width: 100%;
}
</style> 