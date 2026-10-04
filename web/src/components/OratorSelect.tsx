import { useMemo } from 'react';
import type { Dictionary } from '../types';

type OratorSelectProps = {
  id: string;
  label: string;
  value: number | null;
  orators: readonly Dictionary[];
  onChange: (id: number | null) => void;
  emptyLabel?: string;
};

export function OratorSelect({ id, label, value, orators, onChange, emptyLabel }: OratorSelectProps) {
  const groups = useMemo(() => {
    const grouped = new Map<string, Dictionary[]>();
    for (const orator of orators) {
      const list = grouped.get(orator.category);
      if (list) list.push(orator);
      else grouped.set(orator.category, [orator]);
    }
    return [...grouped.entries()];
  }, [orators]);

  return (
    <label className="field" htmlFor={id}>
      <span>{label}</span>
      <select
        id={id}
        value={value ?? ''}
        onChange={(event) => {
          const next = event.target.value;
          onChange(next === '' ? null : Number(next));
        }}
      >
        {emptyLabel ? <option value="">{emptyLabel}</option> : null}
        {groups.map(([category, group]) => (
          <optgroup key={category} label={category}>
            {group.map((orator) => (
              <option key={orator.id} value={orator.id}>
                {orator.name}
              </option>
            ))}
          </optgroup>
        ))}
      </select>
    </label>
  );
}
