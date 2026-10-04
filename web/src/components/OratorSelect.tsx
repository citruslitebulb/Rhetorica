import { oratorsByCategory } from '../data/catalog';

type OratorSelectProps = {
  id: string;
  label: string;
  value: number | null;
  onChange: (id: number | null) => void;
  emptyLabel?: string;
};

export function OratorSelect({ id, label, value, onChange, emptyLabel }: OratorSelectProps) {
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
        {oratorsByCategory.map(([category, orators]) => (
          <optgroup key={category} label={category}>
            {orators.map((orator) => (
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
