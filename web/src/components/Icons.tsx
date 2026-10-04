type IconProps = { className?: string };

export function ColumnMark({ className }: IconProps) {
  return (
    <svg className={className} viewBox="0 0 64 64" aria-hidden="true">
      <circle cx="32" cy="32" r="30" fill="none" stroke="currentColor" strokeWidth="1.5" />
      <rect x="16" y="46" width="32" height="3" fill="currentColor" />
      <rect x="20" y="42" width="24" height="2.5" fill="currentColor" />
      <rect x="25" y="24" width="14" height="18" fill="currentColor" />
      <rect x="18" y="21" width="28" height="2.5" fill="currentColor" />
      <circle cx="18" cy="21" r="3.2" fill="currentColor" />
      <circle cx="46" cy="21" r="3.2" fill="currentColor" />
      <rect x="15" y="16.5" width="34" height="2.4" fill="currentColor" />
    </svg>
  );
}

export function BookmarkIcon({ filled }: { filled: boolean }) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path
        d="M7 4.5h10a1 1 0 0 1 1 1V20l-6-3.2L6 20V5.5a1 1 0 0 1 1-1Z"
        fill={filled ? 'currentColor' : 'none'}
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinejoin="round"
      />
    </svg>
  );
}

export function IconOrators() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="8" r="3" fill="none" stroke="currentColor" strokeWidth="1.6" />
      <path d="M5.5 19.5c1.2-3 3.4-4.5 6.5-4.5s5.3 1.5 6.5 4.5" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
    </svg>
  );
}

export function IconWords() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M6 5.5h9.5A2.5 2.5 0 0 1 18 8v11.5H8.5A2.5 2.5 0 0 1 6 17V5.5Z" fill="none" stroke="currentColor" strokeWidth="1.6" />
      <path d="M9 9.5h6M9 13h6" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
    </svg>
  );
}

export function IconQuotes() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M8 16c-2 0-3.5-1.4-3.5-3.4S6 9 8.2 9c.3-1.8 1.6-3 3.3-3v2.2c-.7.3-1.2.9-1.3 1.7.2 0 .4-.1.7-.1 1.8 0 3.1 1.3 3.1 3.1S12.6 16 10.8 16H8Zm8 0c-2 0-3.5-1.4-3.5-3.4S14 9 16.2 9c.3-1.8 1.6-3 3.3-3v2.2c-.7.3-1.2.9-1.3 1.7.2 0 .4-.1.7-.1 1.8 0 3.1 1.3 3.1 3.1S20.6 16 18.8 16H16Z" fill="currentColor" />
    </svg>
  );
}

export function IconSaved() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M7 4.5h10a1 1 0 0 1 1 1V20l-6-3.2L6 20V5.5a1 1 0 0 1 1-1Z" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
    </svg>
  );
}

export function IconSpeeches() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M6 4.5h8l4 4V19.5H6v-15Z" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
      <path d="M14 4.5V9h4M8.5 12.5h7M8.5 16h5" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
    </svg>
  );
}

export function IconQuest() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="12" r="7.5" fill="none" stroke="currentColor" strokeWidth="1.6" />
      <path d="M9.2 10a2.8 2.8 0 1 1 3.6 2.7c-.7.3-1.1.8-1.1 1.6V15" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
      <circle cx="11.7" cy="17.2" r="0.8" fill="currentColor" />
    </svg>
  );
}

export function IconProfile() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="9" r="3" fill="none" stroke="currentColor" strokeWidth="1.6" />
      <path d="M6 19c1.1-2.6 3.2-4 6-4s4.9 1.4 6 4" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
    </svg>
  );
}
