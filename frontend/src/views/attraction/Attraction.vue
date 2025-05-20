<template>
  <div class="p-4">
    <AttractionDetail @location-loaded="onLocationLoaded" />
    <KakaoMapOneAttr :latitude="latitude" :longitude="longitude" />
  </div>
</template>

<script setup>
import { ref } from "vue";
import AttractionDetail from "@/components/attraction/AttractionDetail.vue";
import KakaoMapOneAttr from "@/components/attraction/KakaoMapOneAttr.vue";

const latitude = ref(null);
const longitude = ref(null);

const onLocationLoaded = (location) => {
  latitude.value = location.latitude;
  longitude.value = location.longitude;
};
</script>
