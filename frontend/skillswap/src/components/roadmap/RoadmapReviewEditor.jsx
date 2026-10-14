// components/roadmap/RoadmapReviewEditor.jsx  (NEW FILE)
// Lets the user edit a generated roadmap BEFORE it is saved.
import Icon from "../Icon";
import { LEVELS, TASK_TYPES } from "../../utils/roadmap";

const blankTask = () => ({
  title: "",
  description: "",
  type: "LEARN",
  estimatedMinutes: 30,
});
const blankMilestone = () => ({
  title: "",
  description: "",
  estimatedHours: 4,
  learningObjectives: [],
  completionCriteria: "",
  dependsOn: [],
  tasks: [blankTask()],
});

export default function RoadmapReviewEditor({ draft, onChange }) {
  const set = (patch) => onChange({ ...draft, ...patch });

  const setMilestone = (i, patch) =>
    set({
      milestones: draft.milestones.map((m, idx) =>
        idx === i ? { ...m, ...patch } : m,
      ),
    });

  const setTask = (i, j, patch) =>
    setMilestone(i, {
      tasks: draft.milestones[i].tasks.map((t, k) =>
        k === j ? { ...t, ...patch } : t,
      ),
    });

  const addMilestone = () =>
    set({ milestones: [...draft.milestones, blankMilestone()] });

  // keep "depends on" numbers correct when a milestone disappears
  const removeMilestone = (i) => {
    const removed = i + 1;
    set({
      milestones: draft.milestones
        .filter((_, idx) => idx !== i)
        .map((m) => ({
          ...m,
          dependsOn: (m.dependsOn ?? [])
            .filter((d) => d !== removed)
            .map((d) => (d > removed ? d - 1 : d)),
        })),
    });
  };

  const addTask = (i) =>
    setMilestone(i, { tasks: [...draft.milestones[i].tasks, blankTask()] });
  const removeTask = (i, j) =>
    setMilestone(i, {
      tasks: draft.milestones[i].tasks.filter((_, k) => k !== j),
    });

  return (
    <div className="space-y-6">
      <section className="card card-white space-y-4">
        <h2 className="section-title">Roadmap details</h2>

        <div>
          <label
            className="mb-1 block text-sm font-semibold"
            htmlFor="rd-title"
          >
            Title
          </label>
          <input
            id="rd-title"
            className="input"
            maxLength={150}
            value={draft.title}
            onChange={(e) => set({ title: e.target.value })}
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-semibold" htmlFor="rd-desc">
            Description
          </label>
          <textarea
            id="rd-desc"
            className="textarea"
            rows={3}
            maxLength={4000}
            value={draft.description ?? ""}
            onChange={(e) => set({ description: e.target.value })}
          />
        </div>

        <div className="grid gap-4 sm:grid-cols-3">
          <div>
            <label
              className="mb-1 block text-sm font-semibold"
              htmlFor="rd-date"
            >
              Target date
            </label>
            <input
              id="rd-date"
              type="date"
              className="input"
              value={draft.targetDate ?? ""}
              onChange={(e) => set({ targetDate: e.target.value })}
            />
          </div>
          <div>
            <label
              className="mb-1 block text-sm font-semibold"
              htmlFor="rd-hours"
            >
              Hours per week
            </label>
            <input
              id="rd-hours"
              type="number"
              min={1}
              max={80}
              className="input"
              value={draft.hoursPerWeek ?? ""}
              onChange={(e) => set({ hoursPerWeek: e.target.value })}
            />
          </div>
          <div>
            <label
              className="mb-1 block text-sm font-semibold"
              htmlFor="rd-target"
            >
              Target level
            </label>
            <select
              id="rd-target"
              className="input select"
              value={draft.targetLevel ?? ""}
              onChange={(e) => set({ targetLevel: e.target.value })}
            >
              <option value="">Not set</option>
              {LEVELS.map((l) => (
                <option key={l.value} value={l.value}>
                  {l.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <label
            className="mb-1 block text-sm font-semibold"
            htmlFor="rd-prereq"
          >
            Prerequisites{" "}
            <span className="meta font-normal">(one per line)</span>
          </label>
          <textarea
            id="rd-prereq"
            className="textarea"
            rows={2}
            value={(draft.prerequisites ?? []).join("\n")}
            onChange={(e) => set({ prerequisites: e.target.value.split("\n") })}
          />
        </div>
      </section>

      <section className="space-y-4">
        <h2 className="section-title">
          Milestones ({draft.milestones.length})
        </h2>

        {draft.milestones.map((m, i) => (
          <div key={i} className="card card-white space-y-4">
            <div className="flex items-start gap-3">
              <span className="medal medal-sm medal-periwinkle text-sm font-bold">
                {i + 1}
              </span>
              <input
                aria-label={`Milestone ${i + 1} title`}
                className="input"
                placeholder="Milestone title"
                maxLength={150}
                value={m.title}
                onChange={(e) => setMilestone(i, { title: e.target.value })}
              />
              <button
                type="button"
                className="btn-icon shrink-0"
                aria-label={`Remove milestone ${i + 1}`}
                title="Remove milestone"
                onClick={() => removeMilestone(i)}
              >
                <Icon name="x" size={18} />
              </button>
            </div>

            <textarea
              aria-label={`Milestone ${i + 1} description`}
              className="textarea"
              rows={2}
              placeholder="What is this milestone about?"
              value={m.description ?? ""}
              onChange={(e) => setMilestone(i, { description: e.target.value })}
            />

            <div className="grid gap-3 sm:grid-cols-3">
              <div>
                <label className="meta mb-1 block">Estimated hours</label>
                <input
                  type="number"
                  min={1}
                  max={300}
                  className="input"
                  value={m.estimatedHours ?? ""}
                  onChange={(e) =>
                    setMilestone(i, { estimatedHours: e.target.value })
                  }
                />
              </div>
              <div className="sm:col-span-2">
                <label className="meta mb-1 block">Completion criteria</label>
                <input
                  className="input"
                  value={m.completionCriteria ?? ""}
                  onChange={(e) =>
                    setMilestone(i, { completionCriteria: e.target.value })
                  }
                />
              </div>
            </div>

            <div>
              <label className="meta mb-1 block">
                Learning objectives (one per line)
              </label>
              <textarea
                className="textarea"
                rows={2}
                value={(m.learningObjectives ?? []).join("\n")}
                onChange={(e) =>
                  setMilestone(i, {
                    learningObjectives: e.target.value.split("\n"),
                  })
                }
              />
            </div>

            <div className="space-y-2">
              <p className="eyebrow">Tasks</p>
              {m.tasks.map((t, j) => (
                <div
                  key={j}
                  className="grid gap-2 rounded-2xl bg-surface p-3 sm:grid-cols-[1fr_9rem_6rem_auto]"
                >
                  <input
                    aria-label={`Task ${j + 1} title`}
                    className="input"
                    placeholder="Task title"
                    maxLength={200}
                    value={t.title}
                    onChange={(e) => setTask(i, j, { title: e.target.value })}
                  />
                  <select
                    aria-label="Task type"
                    className="input select"
                    value={t.type ?? "LEARN"}
                    onChange={(e) => setTask(i, j, { type: e.target.value })}
                  >
                    {TASK_TYPES.map((tt) => (
                      <option key={tt.value} value={tt.value}>
                        {tt.label}
                      </option>
                    ))}
                  </select>
                  <input
                    aria-label="Task minutes"
                    type="number"
                    min={5}
                    max={2400}
                    className="input"
                    placeholder="Minutes"
                    value={t.estimatedMinutes ?? ""}
                    onChange={(e) =>
                      setTask(i, j, { estimatedMinutes: e.target.value })
                    }
                  />
                  <button
                    type="button"
                    className="btn-icon"
                    aria-label={`Remove task ${j + 1}`}
                    title="Remove task"
                    onClick={() => removeTask(i, j)}
                  >
                    <Icon name="x" size={16} />
                  </button>
                </div>
              ))}
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => addTask(i)}
              >
                <Icon name="plus" size={14} />
                Add task
              </button>
            </div>
          </div>
        ))}

        <button
          type="button"
          className="btn btn-secondary"
          onClick={addMilestone}
        >
          <Icon name="plus" size={16} />
          Add milestone
        </button>
      </section>
    </div>
  );
}
