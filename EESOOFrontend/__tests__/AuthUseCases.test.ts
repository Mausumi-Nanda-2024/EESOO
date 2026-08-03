import { LoginUserUseCase } from '../src/features/auth/application/login/usecase/LoginUserUseCase';
import { AuthApiService } from '../src/features/auth/application/login/interface/AuthApiService';
import { AuthSessionApiService } from '../src/features/auth/application/session/interface/AuthSessionApiService';
import { AuthTokenStorage } from '../src/features/auth/application/session/interface/AuthTokenStorage';
import { LogoutUserUseCase } from '../src/features/auth/application/session/usecase/LogoutUserUseCase';
import { RestoreAuthSessionUseCase } from '../src/features/auth/application/session/usecase/RestoreAuthSessionUseCase';
import { AuthTokenPair } from '../src/features/auth/domain/model/valueobject/AuthTokenPair';
import { Device } from '../src/features/shared/device/domain/model/entity/device';
import { DeviceRepository } from '../src/features/shared/device/domain/repository/DeviceRepository';

function createTokenPair(
  accessToken = 'access-token',
  refreshToken = 'refresh-token',
): AuthTokenPair {
  const result = AuthTokenPair.create(
    accessToken,
    refreshToken,
  );

  if (!result.ok) {
    throw new Error(result.message);
  }

  return result.value;
}

function createTokenStorage(
  tokenPair: AuthTokenPair | null,
) {
  return {
    saveTokenPair: jest.fn().mockResolvedValue(undefined),
    getTokenPair: jest.fn().mockResolvedValue(tokenPair),
    clearTokenPair: jest.fn().mockResolvedValue(undefined),
  };
}

function createSessionApiService() {
  return {
    refresh: jest.fn(),
    logout: jest.fn(),
  };
}

test('login stores tokens and returns only safe login state', async () => {
  const authApiService = {
    login: jest.fn().mockResolvedValue({
      userId: 'user-1',
      username: 'known-user',
      accessToken: 'new-access-token',
      refreshToken: 'new-refresh-token',
      deviceLinked: true,
      deviceLinkFailureReason: null,
    }),
  };
  const storedDevice = Device.rehydrate(
    'device-1',
    'install-1',
    '15',
    'android',
    null,
    true,
    0,
    false,
  );
  const deviceRepository = {
    getDevice: jest.fn().mockResolvedValue(storedDevice),
  };
  const tokenStorage = createTokenStorage(null);
  const useCase = new LoginUserUseCase(
    authApiService as AuthApiService,
    deviceRepository as unknown as DeviceRepository,
    tokenStorage as AuthTokenStorage,
  );

  const result = await useCase.loginUser({
    phoneNumber: '9876543210',
    pin: '1234',
  });

  expect(authApiService.login).toHaveBeenCalledWith({
    phoneNumber: '9876543210',
    pin: '1234',
    deviceId: 'device-1',
    installId: 'install-1',
  });
  expect(tokenStorage.saveTokenPair).toHaveBeenCalledTimes(1);
  expect(result).toEqual({
    identity: {
      userId: 'user-1',
      username: 'known-user',
    },
    deviceLinked: true,
    deviceLinkFailureReason: null,
  });
  expect(result).not.toHaveProperty('accessToken');
  expect(result).not.toHaveProperty('refreshToken');
});

test('session restoration rotates and stores the token pair', async () => {
  const tokenStorage = createTokenStorage(createTokenPair());
  const sessionApiService = createSessionApiService();

  sessionApiService.refresh.mockResolvedValue({
    ok: true,
    value: {
      accessToken: 'rotated-access-token',
      refreshToken: 'rotated-refresh-token',
    },
  });

  const useCase = new RestoreAuthSessionUseCase(
    tokenStorage as AuthTokenStorage,
    sessionApiService as AuthSessionApiService,
  );

  await expect(useCase.execute()).resolves.toEqual({
    status: 'RESTORED',
  });
  expect(sessionApiService.refresh).toHaveBeenCalledWith({
    refreshToken: 'refresh-token',
  });
  expect(tokenStorage.saveTokenPair).toHaveBeenCalledTimes(1);
});

test('invalid restored session clears local tokens', async () => {
  const tokenStorage = createTokenStorage(createTokenPair());
  const sessionApiService = createSessionApiService();

  sessionApiService.refresh.mockResolvedValue({
    ok: false,
    reason: 'INVALID_SESSION',
    message: 'Session expired.',
  });

  const useCase = new RestoreAuthSessionUseCase(
    tokenStorage as AuthTokenStorage,
    sessionApiService as AuthSessionApiService,
  );

  await expect(useCase.execute()).resolves.toEqual({
    status: 'NO_SESSION',
  });
  expect(tokenStorage.clearTokenPair).toHaveBeenCalledTimes(1);
});

test('logout refreshes an expired access token and retries once', async () => {
  const tokenStorage = createTokenStorage(createTokenPair());
  const sessionApiService = createSessionApiService();

  sessionApiService.logout
    .mockResolvedValueOnce({
      ok: false,
      reason: 'UNAUTHORIZED',
      message: 'Access token expired.',
    })
    .mockResolvedValueOnce({ ok: true });
  sessionApiService.refresh.mockResolvedValue({
    ok: true,
    value: {
      accessToken: 'rotated-access-token',
      refreshToken: 'rotated-refresh-token',
    },
  });

  const useCase = new LogoutUserUseCase(
    tokenStorage as AuthTokenStorage,
    sessionApiService as AuthSessionApiService,
  );

  await expect(useCase.execute()).resolves.toEqual({
    status: 'LOGGED_OUT',
  });
  expect(sessionApiService.logout).toHaveBeenNthCalledWith(1, {
    accessToken: 'access-token',
  });
  expect(sessionApiService.logout).toHaveBeenNthCalledWith(2, {
    accessToken: 'rotated-access-token',
  });
  expect(tokenStorage.clearTokenPair).toHaveBeenCalledTimes(1);
});

test('temporary logout failure keeps the local session', async () => {
  const tokenStorage = createTokenStorage(createTokenPair());
  const sessionApiService = createSessionApiService();

  sessionApiService.logout.mockResolvedValue({
    ok: false,
    reason: 'TEMPORARY_FAILURE',
    message: 'Internet connection required.',
  });

  const useCase = new LogoutUserUseCase(
    tokenStorage as AuthTokenStorage,
    sessionApiService as AuthSessionApiService,
  );

  await expect(useCase.execute()).resolves.toEqual({
    status: 'RETRY_REQUIRED',
    message: 'Internet connection required.',
  });
  expect(tokenStorage.clearTokenPair).not.toHaveBeenCalled();
});
