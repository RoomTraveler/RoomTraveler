import { defineStore } from 'pinia';
import apiGroup from '@/api'; // API axios 인스턴스 (경로 확인 필요)
// import axios from 'axios'; // 만약 api/index.js가 없다면 직접 axios 사용

export const usePaymentStore = defineStore('payment', {
  state: () => ({
    isLoading: false,
    error: null,
    paymentPrepareResponse: null, // 결제 준비 단계에서 받은 응답 (merchant_uid 등)
    paymentCompleteResponse: null, // 최종 결제 완료 응답
  }),
  actions: {
    async preparePayment(prepareData) {
      console.log('[PaymentStore] preparePayment action called with prepareData:', JSON.parse(JSON.stringify(prepareData))); // 로그 추가
      this.isLoading = true;
      this.error = null;
      this.paymentPrepareResponse = null;
      try {
        // CartCheckout.vue에서 사용하던 API 경로와 DTO 구조를 따릅니다.
        // prepareData는 { reservationsToCreate: [], specialRequests: "" } 형태여야 합니다.
        if (!prepareData || !Array.isArray(prepareData.reservationsToCreate) || prepareData.reservationsToCreate.length === 0) {
          console.error('[PaymentStore] Invalid prepareData: reservationsToCreate is missing or empty.', prepareData);
          throw new Error("결제할 예약 정보가 올바르지 않습니다.");
        }
        // 각 reservationToCreate 항목 유효성 검사 (간단한 예시)
        for (const item of prepareData.reservationsToCreate) {
          if (!item.roomId || !item.accommodationId || !item.checkInDate || !item.checkOutDate || item.guestCount == null || item.totalPrice == null) {
            console.error("[PaymentStore] 결제 준비 DTO 생성 중 예약 항목 정보 부족:", JSON.parse(JSON.stringify(item)));
            throw new Error(`'${item.roomName || '알 수 없는 객실'}' 항목의 정보가 결제를 위해 충분하지 않습니다.`);
          }
        }

        // 실제 백엔드로 전송될 데이터 (prepareData 자체가 될 수도 있고, 가공될 수도 있음)
        const dataToSendToApi = prepareData; 
        console.log('[PaymentStore] Data being sent to /api/v1/payments/prepare:', JSON.parse(JSON.stringify(dataToSendToApi))); // 로그 추가

        const response = await apiGroup.api.post('/api/v1/payments/prepare', dataToSendToApi); // apiGroup.api 사용 (인증 필요 가정)
        console.log('[PaymentStore] /api/v1/payments/prepare API response:', response);
        this.paymentPrepareResponse = response.data;
        return response.data; // { merchantUid, amount, paymentName, buyerEmail, buyerName, buyerTel }
      } catch (err) {
        console.error("[PaymentStore] Error in paymentStore.preparePayment:", err);
        if (err.response) {
            console.error("[PaymentStore] API Error Response Data:", err.response.data);
            console.error("[PaymentStore] API Error Response Status:", err.response.status);
        }
        this.error = err.response?.data?.error || err.response?.data?.message || err.message || '결제 준비 중 오류가 발생했습니다.';
        throw this.error; // 컴포넌트에서 catch 할 수 있도록 에러 다시 throw
      } finally {
        this.isLoading = false;
      }
    },

    async completePayment(completeData) {
      this.isLoading = true;
      this.error = null;
      this.paymentCompleteResponse = null;
      try {
        // CartCheckout.vue에서 사용하던 API 경로와 DTO 구조를 따릅니다.
        // completeData는 { impUid, merchantUid } 형태여야 합니다.
        if (!completeData || !completeData.impUid || !completeData.merchantUid) {
          throw new Error("결제 완료 정보가 올바르지 않습니다.");
        }
        const response = await apiGroup.api.post('/api/v1/payments/complete', completeData);
        this.paymentCompleteResponse = response.data;
        return response.data; // { message, paymentId, relatedReservationInfo }
      } catch (err) {
        console.error("Error in paymentStore.completePayment:", err);
        this.error = err.response?.data?.error || err.message || '결제 완료 처리 중 오류가 발생했습니다.';
        throw this.error;
      } finally {
        this.isLoading = false;
      }
    },

    // (선택적) 아임포트 결제창에서 사용자가 결제를 취소하거나 실패했을 때,
    // /prepare 단계에서 생성된 PENDING_PAYMENT 상태의 예약을 취소하는 로직.
    // 백엔드에 해당 merchantUid의 예약들을 취소(또는 재고 복구)하는 API가 필요합니다.
    async cancelPreparedReservations(merchantUid) {
      if (!merchantUid) return;
      try {
        // 예시: 백엔드에 merchantUid로 예약 취소 요청
        // await api.post(`/api/v1/reservations/cancel-by-merchant/${merchantUid}`);
        console.log(`PaymentStore: Prepared reservations for merchantUid ${merchantUid} should be cancelled.`);
        // 실제 취소 로직은 백엔드 구현에 따라 달라집니다.
        // 이 함수는 아임포트 콜백에서 결제 실패 시 호출될 수 있습니다.
      } catch (err) {
        console.error(`Error cancelling prepared reservations for merchantUid ${merchantUid}:`, err);
        // 여기서 발생하는 오류는 사용자에게 직접적으로 큰 영향을 주지 않을 수 있으므로, 로깅만 할 수도 있습니다.
      }
    },

    clearError() {
      this.error = null;
    }
  },
}); 