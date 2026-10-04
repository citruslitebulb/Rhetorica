import { localDateString } from './dates';

export function reminderIsDue(args: {
  enabled: boolean;
  hour: number;
  minute: number;
  now: Date;
  lastNotifiedDate: string;
}): boolean {
  if (!args.enabled) return false;
  const today = localDateString(args.now);
  if (args.lastNotifiedDate === today) return false;
  const slot = new Date(args.now);
  slot.setHours(args.hour, args.minute, 0, 0);
  return args.now.getTime() >= slot.getTime();
}

export function speakSupported(): boolean {
  return typeof window !== 'undefined' && 'speechSynthesis' in window && typeof SpeechSynthesisUtterance !== 'undefined';
}

/** Speak the headword and definition. Unavailable speech fails quietly. */
export function speakWordAndDefinition(word: string, definition: string): void {
  if (!speakSupported()) return;
  const spokenWord = word.trim();
  const spokenDefinition = definition.trim();
  if (spokenWord.length === 0 && spokenDefinition.length === 0) return;
  const text = spokenDefinition.length > 0 ? `${spokenWord}. ${spokenDefinition}` : spokenWord;
  try {
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'en-US';
    window.speechSynthesis.speak(utterance);
  } catch {
    // The browser speech API can throw if voices are unavailable. Stay silent.
  }
}

export function notificationsSupported(): boolean {
  return typeof window !== 'undefined' && 'Notification' in window;
}

export async function requestNotificationPermission(): Promise<NotificationPermission | 'unsupported'> {
  if (!notificationsSupported()) return 'unsupported';
  try {
    return await Notification.requestPermission();
  } catch {
    return 'denied';
  }
}

export function showWordNotification(word: string, definition: string, wordId: number): void {
  if (!notificationsSupported() || Notification.permission !== 'granted') return;
  try {
    const notification = new Notification('Word of the Day', {
      body: `${word} — ${definition}`,
      icon: '/icons/icon-192.png',
      tag: 'rhetorica-word-of-day',
    });
    notification.onclick = () => {
      window.focus();
      window.location.assign(`/words/${wordId}`);
      notification.close();
    };
  } catch {
    // A missing permission or a blocked constructor should not surface as an error.
  }
}
