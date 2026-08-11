export type PinResetStatus =
  | 'FIRST_FAILURE'
  | 'RESET_AVAILABLE'
  | 'MOBILE_CONFIRMED'
  | 'PIN_ISSUED';

export type AuthOperationFailureReason =
  | 'VALIDATION_FAILURE'
  | 'DEVICE_NOT_READY'
  | 'BACKEND_REJECTION'
  | 'TEMPORARY_FAILURE';

export interface AuthOperationFailure {
  reason: AuthOperationFailureReason;
  message: string;
  httpStatus?: number;
  code?: string;
  resetStatus?: PinResetStatus;
  remainingAttempts?: number;
  pinResetAttemptId?: string;
  fieldErrors?: Record<string, string>;
}

export type AuthOperationResult<T> =
  | {
      ok: true;
      value: T;
    }
  | {
      ok: false;
      error: AuthOperationFailure;
    };
