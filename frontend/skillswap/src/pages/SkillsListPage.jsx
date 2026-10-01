import { useEffect, useState } from "react";
import { searchSkills } from "../api/skillApi";
import { getErrorMessage } from "../api/axios";

const PAGE_SIZE = 12;
const field =
  "rounded-lg bg-slate-800 border border-slate-600 px-3 py-2 outline-none focus:border-red-200";

export default function SkillsListPage() {
  const [search, setSearch] = useState("");
  const [debounced, setDebounced] = useState("");
  const [type, setType] = useState("");
  const [minRating, setMinRating] = useState("");
  const [sort, setSort] = useState("name,asc");
  const [page, setPage] = useState(0);
  const [result, setResult] = useState({ key: null, data: null, error: "" });

  // wait 300 ms after typing stops before searching
  useEffect(() => {
    const t = setTimeout(() => {
      setDebounced(search.trim());
      setPage(0);
    }, 300);
    return () => clearTimeout(t);
  }, [search]);

  const params = {
    page,
    size: PAGE_SIZE,
    sort,
    ...(debounced && { search: debounced }),
    ...(type && { type }),
    ...(minRating && { minRating }),
  };
  const key = JSON.stringify(params);

  useEffect(() => {
    let ignore = false;
    searchSkills(JSON.parse(key))
      .then(
        (res) => !ignore && setResult({ key, data: res.data.data, error: "" }),
      )
      .catch(
        (e) =>
          !ignore && setResult({ key, data: null, error: getErrorMessage(e) }),
      );
    return () => {
      ignore = true;
    };
  }, [key]);

  const loading = result.key !== key;
  const data = result.data;

  return (
    <main className="max-w-4xl mx-auto px-4 pt-10">
      <h1 className="text-2xl font-bold mb-4">Skills</h1>

      <div className="flex flex-wrap gap-3 mb-6">
        <input
          className={`${field} flex-1 min-w-48`}
          placeholder="Search skills..."
          value={search}
          maxLength={50}
          onChange={(e) => setSearch(e.target.value)}
        />
        <select
          className={field}
          value={type}
          onChange={(e) => {
            setType(e.target.value);
            setPage(0);
          }}
        >
          <option value="">Offered or wanted</option>
          <option value="OFFERED">Offered by someone</option>
          <option value="WANTED">Wanted by someone</option>
        </select>
        <select
          className={field}
          value={minRating}
          onChange={(e) => {
            setMinRating(e.target.value);
            setPage(0);
          }}
        >
          <option value="">Any provider rating</option>
          <option value="3">Providers 3★ and up</option>
          <option value="4">Providers 4★ and up</option>
        </select>
        <select
          className={field}
          value={sort}
          onChange={(e) => {
            setSort(e.target.value);
            setPage(0);
          }}
        >
          <option value="name,asc">Name A–Z</option>
          <option value="name,desc">Name Z–A</option>
          <option value="id,desc">Newest first</option>
        </select>
      </div>

      {result.error && !loading && (
        <p className="rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
          {result.error}
        </p>
      )}
      {loading && <p className="text-slate-400">Searching...</p>}
      {!loading && data && data.content.length === 0 && (
        <p className="text-slate-400">No skills match your filters.</p>
      )}

      {data && (
        <ul className="grid grid-cols-2 sm:grid-cols-3 gap-4">
          {data.content.map((s) => (
            <li
              key={s.id}
              className="rounded-xl bg-slate-800 border border-slate-700 p-4 text-center"
            >
              <p className="font-medium">{s.name}</p>
              <p className="text-xs text-slate-400 mt-1">
                {s.offeredCount} offering · {s.wantedCount} wanting
              </p>
            </li>
          ))}
        </ul>
      )}

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between mt-6 text-sm">
          <button
            disabled={!data.hasPrevious}
            onClick={() => setPage((p) => p - 1)}
            className="rounded-lg bg-slate-700 px-3 py-1 disabled:opacity-40 cursor-pointer"
          >
            Prev
          </button>
          <span className="text-slate-400">
            Page {data.page + 1} of {data.totalPages} · {data.totalElements}{" "}
            skills
          </span>
          <button
            disabled={!data.hasNext}
            onClick={() => setPage((p) => p + 1)}
            className="rounded-lg bg-slate-700 px-3 py-1 disabled:opacity-40 cursor-pointer"
          >
            Next
          </button>
        </div>
      )}
    </main>
  );
}
