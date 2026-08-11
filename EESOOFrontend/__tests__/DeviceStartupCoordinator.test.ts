import { DeviceStartupCoordinator } from '../src/app/startup/device/DeviceStartupCoordinator';
import { CheckDeviceLinkUseCase } from '../src/features/shared/device/application/CheckDeviceLinkUseCase';
import { StoreDeviceUseCase } from '../src/features/shared/device/application/StoreDeviceUsecase';

function createCoordinator() {
  const storeDeviceUseCase = {
    storeDevice: jest.fn(),
    forceStoreDevice: jest.fn(),
  };
  const checkDeviceLinkUseCase = {
    execute: jest.fn(),
  };
  const coordinator = new DeviceStartupCoordinator(
    storeDeviceUseCase as unknown as StoreDeviceUseCase,
    checkDeviceLinkUseCase as unknown as CheckDeviceLinkUseCase,
  );

  return {
    coordinator,
    storeDeviceUseCase,
    checkDeviceLinkUseCase,
  };
}

test('returns not ready when normal device storage fails', async () => {
  const setup = createCoordinator();

  setup.storeDeviceUseCase.storeDevice.mockResolvedValue(false);

  await expect(setup.coordinator.execute()).resolves.toEqual({
    status: 'NOT_READY',
    message:
      'Unable to register this device right now. Please try again.',
  });
  expect(
    setup.checkDeviceLinkUseCase.execute,
  ).not.toHaveBeenCalled();
});

test('repairs a device missing from the backend and checks again', async () => {
  const setup = createCoordinator();

  setup.storeDeviceUseCase.storeDevice.mockResolvedValue(true);
  setup.storeDeviceUseCase.forceStoreDevice.mockResolvedValue(true);
  setup.checkDeviceLinkUseCase.execute
    .mockResolvedValueOnce({
      linked: false,
      reason: 'DEVICE_NOT_REGISTERED',
    })
    .mockResolvedValueOnce({
      linked: true,
      userId: 'user-1',
      username: 'known-user',
      phoneNumber: '9876543210',
    });

  await expect(setup.coordinator.execute()).resolves.toEqual({
    status: 'LINKED',
    identity: {
      userId: 'user-1',
      username: 'known-user',
      phoneNumber: '9876543210',
    },
  });
  expect(
    setup.storeDeviceUseCase.forceStoreDevice,
  ).toHaveBeenCalledTimes(1);
  expect(
    setup.checkDeviceLinkUseCase.execute,
  ).toHaveBeenCalledTimes(2);
});

test('keeps a stored but unlinked device ready for authentication', async () => {
  const setup = createCoordinator();

  setup.storeDeviceUseCase.storeDevice.mockResolvedValue(true);
  setup.checkDeviceLinkUseCase.execute.mockResolvedValue({
    linked: false,
    reason: 'DEVICE_NOT_LINKED',
  });

  await expect(setup.coordinator.execute()).resolves.toEqual({
    status: 'READY_NOT_LINKED',
    reason: 'DEVICE_NOT_LINKED',
  });
  expect(
    setup.storeDeviceUseCase.forceStoreDevice,
  ).not.toHaveBeenCalled();
});
