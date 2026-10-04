import { useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { ColumnMark } from './Icons';
import { InstallControl } from './InstallControl';
import { SetupFlow } from './SetupFlow';
import { SiteNav } from './SiteNav';

export function AppShell() {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);

  return (
    <div className="shell">
      <SiteNav variant="side" />
      <div className="workspace">
        <a className="skip-link" href="#main">
          Skip to content
        </a>
        <header className="topbar">
          <div className="mobile-brand">
            <ColumnMark className="brand-mark" />
            <span>Rhetorica</span>
          </div>
          <InstallControl />
        </header>
        <main id="main">
          <Outlet />
        </main>
      </div>
      <SiteNav variant="bottom" />
      <SetupFlow />
    </div>
  );
}
