import type { CatalogKind } from '../types';

const THEME_LABELS: Record<string, string> = {
  inspirational: 'Inspirational',
  tech: 'Tech',
  humanities: 'Humanities',
  arts: 'Arts',
  leadership: 'Leadership',
  democracy: 'Democracy',
  courage: 'Courage',
  legacy: 'Legacy',
};

/** Mirrors OratorCatalogKind.fromCategory. */
export function catalogKind(category: string): CatalogKind {
  const normalized = category.toLowerCase();
  if (normalized.includes('fictional') || normalized.includes('mythic')) return 'fictional';
  if (normalized.includes('literary')) return 'literary';
  return 'historical';
}

/** Mirrors WordThemes.displayName. */
export function themeLabel(theme: string): string {
  const known = THEME_LABELS[theme];
  if (known) return known;
  return theme
    .split(/\s+/)
    .map((part) => (part.length === 0 ? part : part.charAt(0).toUpperCase() + part.slice(1)))
    .join(' ');
}

/** Mirrors OratorPortraits.monogram. */
export function monogram(oratorName: string | null | undefined): string {
  if (!oratorName || oratorName.trim().length === 0) return '?';
  const parts = oratorName
    .replaceAll('.', ' ')
    .split(/\s+/)
    .filter((part) => part.length > 0 && part.toLowerCase() !== 'jr' && part.toLowerCase() !== 'mr');
  if (parts.length >= 3) return parts.slice(0, 3).map((part) => part.charAt(0).toUpperCase()).join('');
  if (parts.length === 2) return parts.map((part) => part.charAt(0).toUpperCase()).join('');
  const only = parts[0];
  if (!only) return '?';
  return only.slice(0, 2).toUpperCase();
}

export function formatYear(year: number | null | undefined): string | null {
  if (year == null || Number.isNaN(year)) return null;
  if (year < 0) return `${Math.abs(year)} BC`;
  return String(year);
}

/** Android color int to a CSS color. Very dark values fall back to gold. */
export function accentColor(value: number): string {
  const rgb = value >>> 0;
  const r = (rgb >> 16) & 255;
  const g = (rgb >> 8) & 255;
  const b = rgb & 255;
  if (r + g + b < 48) return '#d4af37';
  return `rgb(${r} ${g} ${b})`;
}

export function normalizeTitle(value: string): string {
  return value.toLowerCase().replace(/[^a-z0-9]+/g, ' ').trim();
}

export function dayOfYear(date: Date): number {
  const utc = Date.UTC(date.getFullYear(), date.getMonth(), date.getDate());
  const start = Date.UTC(date.getFullYear(), 0, 0);
  return Math.floor((utc - start) / 86_400_000);
}
