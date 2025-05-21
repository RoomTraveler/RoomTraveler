import { defineStore } from "pinia";
// import axios from "axios"; // 기존 axios import 주석 처리 또는 삭제
import api from "@/api/index"; // api/index.js에서 api 객체 가져오기
import { useUserStore } from "./userStore"; // 사용자 스토어 필요시 사용

export const useNotificationStore = defineStore("notification", {
  state: () => ({
    notifications: [],
    unreadCount: 0,
    totalItems: 0,
    totalPages: 0,
    currentPage: 1,
    loading: false,
    error: null,
    message: "", // 성공/정보 메시지 표시용
  }),
  getters: {
    // 예시: 읽지 않은 알림이 있는지 확인
    hasUnreadNotifications: (state) => state.unreadCount > 0,
  },
  actions: {
    /**
     * 알림 목록 가져오기 (페이지네이션 및 필터링 포함)
     * @param {object} filter - { type, readStatus, sortBy, page, size }
     */
    async fetchNotifications(filter) {
      this.loading = true;
      this.error = null;
      try {
        // API 엔드포인트는 NotificationController의 @GetMapping에 매칭됨
        const response = await api.api.get("/api/notifications", { params: filter });
        this.notifications = response.data.content;
        this.totalItems = response.data.totalElements;
        this.totalPages = response.data.totalPages;
        this.currentPage = response.data.number + 1; // Spring Pageable은 0부터 시작

        await this.fetchUnreadCount();

        return response.data;
      } catch (err) {
        console.error("Error fetching notifications:", err);
        this.error = "알림 목록을 불러오는 중 오류가 발생했습니다.";
        throw err;
      } finally {
        this.loading = false;
      }
    },

    /**
     * 읽지 않은 알림 개수 가져오기
     */
    async fetchUnreadCount() {
      try {
        // API 엔드포인트는 NotificationController의 @GetMapping("/count/unread")에 매칭됨
        const response = await api.api.get("/api/notifications/count/unread");
        this.unreadCount = response.data;
      } catch (err) {
        console.error("Error fetching unread notification count:", err);
        // 개수 조회 실패 시 에러를 별도로 표시하거나, 목록 조회 에러에 포함될 수 있음
      }
    },

    /**
     * 알림을 읽음으로 표시
     * @param {number|string} notificationId - 알림 ID
     */
    async markNotificationAsRead(notificationId) {
      this.error = null;
      this.message = "";
      try {
        await api.api.patch(`/api/notifications/${notificationId}/read`);
        const notification = this.notifications.find((n) => n.notificationId === notificationId);
        if (notification && !notification.read) {
          notification.read = true;
          if (this.unreadCount > 0) {
            this.unreadCount--;
          }
        }
        this.message = "알림을 읽음으로 표시했습니다.";
        return true;
      } catch (err) {
        console.error("Error marking notification as read:", err);
        this.error = "알림 읽음 처리 중 오류가 발생했습니다.";
        throw err;
      }
    },

    /**
     * 모든 알림을 읽음으로 표시 (현재 사용자 기준)
     */
    async markAllNotificationsAsRead() {
      if (this.unreadCount === 0) return;
      this.error = null;
      this.message = "";
      try {
        // API 엔드포인트는 NotificationController의 @PutMapping("/read-all")에 매칭됨
        await api.api.patch("/api/notifications/read-all");
        this.notifications.forEach((n) => (n.read = true));
        this.unreadCount = 0;
        this.message = "모든 알림을 읽음으로 표시했습니다.";
      } catch (err) {
        console.error("Error marking all notifications as read:", err);
        this.error = "모든 알림 읽음 처리 중 오류가 발생했습니다.";
        throw err;
      }
    },

    /**
     * 알림 삭제
     * @param {number|string} notificationId - 알림 ID
     */
    async deleteNotification(notificationId) {
      this.error = null;
      this.message = "";
      try {
        await api.api.delete(`/api/notifications/${notificationId}`);
        const index = this.notifications.findIndex((n) => n.notificationId === notificationId);
        if (index !== -1) {
          const removedNotification = this.notifications.splice(index, 1)[0];
          this.totalItems--;
          if (!removedNotification.read && this.unreadCount > 0) {
            this.unreadCount--;
          }
        }
        this.message = "알림이 삭제되었습니다.";
      } catch (err) {
        console.error("Error deleting notification:", err);
        this.error = "알림 삭제 중 오류가 발생했습니다.";
        throw err;
      }
    },

    /**
     * 모든 알림 삭제 (현재 사용자의 모든 알림)
     */
    async deleteAllNotifications() {
      this.error = null;
      this.message = "";
      try {
        // API 엔드포인트는 NotificationController의 @DeleteMapping("/delete-all")에 매칭됨
        await api.api.delete("/api/notifications/delete-all");
        this.notifications = [];
        this.unreadCount = 0;
        this.totalItems = 0;
        this.totalPages = 1;
        this.currentPage = 1;
        this.message = "모든 알림이 삭제되었습니다.";
      } catch (err) {
        console.error("Error deleting all notifications:", err);
        this.error = "모든 알림 삭제 중 오류가 발생했습니다.";
        throw err;
      }
    },

    clearMessage() {
      this.message = "";
    },
    clearError() {
      this.error = "";
    },
  },
});
