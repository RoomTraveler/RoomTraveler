<template>
  <ul style="list-style: none">
    <li v-for="(item, idx) in items" :key="item.order" class="mb-2">
      <span class="font-semibold">{{ idx + 1 }}.</span>
      {{ item.title }}
      <img :src="item.imageUrl" :alt="item.imageUrl" />
    </li>
  </ul>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
})
</script>
