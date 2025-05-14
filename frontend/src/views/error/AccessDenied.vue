<template>
  <Layout>
    <div class="container mt-5">
      <div class="row justify-content-center">
        <div class="col-md-8">
          <div class="card border-danger">
            <div class="card-header bg-danger text-white">
              <h4 class="mb-0">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                접근 거부됨
              </h4>
            </div>
            <div class="card-body text-center">
              <div class="mb-4">
                <i class="bi bi-lock-fill text-danger" style="font-size: 5rem;"></i>
              </div>
              
              <h3 class="mb-3">접근 권한이 없습니다</h3>
              
              <p class="lead mb-4">
                {{ message || '요청하신 페이지에 접근할 권한이 없습니다.' }}
              </p>
              
              <div class="d-flex justify-content-center gap-3">
                <button @click="goBack" class="btn btn-outline-secondary">
                  <i class="bi bi-arrow-left me-2"></i>이전 페이지로
                </button>
                <router-link to="/" class="btn btn-primary">
                  <i class="bi bi-house-door me-2"></i>홈으로 이동
                </router-link>
              </div>
              
              <div class="mt-4 text-muted">
                <small>
                  문제가 지속되면 관리자에게 문의하세요: 
                  <a href="mailto:support@roomtraveler.com">support@roomtraveler.com</a>
                </small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 접근 거부 페이지 컴포넌트
 * 
 * 이 컴포넌트는 사용자가 접근 권한이 없는 페이지에 접근했을 때 표시됩니다.
 * 사용자에게 접근 권한이 없음을 알리고, 이전 페이지나 홈으로 이동할 수 있는 옵션을 제공합니다.
 */
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'AccessDenied',
  components: {
    Layout
  },
  data() {
    return {
      message: this.$route.query.message || ''
    };
  },
  methods: {
    /**
     * 이전 페이지로 이동
     */
    goBack() {
      this.$router.go(-1);
    }
  }
};
</script>

<style scoped>
.card {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
}

.card-header {
  border-bottom: none;
}

.bi-lock-fill {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>