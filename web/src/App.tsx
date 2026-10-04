import { BrowserRouter, Link, Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './components/AppShell';
import { OratorPage } from './pages/OratorPage';
import { OratorsPage } from './pages/OratorsPage';
import { QuotesPage } from './pages/QuotesPage';
import { SavedPage } from './pages/SavedPage';
import { SpeechPage } from './pages/SpeechPage';
import { WordPage } from './pages/WordPage';
import { WordsPage } from './pages/WordsPage';
import { LibraryProvider } from './state/LibraryProvider';

function NotFound() {
  return (
    <div className="page">
      <h1>Page not found</h1>
      <p>That address is outside the library.</p>
      <Link className="button" to="/orators">
        Orators
      </Link>
    </div>
  );
}

export function App() {
  return (
    <LibraryProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<AppShell />}>
            <Route index element={<Navigate to="/orators" replace />} />
            <Route path="orators" element={<OratorsPage />} />
            <Route path="orators/:oratorId" element={<OratorPage />} />
            <Route path="words" element={<WordsPage />} />
            <Route path="words/:wordId" element={<WordPage />} />
            <Route path="quotes" element={<QuotesPage />} />
            <Route path="saved" element={<SavedPage />} />
            <Route path="speeches/:speechId" element={<SpeechPage />} />
            <Route path="*" element={<NotFound />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </LibraryProvider>
  );
}
