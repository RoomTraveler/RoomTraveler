import { defineStore } from "pinia";
import { ref } from "vue";
import apiUtils from "@/api";
import { useUserStore } from "./userStore";

export const useHostStore = defineStore("host", () => {
  const isLoading = ref(false);
  const error = ref(null);
  const registrationStatus = ref(null);

  async function registerHost(formData) {
    isLoading.value = true;
    error.value = null;
    registrationStatus.value = null;
    const userStore = useUserStore();
    const accessToken = userStore._tokens?.access_token;
    const headers = accessToken ? { Authorization: `Bearer ${accessToken}` } : {};

    try {
      const response = await apiUtils.api.post("/api/host/register", formData, { headers });
      if (response.data && (response.status === 200 || response.status === 201)) {
        registrationStatus.value = "success";
        await userStore.fetchCurrentUser();
        return { success: true, data: response.data };
      } else {
        throw new Error(
            response.data?.message ||
            "호스트 등록에 실패했습니다 (서버 응답 오류)."
        );
      }
    } catch (err) {
      let errorMessage = "호스트 등록 중 오류가 발생했습니다.";
      if (err.response) {
        errorMessage =
            err.response.data?.error ||
            err.response.data?.message ||
            `(${err.response.status}) 서버 응답 오류`;
      } else if (err.message) {
        errorMessage = err.message;
      }
      error.value = errorMessage;
      registrationStatus.value = "failed";
      if (err.response?.status === 403) {
        console.error("호스트 등록 접근이 거부되었습니다. 사용자 권한을 확인하세요.");
      }
      return { success: false, error: errorMessage };
    } finally {
      isLoading.value = false;
    }
  }

  function resetHostState() {
    isLoading.value = false;
    error.value = null;
    registrationStatus.value = null;
  }

  return {
    isLoading,
    error,
    registrationStatus,
    registerHost,
    resetHostState,
  };
});
