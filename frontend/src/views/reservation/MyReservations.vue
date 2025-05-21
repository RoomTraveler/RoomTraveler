<template>
  <div class="container mt-5 mb-5">
    <div class="my-reservations-container">
      <h2 class="mb-4">나의 예약 내역</h2>

      <!-- 알림 메시지 -->
      <div v-if="message" :class="`alert alert-${messageType} alert-dismissible fade show`" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>

      <!-- 로딩 상태 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">예약 내역을 불러오는 중입니다...</p>
      </div>

      <!-- 예약 내역이 없는 경우 -->
      <div v-else-if="reservations.length === 0" class="empty-state">
        <div class="empty-state-icon">
          <i class="bi bi-journal-bookmark-fill"></i>
        </div>
        <h4>예약 내역이 없습니다</h4>
        <p class="text-muted">아직 예약하신 내역이 없습니다. 마음에 드는 숙소를 찾아 예약해보세요.</p>
        <router-link to="/accommodation/list" class="btn btn-primary mt-3">
          <i class="bi bi-search"></i> 숙소 찾아보기
        </router-link>
      </div>

      <!-- 예약 내역 목록 -->
      <div v-else class="row">
        <div v-for="reservation in reservations" :key="reservation.reservationId" class="col-md-6 mb-4">
          <div class="card reservation-card h-100">
            <div class="reservation-header">
              <div>
                <h5 class="mb-1">{{ reservation.accommodationTitle }}</h5>
                <small class="text-muted">{{ reservation.roomName }}</small>
              </div>
              <span :class="getStatusBadgeClass(reservation.status)">
                {{ getStatusText(reservation.status) }}
              </span>
            </div>
            <div class="card-body reservation-body">
              <div class="row mb-2">
                <div class="col-sm-6">
                  <p><strong>예약 번호:</strong> {{ reservation.reservationId }}</p>
                  <p><strong>체크인:</strong> {{ formatDate(reservation.checkInDate) }}</p>
                  <p><strong>숙박 일수:</strong> {{ reservation.nights }}박</p>
                </div>
                <div class="col-sm-6">
                  <p><strong>결제 상태:</strong> {{ getPaymentStatusText(reservation.paymentStatus) }}</p>
                  <p><strong>체크아웃:</strong> {{ formatDate(reservation.checkOutDate) }}</p>
                  <p><strong>인원:</strong> {{ reservation.guestCount }}명</p>
                </div>
              </div>
              <p v-if="reservation.specialRequests"><strong>특별 요청:</strong> {{ reservation.specialRequests }}</p>
            </div>
            <div class="card-footer reservation-footer">
              <div class="reservation-price">
                {{ formatCurrency(reservation.totalPrice) }}
              </div>
              <div>
                <router-link
                  :to="`/reservation/detail/${reservation.reservationId}`"
                  class="btn btn-sm btn-outline-primary me-2"
                >
                  <i class="bi bi-info-circle"></i> 상세 보기
                </router-link>
                <button
                  v-if="canCancel(reservation.status)"
                  @click="confirmCancelReservation(reservation.reservationId)"
                  class="btn btn-sm btn-outline-danger"
                  :disabled="cancelling === reservation.reservationId"
                >
                  <span
                    v-if="cancelling === reservation.reservationId"
                    class="spinner-border spinner-border-sm"
                    role="status"
                    aria-hidden="true"
                  ></span>
                  <i v-else class="bi bi-calendar-x"></i> 예약 취소
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 예약 취소 확인 모달 -->
    <div
      class="modal fade"
      id="cancelConfirmModal"
      tabindex="-1"
      aria-labelledby="cancelConfirmModalLabel"
      aria-hidden="true"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="cancelConfirmModalLabel">예약 취소 확인</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">정말로 이 예약을 취소하시겠습니까? 이 작업은 되돌릴 수 없습니다.</div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
            <button type="button" class="btn btn-danger" @click="executeCancelReservation">예약 취소</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Modal } from "bootstrap";

