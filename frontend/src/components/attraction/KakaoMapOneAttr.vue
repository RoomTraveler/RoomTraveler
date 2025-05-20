<script setup>
import { KakaoMap, KakaoMapMarker } from "vue3-kakao-maps";
const props = defineProps({
  latitude: Number,
  longitude: Number,
});
</script>

<template>
  <KakaoMap :lat="latitude" :lng="longitude" :draggable="true">
    <KakaoMapMarker :lat="latitude" :lng="longitude"></KakaoMapMarker>
  </KakaoMap>
</template>
