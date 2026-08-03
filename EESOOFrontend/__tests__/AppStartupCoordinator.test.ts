import { AppStartupCoordinator } from '../src/app/startup/AppStartupCoordinator';
import { DeviceStartupCoordinator } from '../src/app/startup/device/DeviceStartupCoordinator';
import { RestoreAuthSessionUseCase } from '../src/features/auth/application/session/usecase/RestoreAuthSessionUseCase';

function createCoordinator() {
  const deviceStartupCoordinator = {
    execute: jest.fn(),
  };
  const restoreAuthSessionUseCase = {
    execute: jest.fn(),
  };
  const coordinator = new AppStartupCoordinator(
    deviceStartupCoordinator as unknown as DeviceStartupCoordinator,
    restoreAuthSessionUseCase as unknown as RestoreAuthSessionUseCase,
  );

  return {
    coordinator,
    deviceStartupCoordinator,
    restoreAuthSessionUseCase,
  };
}

test('linked device with restored session is authenticated', async () => {
  const setup = createCoordinator();

  setup.deviceStartupCoordinator.execute.mockResolvedValue({
    status: 'LINKED',
    identity: {
      userId: 'user-1',
      username: 'known-user',
      phoneNumber: '9876543210',
    },
  });
  setup.restoreAuthSessionUseCase.execute.mockResolvedValue({
    status: 'RESTORED',
  });

  await expect(setup.coordinator.execute()).resolves.toEqual({
    name: 'AUTHENTICATED',
    identity: {
      userId: 'user-1',
      username: 'known-user',
    },
  });
});

test('linked device without a session uses known-identity login', async () => {
  const setup = createCoordinator();

  setup.deviceStartupCoordinator.execute.mockResolvedValue({
    status: 'LINKED',
    identity: {
      userId: 'user-1',
      username: 'known-user',
      phoneNumber: '9876543210',
    },
  });
  setup.restoreAuthSessionUseCase.execute.mockResolvedValue({
    status: 'NO_SESSION',
  });

  await expect(setup.coordinator.execute()).resolves.toEqual({
    name: 'LOGIN',
    mode: 'KNOWN_IDENTITY',
    knownIdentity: {
      userId: 'user-1',
      username: 'known-user',
      phoneNumber: '9876543210',
    },
  });
});

test('unlinked device with restored session uses recovery', async () => {
  const setup = createCoordinator();

  setup.deviceStartupCoordinator.execute.mockResolvedValue({
    status: 'READY_NOT_LINKED',
    reason: 'DEVICE_NOT_LINKED',
  });
  setup.restoreAuthSessionUseCase.execute.mockResolvedValue({
    status: 'RESTORED',
  });

  await expect(setup.coordinator.execute()).resolves.toEqual({
    name: 'DEVICE_LINK_RECOVERY',
    reason: 'DEVICE_NOT_LINKED',
  });
});

test('unlinked device without a session uses the auth entry', async () => {
  const setup = createCoordinator();

  setup.deviceStartupCoordinator.execute.mockResolvedValue({
    status: 'READY_NOT_LINKED',
    reason: 'DEVICE_NOT_LINKED',
  });
  setup.restoreAuthSessionUseCase.execute.mockResolvedValue({
    status: 'NO_SESSION',
  });

  await expect(setup.coordinator.execute()).resolves.toEqual({
    name: 'AUTH_ENTRY',
  });
});

test('device startup failure does not attempt session restoration', async () => {
  const setup = createCoordinator();

  setup.deviceStartupCoordinator.execute.mockResolvedValue({
    status: 'NOT_READY',
    message: 'Device unavailable.',
  });

  await expect(setup.coordinator.execute()).resolves.toEqual({
    name: 'DEVICE_STARTUP_ERROR',
    message: 'Device unavailable.',
  });
  expect(
    setup.restoreAuthSessionUseCase.execute,
  ).not.toHaveBeenCalled();
});

test('temporary session failure requests a retry', async () => {
  const setup = createCoordinator();

  setup.deviceStartupCoordinator.execute.mockResolvedValue({
    status: 'READY_NOT_LINKED',
    reason: 'DEVICE_NOT_LINKED',
  });
  setup.restoreAuthSessionUseCase.execute.mockResolvedValue({
    status: 'RETRY_REQUIRED',
    message: 'Server temporarily unavailable.',
  });

  await expect(setup.coordinator.execute()).resolves.toEqual({
    name: 'SESSION_RESTORE_ERROR',
    message: 'Server temporarily unavailable.',
  });
});
