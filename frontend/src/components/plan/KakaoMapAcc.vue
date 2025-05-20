<template>
  <div ref="mapContainer" style="width: 100%; height: 350px;"></div>
</template>

<script setup>
import { onMounted, ref } from 'vue'

const props = defineProps({
  address: {
    type: String,
    required: true
  }
})

const mapContainer = ref(null)

onMounted(() => {
  if (typeof kakao === 'undefined') {
    // Kakao SDK가 아직 로드되지 않았다면 동적으로 로드
    const script = document.createElement('script')
    script.src = '//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_APP_KEY&libraries=services'
    script.onload = () => initMap()
    document.head.appendChild(script)
  } else {
    initMap()
  }
})

function initMap() {
  const mapOption = {
    center: new kakao.maps.LatLng(33.450701, 126.570667),
    level: 3
  }

  const map = new kakao.maps.Map(mapContainer.value, mapOption)
  const geocoder = new kakao.maps.services.Geocoder()

  geocoder.addressSearch(props.address, function (result, status) {
    if (status === kakao.maps.services.Status.OK) {
      const coords = new kakao.maps.LatLng(result[0].y, result[0].x)

      const marker = new kakao.maps.Marker({
        map: map,
        position: coords
      })

      const infowindow = new kakao.maps.InfoWindow({
        content: `<div style="width:150px;text-align:center;padding:6px 0;">${props.address}</div>`
      })
      infowindow.open(map, marker)

      map.setCenter(coords)
    } else {
      alert('주소 검색에 실패했습니다.')
    }
  })
}
</script>

// <template>
//   <KakaoMap address="서울특별시 중구 세종대로 110" />
// </template>

// <script setup>
// import KakaoMap from './components/KakaoMap.vue'
// </script>

