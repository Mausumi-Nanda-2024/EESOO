import axiosInstance from '../src/config/axiosInstance';
import { PinResetApiServiceImpl } from '../src/features/auth/infrastructure/pinReset/api/PinResetApiServiceImpl';

jest.mock('../src/config/axiosInstance', () => ({
  __esModule: true,
  default: {
    post: jest.fn(),
  },
}));

const mockedPost = axiosInstance.post as jest.Mock;

beforeEach(() => {
  mockedPost.mockReset();
});

test('confirm-mobile calls the public reset endpoint with the full contract', async () => {
  mockedPost.mockResolvedValue({
    data: {
      status: 'success',
      message: 'Mobile number confirmed successfully',
      data: {
        pinResetAttemptId: 'attempt-1',
        status: 'MOBILE_CONFIRMED',
        mobileConfirmedAt: '2026-08-10T06:00:00Z',
      },
    },
  });
  const service = new PinResetApiServiceImpl();

  await expect(
    service.confirmMobile({
      pinResetAttemptId: 'attempt-1',
      phoneNumber: '9876543210',
      installId: 'install-1',
    }),
  ).resolves.toEqual({
    ok: true,
    value: {
      pinResetAttemptId: 'attempt-1',
      status: 'MOBILE_CONFIRMED',
      mobileConfirmedAt: '2026-08-10T06:00:00Z',
    },
  });
  expect(mockedPost).toHaveBeenCalledWith(
    '/auth/pin-reset/confirm-mobile',
    {
      pinResetAttemptId: 'attempt-1',
      phoneNumber: '9876543210',
      installId: 'install-1',
    },
  );
});

test('issue calls the public reset endpoint without phone, user, or PIN fields', async () => {
  mockedPost.mockResolvedValue({
    data: {
      status: 'success',
      message: 'PIN reset successfully',
      data: {
        pinResetAttemptId: 'attempt-1',
        status: 'PIN_ISSUED',
        newPin: '0042',
        pinIssuedAt: '2026-08-10T06:01:00Z',
      },
    },
  });
  const service = new PinResetApiServiceImpl();

  const result = await service.issuePin({
    pinResetAttemptId: 'attempt-1',
    installId: 'install-1',
  });

  expect(result).toEqual({
    ok: true,
    value: {
      pinResetAttemptId: 'attempt-1',
      status: 'PIN_ISSUED',
      newPin: '0042',
      pinIssuedAt: '2026-08-10T06:01:00Z',
    },
  });
  expect(mockedPost).toHaveBeenCalledWith(
    '/auth/pin-reset/issue',
    {
      pinResetAttemptId: 'attempt-1',
      installId: 'install-1',
    },
  );
  expect(mockedPost.mock.calls[0][1]).not.toHaveProperty(
    'phoneNumber',
  );
  expect(mockedPost.mock.calls[0][1]).not.toHaveProperty(
    'newPin',
  );
});

test('login-style 423 metadata remains available to the state machine', async () => {
  mockedPost.mockRejectedValue({
    isAxiosError: true,
    response: {
      status: 423,
      data: {
        status: 'error',
        message: 'PIN reset is already in progress.',
        data: {
          code: 'PIN_RESET_IN_PROGRESS',
          resetStatus: 'MOBILE_CONFIRMED',
          remainingAttempts: 0,
          pinResetAttemptId: 'attempt-1',
        },
      },
    },
  });
  const service = new PinResetApiServiceImpl();

  await expect(
    service.issuePin({
      pinResetAttemptId: 'attempt-1',
      installId: 'install-1',
    }),
  ).resolves.toEqual({
    ok: false,
    error: {
      reason: 'BACKEND_REJECTION',
      message: 'PIN reset is already in progress.',
      httpStatus: 423,
      code: 'PIN_RESET_IN_PROGRESS',
      resetStatus: 'MOBILE_CONFIRMED',
      remainingAttempts: 0,
      pinResetAttemptId: 'attempt-1',
      fieldErrors: undefined,
    },
  });
});
