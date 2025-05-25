<template>
  <Header />
  <div class="container py-4">
    <AttractionDetail @location-loaded="onLocationLoaded" />
    <div class="d-flex justify-content-center mt-5">
      <KakaoMapOneAttr :latitude="latitude" :longitude="longitude" />
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import AttractionDetail from "@/components/attraction/AttractionDetail.vue";
import KakaoMapOneAttr from "@/components/attraction/KakaoMapOneAttr.vue";
import Header from '@/components/layout/Header.vue'

const latitude = ref(null);
const longitude = ref(null);

const onLocationLoaded = (location) => {
  latitude.value = location.latitude;
  longitude.value = location.longitude;
};
</script>
