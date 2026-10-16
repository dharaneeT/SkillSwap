// components/studyroom/RoomCard.jsx  (NEW FILE)
import Icon from "../Icon";
import { difficultyLabel, formatDateTime } from "../../utils/studyRoom";

export default function RoomCard({ room, busy, onJoin }) {
  const full = room.participants >= room.maxParticipants;
  const live = room.status === "LIVE";
  const upcoming =
    room.scheduledStart && new Date(room.scheduledStart) > new Date() && !live;

  return (
    <article className="card card-white flex h-full flex-col gap-3">
      <div className="flex flex-wrap items-center gap-2">
        <span className="tag tag-periwinkle">{room.topic}</span>
        <span className="badge">{difficultyLabel(room.difficulty)}</span>
        {live && (
          <span className="badge badge-success">
            <span className="live-dot" /> Live
          </span>
        )}
        {room.visibility === "PRIVATE" && (
          <span className="badge badge-pending">
            <Icon name="lock" size={11} /> Private
          </span>
        )}
      </div>

      <div className="space-y-1">
        <h3 className="card-title text-lg">{room.name}</h3>
        {room.description && (
          <p className="line-clamp-2 text-sm text-muted">{room.description}</p>
        )}
      </div>

      <dl className="meta mt-auto space-y-1">
        <div className="flex items-center gap-2">
          <Icon name="crown" size={14} />
          <dt className="sr-only">Host</dt>
          <dd>Hosted by {room.host.name}</dd>
        </div>
        <div className="flex items-center gap-2">
          <Icon name="users" size={14} />
          <dt className="sr-only">Participants</dt>
          <dd>
            {room.participants} / {room.maxParticipants} here now
          </dd>
        </div>
        <div className="flex items-center gap-2">
          <Icon name="clock" size={14} />
          <dt className="sr-only">Activity</dt>
          <dd>{room.activity}</dd>
        </div>
        {upcoming && (
          <div className="flex items-center gap-2">
            <Icon name="calendar" size={14} />
            <dt className="sr-only">Scheduled</dt>
            <dd>
              Starts {formatDateTime(room.scheduledStart)}
              {room.durationMinutes ? ` · ${room.durationMinutes} min` : ""}
            </dd>
          </div>
        )}
      </dl>

      <button
        type="button"
        className="btn btn-primary w-full"
        disabled={busy || (full && !room.member)}
        onClick={() => onJoin(room)}
      >
        {room.member ? "Open room" : full ? "Room is full" : "Join room"}
        {!full || room.member ? <Icon name="arrow-right" size={15} /> : null}
      </button>
    </article>
  );
}
