import api from "./axios";

export const addReview = (data) => api.post("/review/add", data);

export const fetchReviewsForUser = (userId, page = 0) =>
  api.get(`/review/user/${userId}`, { params: { page } });

export const fetchMyWrittenReviews = (page = 0) =>
  api.get("/review/my", { params: { page } });
