import { useEffect } from 'react';

export function useDocumentTitle(title: string) {
  useEffect(() => {
    document.title = title === 'Rhetorica' ? title : `${title} · Rhetorica`;
  }, [title]);
}
