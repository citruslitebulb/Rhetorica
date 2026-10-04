/** Local calendar helpers. Android uses the device zone, so these stay in local time. */

export function localDateString(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function previousDateString(isoDate: string): string | null {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(isoDate);
  if (!match) return null;
  const date = new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]));
  if (Number.isNaN(date.getTime())) return null;
  date.setDate(date.getDate() - 1);
  return localDateString(date);
}

export function clampInt(value: number, min: number, max: number): number {
  if (!Number.isFinite(value)) return min;
  return Math.min(max, Math.max(min, Math.trunc(value)));
}

/**
 * Milliseconds until the next occurrence of a local clock time.
 * A time that is not strictly after [now] rolls forward one day.
 */
export function millisUntilNext(hour: number, minute: number, now: Date): number {
  const next = new Date(now);
  next.setHours(clampInt(hour, 0, 23), clampInt(minute, 0, 59), 0, 0);
  if (next.getTime() <= now.getTime()) next.setDate(next.getDate() + 1);
  return Math.max(1, next.getTime() - now.getTime());
}
