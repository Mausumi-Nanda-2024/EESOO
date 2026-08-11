export type SessionRestorationResult =
  | {
      status: 'RESTORED';
    }
  | {
      status: 'NO_SESSION';
    }
  | {
      status: 'RETRY_REQUIRED';
      message: string;
    };