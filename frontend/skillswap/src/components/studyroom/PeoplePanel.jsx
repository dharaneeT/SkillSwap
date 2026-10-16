// components/studyroom/PeoplePanel.jsx  (NEW FILE)
import { useEffect, useState } from "react";
import {
  fetchMembers,
  removeMember,
  restoreMember,
} from "../../api/studyRoomApi";
import { getErrorMessage } from "../../api/axios";
import { useFetch } from "../../hooks/useFetch";
import Icon from "../Icon";

function HostMembers({ roomId, openCount }) {
  const members = useFetch(fetchMembers, roomId);
  const [error, setError] = useState("");
  const { refetch } = members;

  useEffect(() => {
    if (openCount > 1) refetch();
  }, [openCount, refetch]);

  const act = async (m, remove) => {
    if (
      remove &&
      !window.confirm(
        `Remove ${m.name} from this room? They will not be able to rejoin until you restore them.`,
      )
    )
      return;
    setError("");
    try {
      await (remove ? removeMember : restoreMember)(roomId, m.userId);
      refetch();
    } catch (e) {
      setError(getErrorMessage(e));
    }
  };

  const list = members.data ?? [];
  return (
    <div className="space-y-2">
      <h3 className="card-title">
        Members ({list.filter((m) => m.status === "ACTIVE").length})
      </h3>
      {(error || members.error) && (
        <p role="alert" className="alert alert-error">
          {error || members.error}
        </p>
      )}
      <ul className="divide-y divide-line rounded-2xl bg-cream">
        {list.map((m) => (
          <li
            key={m.userId}
            className="flex items-center justify-between gap-2 px-3 py-2 text-sm"
          >
            <span className="flex items-center gap-2">
              {m.role === "HOST" && <Icon name="crown" size={14} />}
              {m.name}
              {m.status === "REMOVED" && (
                <span className="badge badge-danger">Removed</span>
              )}
            </span>
            {m.role !== "HOST" &&
              (m.status === "ACTIVE" ? (
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  onClick={() => act(m, true)}
                >
                  <Icon name="ban" size={14} /> Remove
                </button>
              ) : (
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  onClick={() => act(m, false)}
                >
                  Restore
                </button>
              ))}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default function PeoplePanel({
  roomId,
  presence,
  max,
  isHost,
  openCount,
}) {
  return (
    <section
      aria-label="People in this room"
      className="card card-white space-y-4"
    >
      <div className="flex items-center justify-between">
        <h2 className="section-title">Here now</h2>
        <span className="badge">
          {presence.length} / {max}
        </span>
      </div>
      {presence.length === 0 && <p className="meta">Connecting...</p>}
      <ul className="space-y-2" aria-live="polite">
        {presence.map((p) => (
          <li key={p.userId} className="flex items-center gap-2 text-sm">
            <span className="live-dot text-green-600" aria-hidden="true" />
            <span className="font-medium">{p.name}</span>
            {p.host && (
              <span className="badge badge-accepted">
                <Icon name="crown" size={11} /> Host
              </span>
            )}
          </li>
        ))}
      </ul>
      <p className="meta">
        This list shows people connected right now. People who closed the tab or
        lost connection disappear within a minute.
      </p>
      {isHost && <HostMembers roomId={roomId} openCount={openCount} />}
    </section>
  );
}
