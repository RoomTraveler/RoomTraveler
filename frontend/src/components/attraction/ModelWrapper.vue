<template>
  <div
    class="modal fade show"
    tabindex="-1"
    style="display: block; background-color: rgba(0, 0, 0, 0.5)"
    @click.self="close"
  >
    <div class="modal-dialog modal-xl modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ id }}</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <div class="modal-body p-0">
          <!-- 여기서 자식 슬롯(=AttractionDetail) 렌더링 -->
          <slot :id="id"></slot>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineEmits, defineProps } from "vue";

const props = defineProps({
  id: { type: Number },
});
const emit = defineEmits(["close"]);

function close() {
  emit("close");
}
</script>

<style scoped>
/* 모달 뒤 배경 클릭시 닫기 위해 @click.self 사용 */
</style>
