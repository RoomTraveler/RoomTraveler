import { defineStore } from 'pinia'

export const useScrollStore = defineStore('scroll', {
  state: () => ({
    keyword: '',        // 검색어
    page: 0,            // 로드된 페이지 수
    items: [],          // 쌓인 검색 결과 목록
    scrollY: 0,         // 마지막 스크롤 위치
  }),
  actions: {
    saveState({ keyword, page, items, scrollY }) {
      this.keyword = keyword
      this.page    = page
      this.items   = items
      this.scrollY = scrollY
    },
    clearState() {
      this.keyword = ''
      this.page    = 0
      this.items   = []
      this.scrollY = 0
    }
  }
})