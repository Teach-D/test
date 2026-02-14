import { type ReactElement } from 'react';

export function LoadingSpinner(): ReactElement {
  return (
    <div className="flex items-center justify-center">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
    </div>
  );
}
