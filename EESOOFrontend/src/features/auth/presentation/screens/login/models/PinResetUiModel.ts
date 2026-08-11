import { AuthOperationFailure } from '../../../../application/common/model/AuthOperationResult';

export type PinResetUiState =
  | 'LOGIN'
  | 'FIRST_FAILURE_WARNING'
  | 'RESET_AVAILABLE'
  | 'CONFIRMING_MOBILE'
  | 'MOBILE_CONFIRMED'
  | 'ISSUING_PIN'
  | 'PIN_ISSUED';

export interface PinResetLoginTransition {
  state:
    | 'FIRST_FAILURE_WARNING'
    | 'RESET_AVAILABLE'
    | 'MOBILE_CONFIRMED'
    | 'PIN_ISSUED';
  pinResetAttemptId?: string;
}

export function resolvePinResetLoginTransition(
  error: AuthOperationFailure,
): PinResetLoginTransition | null {
  if (
    error.code === 'PIN_INCORRECT' &&
    error.resetStatus === 'FIRST_FAILURE'
  ) {
    return { state: 'FIRST_FAILURE_WARNING' };
  }

  if (
    error.code === 'PIN_RESET_AVAILABLE' ||
    error.resetStatus === 'RESET_AVAILABLE'
  ) {
    return error.pinResetAttemptId
      ? {
          state: 'RESET_AVAILABLE',
          pinResetAttemptId: error.pinResetAttemptId,
        }
      : null;
  }

  if (
    error.code === 'PIN_RESET_IN_PROGRESS' ||
    error.resetStatus === 'MOBILE_CONFIRMED'
  ) {
    return error.pinResetAttemptId
      ? {
          state: 'MOBILE_CONFIRMED',
          pinResetAttemptId: error.pinResetAttemptId,
        }
      : null;
  }

  if (
    error.code === 'PIN_ALREADY_ISSUED' ||
    error.resetStatus === 'PIN_ISSUED'
  ) {
    return error.pinResetAttemptId
      ? {
          state: 'PIN_ISSUED',
          pinResetAttemptId: error.pinResetAttemptId,
        }
      : null;
  }

  return null;
}

export function isProtectedPinResetState(
  state: PinResetUiState,
): boolean {
  return ![
    'LOGIN',
    'FIRST_FAILURE_WARNING',
  ].includes(state);
}
