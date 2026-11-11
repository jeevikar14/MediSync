import { useCallback, useEffect, useState } from 'react';
import api from './api';

/** Loads a GET endpoint; returns [data, reload, error]. */
export function useFetch(url, initial = []) {
  const [data, setData] = useState(initial);
  const [error, setError] = useState(null);
  const load = useCallback(() => {
    if (!url) return;
    api.get(url).then((r) => { setData(r.data); setError(null); }).catch((e) => setError(e));
  }, [url]);
  useEffect(load, [load]);
  return [data, load, error];
}

/** Patient list + id -> display name helper. */
export function usePatients() {
  const [patients] = useFetch('/patients');
  const name = (id) => {
    const p = patients.find((x) => x.id === Number(id));
    return p ? `${p.firstName} ${p.lastName}` : `Patient #${id}`;
  };
  return { patients, name };
}
