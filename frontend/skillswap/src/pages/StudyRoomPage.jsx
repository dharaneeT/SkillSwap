// pages/StudyRoomPage.jsx  (NEW FILE)  route: /study-rooms/:id
import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  archiveRoom,
  endSession,
  fetchRoom,
  joinRoom,
  leaveRoom,
  startSession,
  updateRoom,
} from "../api/studyRoomApi";
import { getErrorMessage } from "../api/axios";
import { useFetch } from "../hooks/useFetch";
import { useRoomSocket } from "../hooks/useRoomSocket";
import Icon from "../components/Icon";
import ChatPanel from "../components/studyroom/ChatPanel";
import TimerPanel from "../components/studyroom/TimerPanel";
import NotesPanel from "../components/studyroom/NotesPanel";
import ChallengesPanel from "../components/studyroom/ChallengesPanel";
import PeoplePanel from "../components/studyroom/PeoplePanel";
import RewardsPanel from "../components/studyroom/RewardsPanel";
import RoomFormModal from "../components/studyroom/RoomFormModal";
import { difficultyLabel } from "../utils/studyRoom";

const TABS = [
  { id: "chat", label: "Chat", icon: "message-circle", mobileOnly: true },
  { id: "timer", label: "Timer", icon: "clock" },
  { id: "notes", label: "Notes", icon: "book-open" },
  { id: "challenges", label: "Challenges", icon: "trophy" },
  { id: "people", label: "People", icon: "users" },
  { id: "rewards", label: "Rewards", icon: "coins" },
];

function useIsDesktop() {
  const query = "(min-width: 1024px)";
  const [match, setMatch] = useState(() => window.matchMedia(query).matches);
  useEffect(() => {
    const mq = window.matchMedia(query);
    const on = () => setMatch(mq.matches);
    mq.addEventListener("change", on);
    return () => mq.removeEventListener("change", on);
  }, []);
  return match;
}

const ENDED_TEXT = {
  kicked: "You were removed from this room by the host.",
  full: "This room is full right now. Try again in a moment.",
  ended: "This room was closed by its host.",
  closed:
    "Could not connect to the room. Check your connection and reload the page.",
};

