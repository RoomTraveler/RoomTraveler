<template>
  <div>
    <Header />
    <div class="container">
      <!-- 메인 콘텐츠 영역 -->
      <slot></slot>
    </div>
    <Footer />
  </div>
</template>

<script>
/**
 * 레이아웃 컴포넌트
 * 
 * 이 컴포넌트는 웹사이트의 전체 레이아웃을 담당합니다.
 * Header와 Footer 컴포넌트를 포함하며, 메인 콘텐츠는 slot을 통해 주입됩니다.
 */
import Header from './Header.vue'
import Footer from './Footer.vue'

export default {
  name: 'Layout',
  components: {
    Header,
    Footer
  }
}
</script>

<style scoped>
/* 레이아웃 스타일 */
</style>