export default {
  name: "MyReservations",
  data() {
    return {
      reservations: [],
      loading: false,
      message: "",
      messageType: "success", // 'success' or 'danger'
      cancelling: null, // 현재 취소 중인 예약 ID
      reservationToCancel: null, // 취소할 예약 ID 저장
      cancelConfirmModalInstance: null,
    };
  },
  created() {
    this.fetchMyReservations();
  },
  mounted() {
    this.cancelConfirmModalInstance = new Modal(document.getElementById("cancelConfirmModal"));
  },
  methods: {
    async fetchMyReservations() {
      this.loading = true;
      this.message = "";
      try {
        const response = await fetch("/api/reservation/my-reservations");
        if (!response.ok) {
          const errorData = await response.json().catch(() => ({ message: "예약 내역을 불러오는 데 실패했습니다." }));
          throw new Error(errorData.message || "서버 오류가 발생했습니다.");
        }
        const data = await response.json();
        this.reservations = data.reservations || [];
      } catch (error) {
        console.error("내 예약 목록 조회 중 오류:", error);
        this.message = error.message;
        this.messageType = "danger";
      } finally {
        this.loading = false;
      }
    },
    confirmCancelReservation(reservationId) {
      this.reservationToCancel = reservationId;
      this.cancelConfirmModalInstance.show();
    },
    async executeCancelReservation() {
      if (!this.reservationToCancel) return;
      this.cancelling = this.reservationToCancel;
      this.message = "";
      this.cancelConfirmModalInstance.hide();

      try {
        const response = await fetch(`/api/reservation/cancel/${this.reservationToCancel}`, {
          method: "POST",
        });
        const responseData = await response.json();
        if (!response.ok) {
          throw new Error(responseData.error || responseData.message || "예약 취소에 실패했습니다.");
        }
        this.message = responseData.message || "예약이 성공적으로 취소되었습니다.";
        this.messageType = "success";
        // 예약 목록 새로고침
        this.fetchMyReservations();
      } catch (error) {
        console.error("예약 취소 중 오류:", error);
        this.message = error.message;
        this.messageType = "danger";
      } finally {
        this.cancelling = null;
        this.reservationToCancel = null;
      }
    },
    getStatusBadgeClass(status) {
      switch (status) {
        case "CONFIRMED":
          return "badge bg-success";
        case "PENDING":
          return "badge bg-warning text-dark";
        case "CANCELLED":
          return "badge bg-secondary";
        case "COMPLETED":
          return "badge bg-primary";
        default:
          return "badge bg-info";
      }
    },
    getStatusText(status) {
      switch (status) {
        case "CONFIRMED":
          return "예약 확정";
        case "PENDING":
          return "예약 대기";
        case "CANCELLED":
          return "취소됨";
        case "COMPLETED":
          return "이용 완료";
        default:
          return status;
      }
    },
    getPaymentStatusText(paymentStatus) {
      switch (paymentStatus) {
        case "PAID":
          return "결제 완료";
        case "UNPAID":
          return "결제 대기";
        case "REFUNDED":
          return "환불됨";
        default:
          return paymentStatus;
      }
    },
    canCancel(status) {
      return ["PENDING", "CONFIRMED"].includes(status);
    },
    formatDate(dateString) {
      if (!dateString) return "-";
      const date = new Date(dateString);
      return new Intl.DateTimeFormat("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
      }).format(date);
    },
    formatCurrency(amount) {
      if (amount === null || amount === undefined) return "-";
      return new Intl.NumberFormat("ko-KR", {
        style: "currency",
        currency: "KRW",
        maximumFractionDigits: 0,
      }).format(amount);
    },
  },
};
</script>

<style scoped>
.my-reservations-container {
  max-width: 1000px;
  margin: 0 auto;
}
.reservation-card {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition:
    transform 0.2s ease-in-out,
    box-shadow 0.2s ease-in-out;
}
.reservation-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}
.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
}
.reservation-header h5 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}
.reservation-body {
  padding: 20px;
  font-size: 0.9rem;
}
.reservation-body p {
  margin-bottom: 0.5rem;
}
.reservation-body strong {
  color: #495057;
}
.reservation-footer {
  padding: 15px 20px;
  background-color: #f8f9fa;
  border-top: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.reservation-price {
  font-size: 1.25rem;
  font-weight: bold;
  color: #007bff; /* Primary color for price */
}
.empty-state {
  text-align: center;
  padding: 50px 0;
}
.empty-state-icon {
  font-size: 4rem;
  color: #6c757d;
  margin-bottom: 20px;
}

.btn-outline-danger {
  border-color: #dc3545;
  color: #dc3545;
}
.btn-outline-danger:hover {
  background-color: #dc3545;
  color: white;
}
</style>
