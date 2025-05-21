import { defineStore, acceptHMRUpdate } from "pinia";
import { ref, computed } from "vue";
export const useCommonStore = defineStore("common", () => {
  const _taskCnt = ref(0);
  const hasMoreTask = computed(() => _taskCnt.value > 0);
  const addTask = () => {
    _taskCnt.value++;
  };
  const removeTask = () => {
    _taskCnt.value--;
  };

  return { hasMoreTask, addTask, removeTask };
});

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useCommonStore, import.meta.hot));
}
