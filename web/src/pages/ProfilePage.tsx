import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { oratorById } from '../data/catalog';
import { requestNotificationPermission } from '../lib/reminder';
import { useDocumentTitle } from '../lib/useDocumentTitle';
import { useLibrary } from '../state/LibraryProvider';
import type { ThemeMode } from '../storage/preferences';
import { SwitchRow } from '../components/SwitchRow';

const THEMES: { id: ThemeMode; label: string }[] = [
  { id: 'system', label: 'System' },
  { id: 'dark', label: 'Dark' },
  { id: 'light', label: 'Light' },
];

function clockValue(hour: number, minute: number): string {
  return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
}

export function ProfilePage() {
  useDocumentTitle('Profile');
  const library = useLibrary();
  const { preferences, snapshot, wordOfDay, progress } = library;
  const [query, setQuery] = useState('');
  const [reminderNote, setReminderNote] = useState('');
  const openedToday = wordOfDay != null && progress.openedWordIds.includes(wordOfDay.id);

  const orators = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return library.visibleOrators.filter((orator) => {
      if (!needle) return true;
      return `${orator.name} ${orator.oratorName}`.toLowerCase().includes(needle);
    });
  }, [library.visibleOrators, query]);

  async function onReminder(enabled: boolean) {
    if (!enabled) {
      setReminderNote('');
      library.setReminder(false, preferences.notificationHour, preferences.notificationMinute);
      return;
    }
    const permission = await requestNotificationPermission();
    if (permission !== 'granted') {
      setReminderNote(
        permission === 'unsupported'
          ? 'This browser does not offer notifications.'
          : 'The browser blocked notifications, so the reminder stays off.',
      );
      library.setReminder(false, preferences.notificationHour, preferences.notificationMinute);
      return;
    }
    setReminderNote('');
    library.setReminder(true, preferences.notificationHour, preferences.notificationMinute);
  }

  function onTime(value: string) {
    const [hourText, minuteText] = value.split(':');
    const hour = Number(hourText);
    const minute = Number(minuteText);
    if (!Number.isInteger(hour) || !Number.isInteger(minute)) return;
    library.setReminder(preferences.notificationsEnabled, hour, minute);
  }

  return (
    <div className="page">
      <header className="page-header">
        <p className="kicker">On this device</p>
        <h1>Profile</h1>
        <p className="lede">Progress, the daily word, and how the library is shown. Nothing here leaves the browser.</p>
      </header>

      <section className="panel">
        <h2>Your progress</h2>
        {openedToday ? <p className="review-badge">Today&apos;s word is opened</p> : null}
        <dl className="stat-grid">
          <div>
            <dt>Day streak</dt>
            <dd>{snapshot.dailyStreak}</dd>
          </div>
          <div>
            <dt>Opens</dt>
            <dd>{snapshot.uniqueWordsOpened}</dd>
          </div>
          <div>
            <dt>Saved</dt>
            <dd>{snapshot.savedCount}</dd>
          </div>
          <div>
            <dt>Quizzes</dt>
            <dd>{snapshot.quizAttemptCount}</dd>
          </div>
          <div>
            <dt>Mastered</dt>
            <dd>{snapshot.masteredCount}</dd>
          </div>
          <div>
            <dt>Due for review</dt>
            <dd>{snapshot.dueCount}</dd>
          </div>
        </dl>
        <p className="meta">Best day streak: {snapshot.bestDailyStreak}</p>
        <p>
          <Link className="text-link" to="/quest">
            Continue the quest
          </Link>
        </p>
      </section>

      <section className="panel">
        <h2>Appearance</h2>
        <p className="meta">Theme</p>
        <div className="chip-row" role="group" aria-label="Theme">
          {THEMES.map((theme) => (
            <button
              key={theme.id}
              type="button"
              className={preferences.themeMode === theme.id ? 'chip is-on' : 'chip'}
              aria-pressed={preferences.themeMode === theme.id}
              onClick={() => library.setThemeMode(theme.id)}
            >
              {theme.label}
            </button>
          ))}
        </div>
      </section>

      <section className="panel">
        <h2>Word of the Day</h2>
        <SwitchRow
          label="Rotate through all orators"
          hint="Cycle the daily word through all visible orators, or only your starred favorites."
          checked={preferences.rotateThroughAll}
          onChange={library.setRotateThroughAll}
        />
        {wordOfDay ? (
          <p className="meta">
            Today: <Link to={`/words/${wordOfDay.id}`}>{wordOfDay.word}</Link>
            {oratorById.get(wordOfDay.oratorId) ? ` · ${oratorById.get(wordOfDay.oratorId)?.oratorName}` : ''}
          </p>
        ) : null}
        <label className="field" htmlFor="orator-filter">
          <span>Find an orator</span>
          <input
            id="orator-filter"
            type="search"
            value={query}
            placeholder="Name"
            onChange={(event) => setQuery(event.target.value)}
          />
        </label>
        <ul className="orator-picks">
          {orators.map((orator) => {
            const favorite = preferences.favoriteOratorIds.includes(orator.id);
            const daily = preferences.selectedOratorId === orator.id && !preferences.rotateThroughAll;
            return (
              <li key={orator.id}>
                <div>
                  <p className="switch-label">{orator.name}</p>
                  <p className="meta">{orator.era}</p>
                </div>
                <div className="action-row">
                  <button
                    type="button"
                    className={favorite ? 'chip is-on' : 'chip'}
                    aria-pressed={favorite}
                    aria-label={favorite ? `Remove ${orator.name} from favorites` : `Star ${orator.name} for rotation`}
                    onClick={() => library.toggleFavoriteOrator(orator.id)}
                  >
                    {favorite ? 'Starred' : 'Star'}
                  </button>
                  <button
                    type="button"
                    className={daily ? 'chip is-on' : 'chip'}
                    aria-pressed={daily}
                    onClick={() => library.chooseDailyOrator(daily ? null : orator.id)}
                  >
                    {daily ? 'Daily word' : 'Use daily'}
                  </button>
                </div>
              </li>
            );
          })}
        </ul>
      </section>

      <section className="panel">
        <h2>Catalog</h2>
        <p className="meta">Historical orators stay on. Literary and fictional voices are optional.</p>
        <SwitchRow label="Literary voices" checked={preferences.includeLiterary} onChange={library.setIncludeLiterary} />
        <SwitchRow label="Fictional voices" checked={preferences.includeFictional} onChange={library.setIncludeFictional} />
        <p className="meta">
          {library.visibleOrators.length} orators in the library.{' '}
          <Link to="/orators">Browse them</Link>
        </p>
      </section>

      <section className="panel">
        <h2>Daily reminder</h2>
        <p className="meta">
          A Word of the Day reminder at the time you choose, sent while Rhetorica is open or installed. A closed
          browser tab cannot be woken, and this is not a phone notification or a home-screen widget. If you open the
          app later the same day and the reminder has not been sent, it is sent then.
        </p>
        <SwitchRow
          label="Daily reminder"
          checked={preferences.notificationsEnabled}
          onChange={(checked) => {
            void onReminder(checked);
          }}
        />
        <label className="field" htmlFor="reminder-time">
          <span>Local time</span>
          <input
            id="reminder-time"
            type="time"
            value={clockValue(preferences.notificationHour, preferences.notificationMinute)}
            onChange={(event) => onTime(event.target.value)}
          />
        </label>
        {reminderNote ? <p className="meta">{reminderNote}</p> : null}
      </section>
    </div>
  );
}
