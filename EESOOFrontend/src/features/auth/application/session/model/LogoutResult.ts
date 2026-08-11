export type LogoutResult =
  | {
      status: 'LOGGED_OUT';
    }
  | {
      status: 'RETRY_REQUIRED';
      message: string;
    };
