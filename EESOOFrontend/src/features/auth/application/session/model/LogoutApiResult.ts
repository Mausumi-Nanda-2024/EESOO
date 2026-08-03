export type LogoutApiResult =
  | {
      ok: true;
    }
  | {
      ok: false;
      reason: 'UNAUTHORIZED' | 'TEMPORARY_FAILURE';
      message: string;
    };
