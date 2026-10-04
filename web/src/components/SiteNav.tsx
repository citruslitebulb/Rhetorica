import { NavLink } from 'react-router-dom';
import { oratorById } from '../data/catalog';
import { useLibrary } from '../state/LibraryProvider';
import { ColumnMark, IconOrators, IconProfile, IconQuest, IconQuotes, IconSaved, IconSpeeches, IconWords } from './Icons';

const LINKS = [
  { to: '/orators', label: 'Orators', icon: IconOrators },
  { to: '/words', label: 'Words', icon: IconWords },
  { to: '/quotes', label: 'Quotes', icon: IconQuotes },
  { to: '/speeches', label: 'Speeches', icon: IconSpeeches },
  { to: '/quest', label: 'Quest', icon: IconQuest },
  { to: '/saved', label: 'Saved', icon: IconSaved },
  { to: '/profile', label: 'Profile', icon: IconProfile },
] as const;

export function SiteNav({ variant }: { variant: 'side' | 'bottom' }) {
  const { selectedOratorId, preferences } = useLibrary();
  const orator = selectedOratorId == null ? undefined : oratorById.get(selectedOratorId);
  const dailyLine = preferences.rotateThroughAll
    ? preferences.favoriteOratorIds.length > 0
      ? 'Daily word rotates through favorites'
      : 'Daily word rotates through all'
    : null;
  return (
    <nav className={variant === 'side' ? 'side-nav' : 'bottom-nav'} aria-label={variant === 'side' ? 'Primary' : 'Sections'}>
      {variant === 'side' ? (
        <div className="brand-lockup">
          <ColumnMark className="brand-mark" />
          <div>
            <p className="brand-name">Rhetorica</p>
            <p className="brand-tag">Daily words that moved empires</p>
          </div>
        </div>
      ) : null}
      <ul>
        {LINKS.map((link) => {
          const Icon = link.icon;
          return (
            <li key={link.to}>
              <NavLink to={link.to}>
                <Icon />
                <span>{link.label}</span>
              </NavLink>
            </li>
          );
        })}
      </ul>
      {variant === 'side' ? (
        <p className="nav-orator">
          {dailyLine ? (
            dailyLine
          ) : orator ? (
            <>
              Reading <strong>{orator.oratorName}</strong>
            </>
          ) : (
            'All orators'
          )}
        </p>
      ) : null}
    </nav>
  );
}
