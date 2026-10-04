type SwitchRowProps = {
  label: string;
  hint?: string;
  checked: boolean;
  onChange: (checked: boolean) => void;
};

export function SwitchRow({ label, hint, checked, onChange }: SwitchRowProps) {
  return (
    <div className="switch-row">
      <div>
        <p className="switch-label">{label}</p>
        {hint ? <p className="meta">{hint}</p> : null}
      </div>
      <button
        type="button"
        role="switch"
        aria-checked={checked}
        aria-label={label}
        className={checked ? 'switch is-on' : 'switch'}
        onClick={() => onChange(!checked)}
      >
        <span />
      </button>
    </div>
  );
}
