<template>
  <div class="col-md-8">
    <div ref="mapContainer" style="width: 100%; height: 400px" class="border"></div>
  </div>
</template>

<script setup>
import { onMounted, ref, defineProps } from 'vue'

const props = defineProps({ items: Array })

const mapContainer = ref(null)
const mapInstance = ref(null)

onMounted(() => {
  loadKakaoMap(mapContainer.value)
})

const loadKakaoMap = (container) => {
  const script = document.createElement('script')
  script.src =
    '//dapi.kakao.com/v2/maps/sdk.js?appkey=a1b7d43f74e8d7c4fa60d02ce2c13f58&autoload=false'
  document.head.appendChild(script)

  script.onload = () => {
    window.kakao.maps.load(() => {
      const centerLatLng = getCenterOfItems()
      const options = {
        center: new window.kakao.maps.LatLng(centerLatLng.latitude, centerLatLng.longitude),
        level: 6,
      }
      mapInstance.value = new window.kakao.maps.Map(container, options)
      setMarkers()
      mapInstance.value.setDraggable(false)
      mapInstance.value.setZoomable(false)
    })
  }
}

const getCenterOfItems = () => {
  let sumLat = 0
  let sumLng = 0

  props.items.forEach((item) => {
    sumLat += item.latitude
    sumLng += item.longitude
  })

  return {
    latitude: sumLat / props.items.length,
    longitude: sumLng / props.items.length,
  }
}

const setMarkers = () => {
  const bounds = new window.kakao.maps.LatLngBounds()

  props.items.forEach((item, index) => {
    const position = new window.kakao.maps.LatLng(item.latitude, item.longitude)

    const content = `<div style="
  background: #3f51b5;
  color: white;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  font-weight: bold;
  box-shadow: 0 0 5px rgba(0,0,0,0.3);
">
  ${index + 1}
</div>`

    const customOverlay = new window.kakao.maps.CustomOverlay({
      position: new window.kakao.maps.LatLng(item.latitude, item.longitude),
      content,
      xAnchor: 0.5,
      yAnchor: 0.5,
    })
    customOverlay.setMap(mapInstance.value)

    bounds.extend(position)

    if (index < props.items.length - 1) {
      const next = props.items[index + 1]
      const linePath = [position, new window.kakao.maps.LatLng(next.latitude, next.longitude)]

      new window.kakao.maps.Polyline({
        path: linePath,
        strokeWeight: 4,
        strokeColor: getColorByIndex(index, props.items.length),
        strokeOpacity: 0.8,
        strokeStyle: 'solid',
        map: mapInstance.value,
      })
    }
  })

  mapInstance.value.setBounds(bounds)
}

const getColorByIndex = (i, total) => `hsl(${(i / total) * 360}, 80%, 60%)`
</script>
