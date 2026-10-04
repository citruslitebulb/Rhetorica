import { useEffect, useMemo, useRef, useState } from 'react';
import { dictionaries } from '../data/catalog';
import { requestNotificationPermission } from '../lib/reminder';
import {
  DEFAULT_VOICE_FAMILIES,
  filterByFamilies,
  VOICE_FAMILIES,
  VOICE_FAMILY_COPY,
  voiceFamilyOf,
  type VoiceFamily,
} from '../lib/voiceFamily';
import { useLibrary } from '../state/LibraryProvider';
import type { ThemeMode } from '../storage/preferences';

const THEMES: { id: ThemeMode; label: string }[] = [
  { id: 'system', label: 'System' },
  { id: 'dark', label: 'Dark' },
  { id: 'light', label: 'Light' },
];

export function SetupFlow() {
  const library = useLibrary();
  const [step, setStep] = useState(0);
  const [families, setFamilies] = useState<Set<VoiceFamily>>(() => new Set(DEFAULT_VOICE_FAMILIES));
  const [themeMode, setThemeMode] = useState<ThemeMode>(library.preferences.themeMode);
  const [enabled, setEnabled] = useState(false);
  const [hour, setHour] = useState(library.preferences.notificationHour);
  const [minute, setMinute] = useState(library.preferences.notificationMinute);
  const [note, setNote] = useState('');
  const cardRef = useRef<HTMLElement>(null);

  useEffect(() => {
    cardRef.current?.focus();
  }, [step]);

  const counts = useMemo(() => {
    const tally = new Map<VoiceFamily, number>();
    for (const family of VOICE_FAMILIES) tally.set(family, 0);
    for (const orator of dictionaries) {
      const family = voiceFamilyOf(orator);
      tally.set(family, (tally.get(family) ?? 0) + 1);
    }
    return tally;
  }, []);

  if (library.preferences.onboardingStatus !== 'pending') return null;

  const matching = filterByFamilies(dictionaries, families);
  const canContinue = families.size > 0 && matching.length > 0;

  function toggleFamily(family: VoiceFamily) {
    setFamilies((current) => {
      const next = new Set(current);
      if (next.has(family)) next.delete(family);
      else next.add(family);
      return next;
    });
  }

  function chooseTheme(mode: ThemeMode) {
    setThemeMode(mode);
    library.previewTheme(mode);
  }

  function skip() {
    library.previewTheme(null);
    library.skipSetup();
  }

  async function toggleReminder(next: boolean) {
    if (!next) {
      setEnabled(false);
      setNote('');
      return;
    }
    const permission = await requestNotificationPermission();
    if (permission !== 'granted') {
      setEnabled(false);
      setNote(
        permission === 'unsupported'
          ? 'This browser does not offer notifications. You can leave the reminder off.'
          : 'The browser blocked notifications. You can leave the reminder off.',
      );
      return;
    }
    setEnabled(true);
    setNote('');
  }

  function begin() {
    library.finishSetup({
      families,
      themeMode,
      notificationsEnabled: enabled,
      notificationHour: hour,
      notificationMinute: minute,
    });
  }

  return (
    <div className="setup-scrim">
      <section className="setup-card" role="dialog" aria-modal="true" aria-labelledby="setup-title" tabIndex={-1} ref={cardRef}>
        <p className="meta">Step {step + 1} of 3</p>
        <div className="setup-progress" aria-hidden="true">
          <span style={{ width: `${((step + 1) / 3) * 100}%` }} />
        </div>
        {step === 0 ? (
          <>
            <h2 id="setup-title">What are you looking for?</h2>
            <p className="lede">
              Choose the voices for your daily word. Classical philosophers start selected. You can change this later
              in Profile.
            </p>
            <ul className="family-list">
              {VOICE_FAMILIES.map((family) => {
                const copy = VOICE_FAMILY_COPY[family];
                const on = families.has(family);
                return (
                  <li key={family}>
                    <button type="button" className={on ? 'family-card is-on' : 'family-card'} aria-pressed={on} onClick={() => toggleFamily(family)}>
                      <span>
                        <strong>{copy.title}</strong>
                        <span className="meta">{counts.get(family) ?? 0} voices</span>
                      </span>
                      <span className="description">{copy.body}</span>
                    </button>
                  </li>
                );
              })}
            </ul>
            {canContinue ? null : <p className="meta">Select at least one family to continue.</p>}
          </>
        ) : null}
        {step === 1 ? (
          <>
            <h2 id="setup-title">Appearance</h2>
            <p className="lede">Rhetorica is dark unless you choose otherwise. System follows this device.</p>
            <div className="chip-row" role="group" aria-label="Theme">
              {THEMES.map((theme) => (
                <button
                  key={theme.id}
                  type="button"
                  className={themeMode === theme.id ? 'chip is-on' : 'chip'}
                  aria-pressed={themeMode === theme.id}
                  onClick={() => chooseTheme(theme.id)}
                >
                  {theme.label}
                </button>
              ))}
            </div>
          </>
        ) : null}
        {step === 2 ? (
          <>
            <h2 id="setup-title">A daily reminder</h2>
            <p className="lede">
              Optional. While Rhetorica is open or installed, it can remind you at a local time you choose. A closed
              browser tab cannot be woken, and this is not a phone alert or a home-screen widget.
            </p>
            <div className="chip-row">
              <button type="button" className={enabled ? 'chip is-on' : 'chip'} aria-pressed={enabled} onClick={() => void toggleReminder(!enabled)}>
                {enabled ? 'Reminder on' : 'Reminder off'}
              </button>
            </div>
            <label className="field" htmlFor="setup-time">
              <span>Local time</span>
              <input
                id="setup-time"
                type="time"
                value={`${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`}
                onChange={(event) => {
                  const [hourText, minuteText] = event.target.value.split(':');
                  const nextHour = Number(hourText);
                  const nextMinute = Number(minuteText);
                  if (Number.isInteger(nextHour)) setHour(nextHour);
                  if (Number.isInteger(nextMinute)) setMinute(nextMinute);
                }}
              />
            </label>
            {note ? <p className="meta">{note}</p> : null}
          </>
        ) : null}
        <div className="action-row">
          <button type="button" className="button" onClick={skip}>
            Skip
          </button>
          {step > 0 ? (
            <button type="button" className="button" onClick={() => setStep((current) => current - 1)}>
              Back
            </button>
          ) : null}
          {step < 2 ? (
            <button type="button" className="button primary" disabled={step === 0 && !canContinue} onClick={() => setStep((current) => current + 1)}>
              Continue
            </button>
          ) : (
            <button type="button" className="button primary" onClick={begin}>
              Begin
            </button>
          )}
        </div>
      </section>
    </div>
  );
}