export default function StudyRoomPage() {
  const { id } = useParams();
  const roomId = Number(id);
  const navigate = useNavigate();
  const isDesktop = useIsDesktop();

  const detail = useFetch(fetchRoom, roomId);
  const d = detail.data;
  const member = Boolean(d?.room.member);
  const rt = useRoomSocket(roomId, { enabled: member, myUserId: d?.me.userId });
  const { applyTimer, setChatVisible, roomTick, reward } = rt;

  const [tab, setTab] = useState("chat");
  const [editing, setEditing] = useState(false);
  const [busy, setBusy] = useState(false);
  const [actionError, setActionError] = useState("");
  const [dismissedAt, setDismissedAt] = useState(0);
  const { refetch } = detail;

  // first timer state comes with the room details; after that the websocket keeps it current
  const firstTimer = d?.timer;
  useEffect(() => {
    if (firstTimer) applyTimer(firstTimer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [firstTimer?.revision, applyTimer]);

  // host changed something (session started/ended, room edited): reload the details
  useEffect(() => {
    if (roomTick > 0) refetch();
  }, [roomTick, refetch]);

  // reward popup: shown until dismissed or 7 seconds pass
  const toast = reward && reward.at !== dismissedAt ? reward : null;
  useEffect(() => {
    if (!reward) return;
    const t = setTimeout(() => setDismissedAt(reward.at), 7000);
    return () => clearTimeout(t);
  }, [reward]);

  const activeTab = isDesktop && tab === "chat" ? "timer" : tab;
  useEffect(() => {
    setChatVisible(isDesktop || tab === "chat");
  }, [isDesktop, tab, setChatVisible]);

  const run = async (fn, after) => {
    setBusy(true);
    setActionError("");
    try {
      await fn();
      after?.();
    } catch (e) {
      setActionError(getErrorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  // ---------- not loaded / not allowed ----------
  if (!d && detail.loading)
    return (
      <main className="page max-w-6xl">
        <p className="text-sm text-muted">Loading room...</p>
      </main>
    );
  if (!d) {
    return (
      <main className="page max-w-3xl space-y-4">
        <p role="alert" className="alert alert-error">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{detail.error || "Room not found"}</span>
        </p>
        <div className="flex gap-2">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={detail.refetch}
          >
            Retry
          </button>
          <Link to="/study-rooms" className="btn btn-primary">
            Back to study rooms
          </Link>
        </div>
      </main>
    );
  }

  const { room, me } = d;
  const isHost = me.host;
  const live = room.status === "LIVE";
  const dead = ["kicked", "full", "ended", "closed"].includes(rt.status);
  const here = rt.status === "open" ? rt.presence.length : room.participants;

  // public room I have not joined yet
  if (!member) {
    return (
      <main className="page max-w-3xl space-y-4">
        <div className="card card-white space-y-3">
          <span className="tag tag-periwinkle w-fit">{room.topic}</span>
          <h1 className="page-title text-3xl">{room.name}</h1>
          {room.description && <p className="page-sub">{room.description}</p>}
          <p className="meta">
            Hosted by {room.host.name} · {difficultyLabel(room.difficulty)} ·{" "}
            {here} / {room.maxParticipants} here now
          </p>
          {actionError && (
            <p role="alert" className="alert alert-error">
              {actionError}
            </p>
          )}
          <div className="flex gap-2">
            <button
              type="button"
              className="btn btn-primary"
              disabled={busy}
              onClick={() => run(() => joinRoom(roomId), refetch)}
            >
              Join this room
            </button>
            <Link to="/study-rooms" className="btn btn-secondary">
              Back
            </Link>
          </div>
        </div>
      </main>
    );
  }

  const archive = () => {
    if (
      !window.confirm(
        "Archive this room? Everyone is disconnected and it disappears from the list.",
      )
    )
      return;
    run(
      () => archiveRoom(roomId),
      () => navigate("/study-rooms"),
    );
  };
  const leave = () =>
    run(
      () => leaveRoom(roomId),
      () => navigate("/study-rooms"),
    );
  const saveEdit = async (body) => {
    await updateRoom(roomId, body);
    setEditing(false);
    refetch();
  };

  const visibleTabs = TABS.filter((t) => (isDesktop ? !t.mobileOnly : true));
  const panel = {
    timer: (
      <TimerPanel
        roomId={roomId}
        timer={rt.timer}
        applyTimer={applyTimer}
        isHost={isHost}
        membersCanPause={d.membersCanPause}
      />
    ),
    notes: (
      <NotesPanel
        roomId={roomId}
        notesEvent={rt.notesEvent}
        openCount={rt.openCount}
        myUserId={me.userId}
      />
    ),
    challenges: (
      <ChallengesPanel
        roomId={roomId}
        isHost={isHost}
        challengeTick={rt.challengeTick}
        openCount={rt.openCount}
      />
    ),
    people: (
      <PeoplePanel
        roomId={roomId}
        presence={rt.presence}
        max={room.maxParticipants}
        isHost={isHost}
        openCount={rt.openCount}
      />
    ),
    rewards: (
      <RewardsPanel
        roomId={roomId}
        reward={rt.reward}
        openCount={rt.openCount}
        live={live}
        minutes={d.minAttendanceMinutes}
      />
    ),
  }[activeTab];

  return (
    <main className="page max-w-6xl space-y-4">
      <nav aria-label="Breadcrumb">
        <Link
          to="/study-rooms"
          className="link inline-flex items-center gap-1 text-sm"
        >
          <Icon name="chevron-left" size={14} /> All study rooms
        </Link>
      </nav>

      {toast && (
        <div role="status" className="alert alert-success">
          <Icon name="award" size={18} className="mt-0.5" />
          <span className="flex-1">
            {toast.credits > 0
              ? `+${toast.credits} credit${toast.credits > 1 ? "s" : ""}! `
              : ""}
            {toast.reason}
          </span>
          <button
            type="button"
            aria-label="Dismiss"
            onClick={() => setDismissedAt(toast.at)}
          >
            <Icon name="x" size={14} />
          </button>
        </div>
      )}

      <header className="card card-white flex flex-wrap items-start justify-between gap-4">
        <div className="space-y-2">
          <div className="flex flex-wrap items-center gap-2">
            <span className="tag tag-periwinkle">{room.topic}</span>
            <span className="badge">{difficultyLabel(room.difficulty)}</span>
            {live ? (
              <span className="badge badge-success">
                <span className="live-dot" /> Session live
              </span>
            ) : (
              <span className="badge">No live session</span>
            )}
            {room.visibility === "PRIVATE" && (
              <span className="badge badge-pending">
                <Icon name="lock" size={11} /> Private
              </span>
            )}
          </div>
          <h1 className="page-title text-3xl">{room.name}</h1>
          {room.description && (
            <p className="page-sub max-w-2xl">{room.description}</p>
          )}
          <p className="meta">
            Hosted by {room.host.name} · {here} / {room.maxParticipants} here
            now · Room ID {room.id}
          </p>
          {isHost && d.accessCode && (
            <p className="text-sm">
              Access code:{" "}
              <code className="rounded bg-surface px-2 py-1 font-bold tracking-widest">
                {d.accessCode}
              </code>{" "}
              <span className="meta">
                (share it with people you invite, together with the Room ID)
              </span>
            </p>
          )}
        </div>

        <div className="flex flex-wrap gap-2">
          {isHost ? (
            <>
              {live ? (
                <button
                  type="button"
                  className="btn btn-secondary"
                  disabled={busy}
                  onClick={() => run(() => endSession(roomId), refetch)}
                >
                  End session
                </button>
              ) : (
                <button
                  type="button"
                  className="btn btn-primary"
                  disabled={busy}
                  onClick={() => run(() => startSession(roomId), refetch)}
                >
                  Start session
                </button>
              )}
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setEditing(true)}
              >
                Edit
              </button>
              <button
                type="button"
                className="btn btn-ghost"
                disabled={busy}
                onClick={archive}
              >
                <Icon name="trash-2" size={15} /> Archive
              </button>
            </>
          ) : (
            <button
              type="button"
              className="btn btn-secondary"
              disabled={busy}
              onClick={leave}
            >
              Leave room
            </button>
          )}
        </div>
      </header>

      {actionError && (
        <p role="alert" className="alert alert-error">
          {actionError}
        </p>
      )}

      {dead ? (
        <div role="alert" className="alert alert-warning flex-wrap">
          <span className="flex-1">{ENDED_TEXT[rt.status]}</span>
          <Link to="/study-rooms" className="btn btn-secondary btn-sm">
            Back to rooms
          </Link>
        </div>
      ) : (
        <>
          {rt.status === "reconnecting" && (
            <p role="status" className="alert alert-warning">
              Connection lost. Reconnecting... your messages will be retried.
            </p>
          )}

          <div
            role="tablist"
            aria-label="Room sections"
            className="flex gap-2 overflow-x-auto pb-1"
          >
            {visibleTabs.map((t) => (
              <button
                key={t.id}
                type="button"
                role="tab"
                id={`tab-${t.id}`}
                aria-selected={activeTab === t.id}
                aria-controls="room-panel"
                className="chip whitespace-nowrap"
                {...(activeTab === t.id ? { "aria-pressed": "true" } : {})}
                onClick={() => setTab(t.id)}
              >
                <Icon name={t.icon} size={14} /> {t.label}
                {t.id === "chat" && rt.unread > 0 && (
                  <span
                    className="badge badge-danger"
                    aria-label={`${rt.unread} unread messages`}
                  >
                    {rt.unread}
                  </span>
                )}
              </button>
            ))}
          </div>

          <div className="grid gap-4 lg:grid-cols-[minmax(0,1.1fr)_minmax(0,1fr)]">
            {(isDesktop || activeTab === "chat") && (
              <ChatPanel
                roomId={roomId}
                rt={rt}
                myUserId={me.userId}
                isHost={isHost}
              />
            )}
            {activeTab !== "chat" && (
              <div
                id="room-panel"
                role="tabpanel"
                aria-labelledby={`tab-${activeTab}`}
              >
                {panel}
              </div>
            )}
          </div>
        </>
      )}

      {editing && (
        <RoomFormModal
          title="Edit room"
          initial={d}
          submitLabel="Save changes"
          onSubmit={saveEdit}
          onClose={() => setEditing(false)}
        />
      )}
    </main>
  );
}
