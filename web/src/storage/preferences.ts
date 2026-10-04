export const SELECTED_ORATOR_KEY = 'rhetorica.selected-orator.v1';

export function readSelectedOratorId(storage: Pick<Storage, 'getItem'>): number | null {
  const raw = storage.getItem(SELECTED_ORATOR_KEY);
  if (!raw) return null;
  const id = Number(raw);
  if (!Number.isInteger(id) || id <= 0) return null;
  return id;
}

export function writeSelectedOratorId(storage: Pick<Storage, 'setItem' | 'removeItem'>, id: number | null): void {
  if (id == null) storage.removeItem(SELECTED_ORATOR_KEY);
  else storage.setItem(SELECTED_ORATOR_KEY, String(id));
}
