import { useEffect, useState } from 'react';

type BeforeInstallPromptEvent = Event & {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
};

function isStandalone(): boolean {
  const iosStandalone = 'standalone' in navigator && Boolean((navigator as Navigator & { standalone?: boolean }).standalone);
  return iosStandalone || window.matchMedia('(display-mode: standalone)').matches;
}

function isIos(): boolean {
  return /iphone|ipad|ipod/i.test(navigator.userAgent);
}

export function InstallControl() {
  const [promptEvent, setPromptEvent] = useState<BeforeInstallPromptEvent | null>(null);
  const [installed, setInstalled] = useState(isStandalone);
  const [iosHelp, setIosHelp] = useState(false);
  const showIos = !installed && isIos();

  useEffect(() => {
    const onPrompt = (event: Event) => {
      event.preventDefault();
      setPromptEvent(event as BeforeInstallPromptEvent);
    };
    const onInstalled = () => {
      setInstalled(true);
      setPromptEvent(null);
    };
    window.addEventListener('beforeinstallprompt', onPrompt);
    window.addEventListener('appinstalled', onInstalled);
    return () => {
      window.removeEventListener('beforeinstallprompt', onPrompt);
      window.removeEventListener('appinstalled', onInstalled);
    };
  }, []);

  if (installed) return null;

  async function install() {
    if (!promptEvent) return;
    await promptEvent.prompt();
    const choice = await promptEvent.userChoice;
    if (choice.outcome === 'accepted') setInstalled(true);
    setPromptEvent(null);
  }

  return (
    <div className="install-control">
      {promptEvent ? (
        <button type="button" className="button primary" onClick={() => void install()}>
          Install
        </button>
      ) : null}
      {showIos ? (
        <button type="button" className="button" onClick={() => setIosHelp(true)}>
          Add to Home Screen
        </button>
      ) : null}
      {iosHelp ? (
        <div className="install-note" role="dialog" aria-labelledby="install-title">
          <h2 id="install-title">Add Rhetorica to your home screen</h2>
          <p>In Safari, tap Share, then Add to Home Screen. The library opens like an app and stays available offline after the first visit.</p>
          <button type="button" className="button" onClick={() => setIosHelp(false)}>
            Close
          </button>
        </div>
      ) : null}
    </div>
  );
}
