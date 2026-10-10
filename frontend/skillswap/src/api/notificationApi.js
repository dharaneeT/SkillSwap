import api from "./axios";

export const fetchNotifications = () => api.get("/notifications");
export const markNotificationRead = (id) =>
  api.put(`/notifications/${id}/read`);
export const markAllNotificationsRead = () =>
  api.put("/notifications/read-all");
export const markChatRead = (userId) =>
  api.put(`/notifications/chat/${userId}/read`);
