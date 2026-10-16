// api/studyRoomApi.js  (NEW FILE)
// Every function returns the axios promise; useFetch unwraps res.data.data for you.
import api from "./axios";

const base = (id) => `/study-rooms/${id}`;

// ---- rooms ----
export const fetchRooms = (params) => api.get("/study-rooms", { params });
export const createRoom = (body) => api.post("/study-rooms", body);
export const fetchRoom = (id) => api.get(base(id));
export const updateRoom = (id, body) => api.put(base(id), body);
export const archiveRoom = (id) => api.delete(base(id));
export const joinRoom = (id, accessCode) =>
  api.post(`${base(id)}/join`, accessCode ? { accessCode } : {});
export const leaveRoom = (id) => api.post(`${base(id)}/leave`);

// ---- host: members + sessions ----
export const fetchMembers = (id) => api.get(`${base(id)}/members`);
export const removeMember = (id, userId) =>
  api.delete(`${base(id)}/members/${userId}`);
export const restoreMember = (id, userId) =>
  api.post(`${base(id)}/members/${userId}/restore`);
export const startSession = (id) => api.post(`${base(id)}/session/start`);
export const endSession = (id) => api.post(`${base(id)}/session/end`);

// ---- chat history (sending goes through the websocket) ----
export const fetchMessages = (id, params) =>
  api.get(`${base(id)}/messages`, { params });
export const deleteMessage = (id, messageId) =>
  api.delete(`${base(id)}/messages/${messageId}`);

// ---- timer ----
export const fetchTimer = (id) => api.get(`${base(id)}/timer`);
export const timerStart = (id, body) =>
  api.post(`${base(id)}/timer/start`, body);
export const timerPause = (id) => api.post(`${base(id)}/timer/pause`);
export const timerResume = (id) => api.post(`${base(id)}/timer/resume`);
export const timerReset = (id) => api.post(`${base(id)}/timer/reset`);

// ---- notes ----
export const fetchNotes = (id) => api.get(`${base(id)}/notes`);
export const saveNotes = (id, body) => api.put(`${base(id)}/notes`, body);

// ---- challenges ----
export const fetchChallenges = (id) => api.get(`${base(id)}/challenges`);
export const createChallenge = (id, body) =>
  api.post(`${base(id)}/challenges`, body);
export const closeChallenge = (id, cid) =>
  api.post(`${base(id)}/challenges/${cid}/close`);
export const submitChallenge = (id, cid, evidence) =>
  api.post(`${base(id)}/challenges/${cid}/submit`, { evidence });
export const fetchSubmissions = (id, cid) =>
  api.get(`${base(id)}/challenges/${cid}/submissions`);
export const approveSubmission = (id, cid, sid) =>
  api.post(`${base(id)}/challenges/${cid}/submissions/${sid}/approve`);
export const rejectSubmission = (id, cid, sid) =>
  api.post(`${base(id)}/challenges/${cid}/submissions/${sid}/reject`);

// ---- attendance ----
export const fetchMyAttendance = (id) => api.get(`${base(id)}/attendance/me`);
