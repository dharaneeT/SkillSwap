import { useCallback, useEffect, useRef, useState } from "react";
import { getErrorMessage } from "../api/axios";

/**
 * useFetch(fetcher, params, { enabled })
 *  fetcher : an API function, e.g. fetchMe or searchSkills
 *  params  : passed to fetcher; a refetch happens when it changes
 *  enabled : set false to wait (e.g. until you have a user id)
 * Returns { data, error, loading, refetch }
 * `data` is res.data.data (your ApiResponse wrapper unwrapped).
 */
export function useFetch(fetcher, params = null, { enabled = true } = {}) {
  const [reloadCount, setReloadCount] = useState(0);
  const [result, setResult] = useState({ key: null, data: null, error: "" });

  // keep the latest fetcher without making it an effect dependency
  const fetcherRef = useRef(fetcher);
  useEffect(() => {
    fetcherRef.current = fetcher;
  });

  const paramsKey = JSON.stringify(params ?? null);
  const key = `${paramsKey}|${reloadCount}`;

  useEffect(() => {
    if (!enabled) return;
    let ignore = false; // ignore stale responses if params changed / unmounted
    fetcherRef
      .current(JSON.parse(paramsKey))
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
  }, [key, paramsKey, enabled]);

  const refetch = useCallback(() => setReloadCount((c) => c + 1), []);

  // loading = the latest request hasn't answered yet.
  // Old data stays visible during a refetch (no flicker).
  const loading = enabled && result.key !== key;

  return { data: result.data, error: result.error, loading, refetch };
}
