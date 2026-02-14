import { type ReactElement } from 'react';

interface ErrorMessageProps {
  message: string;
}

export function ErrorMessage({ message }: ErrorMessageProps): ReactElement {
  return (
    <div className="rounded-lg bg-red-50 p-4 text-sm text-red-800">
      {message}
    </div>
  );
}
