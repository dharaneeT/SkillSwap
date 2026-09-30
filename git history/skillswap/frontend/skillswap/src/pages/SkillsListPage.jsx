import { useEffect, useState } from "react";
import { fetchSkills } from "../api/skillApi";
import { getErrorMessage } from "../api/axios";
export default function SkillsListPage() {
  const [skills, setSkills] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let ignore = false; // avoids state updates after unmount / StrictMode double-run
    fetchSkills()
      .then((res) => {
        if (!ignore) setSkills(res.data.data);
      })
      .catch((err) => {
        if (!ignore) setError(getErrorMessage(err));
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });
    return () => {
      ignore = true;
    };
  }, []);

  return (
    <main className="max-w-4xl mx-auto px-4 pt-10">
      <h1 className="text-2xl font-bold mb-6">Skills</h1>

      {loading && <p className="text-slate-400">Loading skills...</p>}

      {error && (
        <p className="rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
          {error}
        </p>
      )}

      {!loading && !error && skills.length === 0 && (
        <p className="text-slate-400">No skills yet.</p>
      )}

      <ul className="grid grid-cols-2 sm:grid-cols-3 gap-4">
        {skills.map((skill) => (
          <li
            key={skill.id}
            className="rounded-xl bg-slate-800 border border-slate-700 p-4 text-center"
          >
            {skill.name}
          </li>
        ))}
      </ul>
    </main>
  );
}
