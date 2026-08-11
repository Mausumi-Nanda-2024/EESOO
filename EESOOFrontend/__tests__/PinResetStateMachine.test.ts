import {
  isProtectedPinResetState,
  resolvePinResetLoginTransition,
} from '../src/features/auth/presentation/screens/login/models/PinResetUiModel';

test('first incorrect PIN only shows a warning', () => {
  expect(
    resolvePinResetLoginTransition({
      reason: 'BACKEND_REJECTION',
      message: 'Incorrect PIN. One attempt remaining.',
      httpStatus: 401,
      code: 'PIN_INCORRECT',
      resetStatus: 'FIRST_FAILURE',
      remainingAttempts: 1,
    }),
  ).toEqual({ state: 'FIRST_FAILURE_WARNING' });
});

test.each([
  [
    'PIN_RESET_AVAILABLE',
    'RESET_AVAILABLE',
    'RESET_AVAILABLE',
  ],
  [
    'PIN_RESET_IN_PROGRESS',
    'MOBILE_CONFIRMED',
    'MOBILE_CONFIRMED',
  ],
  ['PIN_ALREADY_ISSUED', 'PIN_ISSUED', 'PIN_ISSUED'],
] as const)(
  '%s restores the correct recovery screen',
  (code, resetStatus, expectedState) => {
    expect(
      resolvePinResetLoginTransition({
        reason: 'BACKEND_REJECTION',
        message: 'Recovery response',
        httpStatus: 423,
        code,
        resetStatus,
        remainingAttempts: 0,
        pinResetAttemptId: 'attempt-1',
      }),
    ).toEqual({
      state: expectedState,
      pinResetAttemptId: 'attempt-1',
    });
  },
);

test('recovery state is rejected when backend omits its attempt ID', () => {
  expect(
    resolvePinResetLoginTransition({
      reason: 'BACKEND_REJECTION',
      message: 'PIN reset is available.',
      code: 'PIN_RESET_AVAILABLE',
      resetStatus: 'RESET_AVAILABLE',
    }),
  ).toBeNull();
});

test('only active recovery screens protect navigation', () => {
  expect(isProtectedPinResetState('LOGIN')).toBe(false);
  expect(
    isProtectedPinResetState('FIRST_FAILURE_WARNING'),
  ).toBe(false);
  expect(isProtectedPinResetState('RESET_AVAILABLE')).toBe(
    true,
  );
  expect(isProtectedPinResetState('PIN_ISSUED')).toBe(true);
});
