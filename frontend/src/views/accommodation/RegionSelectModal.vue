<template>
  <div
    v-if="show"
    class="fixed inset-0 z-[999] flex items-center justify-center bg-black/60 backdrop-blur-[2px] animate-fadein"
    @click.self="$emit('close')"
  >
    <div
      class="bg-white shadow-[0_8px_40px_0_rgba(70,90,200,0.14)] rounded-3xl w-[95vw] max-w-[440px] mx-auto px-8 py-10 relative animate-pop border border-[#e8eafd]"
    >
      <button
        class="absolute right-5 top-5 text-[28px] text-gray-400 hover:text-[#304ffe] transition-colors"
        @click="$emit('close')"
        aria-label="닫기"
      >
        <i class="bi bi-x"></i>
      </button>
      <div class="font-extrabold text-2xl text-[#21243d] mb-8 text-center tracking-tight drop-shadow">지역 선택</div>
      <div class="flex gap-8 min-h-[240px]">
        <!-- 시도 리스트 -->
        <div class="flex-1 overflow-y-auto custom-scrollbar pr-3">
          <div
            v-for="sido in sidoList"
            :key="sido.code"
            class="py-3 px-4 mb-2 rounded-2xl cursor-pointer select-none transition font-semibold text-lg text-gray-600 text-center tracking-wide"
            :class="
              sido.code === selectedSidoCode
                ? 'bg-gradient-to-br from-[#e4eeff] to-[#dbebff] text-[#304ffe] font-extrabold shadow-lg'
                : 'hover:bg-blue-50 hover:text-[#304ffe] hover:font-bold'
            "
            @click="selectSido(sido)"
          >
            {{ sido.name }}
          </div>
        </div>
        <!-- 구군 리스트 -->
        <div class="flex-1 overflow-y-auto custom-scrollbar pl-3" v-if="gugunList.length">
          <!-- 전체 버튼 추가 -->
          <div
            class="py-3 px-4 mb-2 rounded-2xl cursor-pointer select-none transition font-semibold text-lg text-gray-700 text-center tracking-wide"
            :class="
              selectedGugunCode === null
                ? 'bg-gradient-to-br from-[#e4eeff] to-[#dbebff] text-[#304ffe] font-extrabold shadow-lg'
                : 'hover:bg-blue-50 hover:text-[#304ffe] hover:font-bold'
            "
            @click="selectAllGugun"
          >
            전체
          </div>
          <div
            v-for="gugun in gugunList"
            :key="gugun.code"
            class="py-3 px-4 mb-2 rounded-2xl cursor-pointer select-none transition font-semibold text-lg text-gray-600 text-center tracking-wide"
            :class="
              gugun.code === selectedGugunCode
                ? 'bg-gradient-to-br from-[#e4eeff] to-[#dbebff] text-[#304ffe] font-extrabold shadow-lg'
                : 'hover:bg-blue-50 hover:text-[#304ffe] hover:font-bold'
            "
            @click="selectGugun(gugun)"
          >
            {{ gugun.name }}
          </div>
        </div>
      </div>
      <button
        v-if="selectedSidoCode"
        @click="apply"
        class="mt-10 w-full py-3 rounded-full bg-gradient-to-r from-[#72aaff] to-[#397cff] text-white font-extrabold text-lg shadow-xl transition hover:scale-[1.03] hover:brightness-105"
      >
        선택 완료
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import axios from "axios";

const props = defineProps({
  show: Boolean,
});
const emit = defineEmits(["close", "selected"]);

const sidoList = ref<any[]>([]);
const gugunList = ref<any[]>([]);
const selectedSidoCode = ref<number | null>(null);
const selectedGugunCode = ref<number | null>(null);
const selectedSidoName = ref("");
const selectedGugunName = ref("");

async function fetchSido() {
  try {
    const { data } = await axios.get("/api/region/sidos");
    sidoList.value = data;
    gugunList.value = [];
    selectedSidoCode.value = null;
    selectedGugunCode.value = null;
    selectedSidoName.value = "";
    selectedGugunName.value = "";
  } catch (err) {
    sidoList.value = [];
  }
}
async function fetchGugun(sidoCode: number) {
  try {
    const { data } = await axios.get("/api/region/guguns", { params: { sido: sidoCode } });
    gugunList.value = data;
    selectedGugunCode.value = null;
    selectedGugunName.value = "";
  } catch (err) {
    gugunList.value = [];
  }
}
function selectSido(sido: any) {
  selectedSidoCode.value = sido.code;
  selectedSidoName.value = sido.name;
  fetchGugun(sido.code);
  selectedGugunCode.value = null;
  selectedGugunName.value = "";
}
function selectGugun(gugun: any) {
  selectedGugunCode.value = gugun.code;
  selectedGugunName.value = gugun.name;
}
// "전체" 선택시
function selectAllGugun() {
  selectedGugunCode.value = null;
  selectedGugunName.value = "";
}
function apply() {
  let name = selectedSidoName.value;
  if (selectedGugunName.value) {
    name += ` ${selectedGugunName.value}`;
  }
  emit("selected", {
    sidoCode: selectedSidoCode.value,
    gugunCode: selectedGugunCode.value,
    name: name,
  });
  emit("close");
}
watch(
  () => props.show,
  (show) => {
    if (show) {
      fetchSido();
    }
  }
);
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 7px;
  background: #ecf1fa;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #c2d7fa;
  border-radius: 10px;
}
.animate-pop {
  animation: popIn 0.18s cubic-bezier(0.35, 1.65, 0.5, 1) both;
}
@keyframes popIn {
  0% {
    opacity: 0;
    transform: scale(0.93);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}
.animate-fadein {
  animation: fadeinBg 0.28s cubic-bezier(0.35, 1.65, 0.5, 1) both;
}
@keyframes fadeinBg {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}
</style>
