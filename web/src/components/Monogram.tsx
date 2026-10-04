import { monogram } from '../lib/format';

export function Monogram({ name }: { name: string }) {
  return (
    <span className="monogram" aria-hidden="true">
      {monogram(name)}
    </span>
  );
}
