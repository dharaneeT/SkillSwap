import { useEffect, useState } from "react";
import { fetchMe } from "../api/userApi";
import { fetchSkills } from "../api/skillApi";
import { addUserSkill } from "../api/userSkillApi";
import { getErrorMessage } from "../api/axios";

const inputClass =
  "rounded-lg bg-slate-800 border border-slate-600 px-3 py-2 outline-none focus:border-red-200";

function Chips({ items, empty }) {
  if (!items?.length) return <p className="text-slate-500 text-sm">{empty}</p>;
  return (
    <ul className="flex flex-wrap gap-2">
      {items.map((s, i) => (
        <li key={i} className="rounded-full bg-slate-700 px-3 py-1 text-sm">
          {s}
        </li>
      ))}
    </ul>
  );
}

export default function ProfilePage() {
  const [profile, setProfile] = useState(null);
  const [skills, setSkills] = useState([]);
  const [form, setForm] = useState({ skillId: "", type: "OFFERED" });
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  useEffect(() => {
    let ignore = false;
    Promise.all([fetchMe(), fetchSkills()])
      .then(([me, sk]) => {
        if (ignore) return;
        setProfile(me.data.data);
        setSkills(sk.data.data);
      })
      .catch((err) => !ignore && setError(getErrorMessage(err)));
    return () => {
      ignore = true;
    };
  }, []);

  const handleAdd = async (e) => {
    e.preventDefault();
    setError("");
    setNotice("");
    try {
      await addUserSkill({
        userId: profile.id,
        skillId: Number(form.skillId),
        type: form.type,
      });
      const res = await fetchMe(); // refresh lists
      setProfile(res.data.data);
      setNotice("Skill added");
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  if (!profile) {
    return (
      <main className="max-w-3xl mx-auto px-4 pt-10">
        {error ? (
          <p className="text-red-300">{error}</p>
        ) : (
          <p className="text-slate-400">Loading...</p>
        )}
      </main>
    );
  }

  return (
    <main className="max-w-3xl mx-auto px-4 pt-10 space-y-8">
      <section className="rounded-xl bg-slate-800 border border-slate-700 p-5 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">{profile.name}</h1>
          <p className="text-slate-400">{profile.email}</p>
        </div>
        <div className="text-right">
          <p className="text-3xl font-bold text-red-200">{profile.credits}</p>
          <p className="text-slate-400 text-sm">credits</p>
        </div>
      </section>

      <section className="grid sm:grid-cols-2 gap-6">
        <div>
          <h2 className="font-semibold mb-2">I can teach</h2>
          <Chips items={profile.offeredSkills} empty="Nothing added yet" />
        </div>
        <div>
          <h2 className="font-semibold mb-2">I want to learn</h2>
          <Chips items={profile.wantedSkills} empty="Nothing added yet" />
        </div>
      </section>

      <form onSubmit={handleAdd} className="space-y-3">
        <h2 className="font-semibold">Add a skill</h2>
        {error && (
          <p className="rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
            {error}
          </p>
        )}
        {notice && <p className="text-green-300 text-sm">{notice}</p>}
        <div className="flex flex-wrap gap-3">
          <select
            className={inputClass}
            value={form.skillId}
            required
            onChange={(e) => setForm({ ...form, skillId: e.target.value })}
          >
            <option value="">Choose skill...</option>
            {skills.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
          <select
            className={inputClass}
            value={form.type}
            onChange={(e) => setForm({ ...form, type: e.target.value })}
          >
            <option value="OFFERED">I can teach this</option>
            <option value="WANTED">I want to learn this</option>
          </select>
          <button className="rounded-lg bg-red-200 text-slate-900 font-semibold px-4 py-2 cursor-pointer">
            Add
          </button>
        </div>
      </form>
      <p className="text-sm text-slate-300">
        {profile.reviewCount > 0
          ? `★ ${profile.averageRating.toFixed(1)} (${profile.reviewCount} reviews)`
          : "No reviews yet"}
      </p>
    </main>
  );
}
