import { defineStore } from "pinia";
import api from "@/api/index";

export const useNotificationStore = defineStore("notification", {
  state: () => ({
    notifications: [],
    unreadCount: 0,
    totalItems: 0,
    totalPages: 0,
    currentPage: 1,
    loading: false,
    error: null,
    message: "",
  }),
  getters: {
    hasUnreadNotifications: (state) => state.unreadCount > 0,
  },
  actions: {
    async fetchNotifications(filter) {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.api.get("/api/notifications", { params: filter });
        this.notifications = response.data.content;
        this.totalItems = response.data.totalElements;
        this.totalPages = response.data.totalPages;
        this.currentPage = response.data.number + 1;
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

    async fetchUnreadCount() {
      try {
        const response = await api.api.get("/api/notifications/count/unread");
        this.unreadCount = response.data;
      } catch (err) {
        console.error("Error fetching unread notification count:", err);
      }
    },

    async markNotificationAsRead(notificationId) {
      this.error = null;
      this.message = "";
      try {
        await api.api.patch(`/api/notifications/${notificationId}/read`);
        const notification = this.notifications.find((n) => n.notificationId === notificationId);
        if (notification && !notification.read) {
          notification.read = true;
          if (this.unreadCount > 0) this.unreadCount--;
        }
        this.message = "알림을 읽음으로 표시했습니다.";
        return true;
      } catch (err) {
        console.error("Error marking notification as read:", err);
        this.error = "알림 읽음 처리 중 오류가 발생했습니다.";
        throw err;
      }
    },

    async markAllNotificationsAsRead() {
      if (this.unreadCount === 0) return;
      this.error = null;
      this.message = "";
      try {
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

    async deleteNotification(notificationId) {
      this.error = null;
      this.message = "";
      try {
        await api.api.delete(`/api/notifications/${notificationId}`);
        const idx = this.notifications.findIndex((n) => n.notificationId === notificationId);
        if (idx !== -1) {
          const removed = this.notifications.splice(idx, 1)[0];
          this.totalItems--;
          if (!removed.read && this.unreadCount > 0) this.unreadCount--;
        }
        this.message = "알림이 삭제되었습니다.";
      } catch (err) {
        console.error("Error deleting notification:", err);
        this.error = "알림 삭제 중 오류가 발생했습니다.";
        throw err;
      }
    },

    async deleteAllNotifications() {
      this.error = null;
      this.message = "";
      try {
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
