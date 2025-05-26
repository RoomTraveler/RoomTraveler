import { defineStore, acceptHMRUpdate } from "pinia";
import { ref } from "vue";
import regionApi from "@/api/regionApi"; // 경로는 실제 프로젝트 구조에 맞게!

export const useCommonStore = defineStore("common", () => {
  // 시도/구군 상태
  const sidos = ref([]);
  const guguns = ref([]);

  // 시도 목록 불러오기
  async function fetchSidos() {
    try {
      const res = await regionApi.getSidos();
      // [{code, name}] → [{sidoCode, sidoName}]
      sidos.value = Array.isArray(res.data)
        ? res.data.map((s) => ({
            sidoCode: s.code,
            sidoName: s.name,
          }))
        : [];
    } catch (e) {
      sidos.value = [];
      console.error("시도 목록 불러오기 실패", e);
    }
  }

  // 구군 목록 불러오기
  async function fetchGuguns(sidoCode) {
    try {
      const res = await regionApi.getGuguns(sidoCode);
      guguns.value = Array.isArray(res.data)
        ? res.data.map((g) => ({
            gugunCode: g.code,
            gugunName: g.name,
          }))
        : [];
    } catch (e) {
      guguns.value = [];
      console.error("구군 목록 불러오기 실패", e);
    }
  }

  return {
    sidos,
    guguns,
    fetchSidos,
    fetchGuguns,
  };
});

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useCommonStore, import.meta.hot));
}
