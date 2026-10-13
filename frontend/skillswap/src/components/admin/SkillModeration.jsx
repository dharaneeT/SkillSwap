// components/admin/SkillModeration.jsx  (NEW FILE)
// Admin queue: new skills start as PENDING. Approve them, or merge duplicates
// ("JS" -> "JavaScript"): users + sessions move to the target and "JS" becomes an alias.
import { useState } from "react";
import { useSelector } from "react-redux";
import {
  approveSkill,
  fetchPendingSkills,
  mergeSkill,
} from "../../api/adminApi";
import { getErrorMessage } from "../../api/axios";
import { useFetch } from "../../hooks/useFetch";
import Icon from "../Icon";

const btn = "btn btn-secondary btn-sm";

function PendingRow({ item, allSkills, busy, onApprove, onMerge }) {
  const suggested = item.suggestions ?? [];
  const [target, setTarget] = useState(suggested[0]?.id ?? "");
  const suggestedIds = new Set(suggested.map((s) => s.id));
  const others = allSkills.filter(
    (s) => s.id !== item.id && !suggestedIds.has(s.id),
  );

  return (
    <li className="card card-white space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="min-w-0">
          <p className="card-title truncate">{item.name}</p>
          <p className="meta">
            Used by {item.usageCount}{" "}
            {item.usageCount === 1 ? "person" : "people"}
          </p>
        </div>
        <button
          className="btn btn-primary btn-sm"
          disabled={busy}
          onClick={() => onApprove(item)}
        >
          <Icon name="check" size={13} />
          Approve
        </button>
      </div>

      <div className="flex flex-wrap items-center gap-2 border-t border-line pt-3">
        <label className="sr-only" htmlFor={`merge-${item.id}`}>
          Merge {item.name} into
        </label>
        <select
          id={`merge-${item.id}`}
          className="input select min-w-0 flex-1"
          value={target}
          onChange={(e) =>
            setTarget(e.target.value ? Number(e.target.value) : "")
          }
        >
          <option value="">Merge into...</option>
          {suggested.length > 0 && (
            <optgroup label="Suggested">
              {suggested.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </optgroup>
          )}
          <optgroup label="All skills">
            {others.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </optgroup>
        </select>
        <button
          className={btn}
          disabled={busy || !target}
          onClick={() => onMerge(item, target)}
        >
          <Icon name="repeat" size={13} />
          Merge
        </button>
      </div>
    </li>
  );
}

export default function SkillModeration({ onChanged }) {
  const allSkills = useSelector((s) => s.skills.items);
  const { data, error: loadError, refetch } = useFetch(fetchPendingSkills);
  const pending = data ?? [];

  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const run = async (id, fn, done) => {
    setBusyId(id);
    setError("");
    setNotice("");
    try {
      await fn();
      setNotice(done);
      refetch();
      onChanged?.();
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setBusyId(null);
    }
  };

  const handleApprove = (item) =>
    run(item.id, () => approveSkill(item.id), `"${item.name}" approved`);

  const handleMerge = (item, targetId) => {
    const target = allSkills.find((s) => s.id === targetId);
    if (
      !window.confirm(
        `Merge "${item.name}" into "${target?.name}"? Everyone using "${item.name}" is moved. This can't be undone.`,
      )
    )
      return;
    run(
      item.id,
      () => mergeSkill(item.id, targetId),
      `"${item.name}" merged into "${target?.name}"`,
    );
  };

  return (
    <section>
      <h2 className="section-title mb-4 flex items-center gap-2">
        <Icon name="shield-check" size={20} />
        Skills awaiting approval ({pending.length})
      </h2>

      {(error || loadError) && (
        <p role="alert" className="alert alert-error mb-3">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{error || loadError}</span>
        </p>
      )}
      {notice && !error && (
        <p role="status" className="alert alert-success mb-3">
          <Icon name="circle-check" size={18} className="mt-0.5" />
          <span>{notice}</span>
        </p>
      )}

      {data && pending.length === 0 && (
        <p className="card text-sm text-muted">Nothing to review.</p>
      )}

      <ul className="grid gap-3 md:grid-cols-2">
        {pending.map((p) => (
          <PendingRow
            key={p.id}
            item={p}
            allSkills={allSkills}
            busy={busyId === p.id}
            onApprove={handleApprove}
            onMerge={handleMerge}
          />
        ))}
      </ul>
    </section>
  );
}
