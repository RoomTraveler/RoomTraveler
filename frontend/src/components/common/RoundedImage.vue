<template>
  <div 
    :class="['overflow-hidden', containerClass]" 
    :style="{ borderRadius: roundedValue }"
  >
    <img 
      :src="src" 
      :alt="alt" 
      :class="['w-full', 'h-full', 'block', imgClass]" 
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { PropType } from 'vue';

const props = defineProps({
  src: {
    type: String as PropType<string>,
    required: true,
  },
  alt: {
    type: String as PropType<string>,
    required: true,
  },
  roundedClass: { // 이 prop은 이제 직접 사용되지 않지만, 호환성을 위해 남겨둘 수 있습니다.
    type: String as PropType<string>,
    default: 'rounded-xl', 
  },
  roundedValue: { // 실제 border-radius 값 (예: '0.75rem', '12px')
    type: String as PropType<string>,
    default: '0.75rem', // rounded-xl에 해당하는 값
  },
  imgClass: {
    type: String as PropType<string>,
    default: 'object-cover',
  },
  containerClass: {
    type: String as PropType<string>,
    default: 'w-full h-full',
  }
});
</script>
  