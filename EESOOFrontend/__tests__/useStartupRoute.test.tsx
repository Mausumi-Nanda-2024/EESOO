import React, { useEffect } from 'react';
import ReactTestRenderer from 'react-test-renderer';
import { useStartupRoute } from '../src/app/routing/useStartupRoute';
import { AppRoute } from '../src/app/routing/AppRoute';
import {
  checkDeviceLinkUseCase,
  storeDeviceUseCase,
} from '../src/features/shared/device/infrastructure/config/DeviceInfrastructureConfig';

jest.mock('../src/features/shared/device/infrastructure/config/DeviceInfrastructureConfig', () => ({
  storeDeviceUseCase: {
    storeDevice: jest.fn(),
    forceStoreDevice: jest.fn(),
  },
  checkDeviceLinkUseCase: {
    execute: jest.fn(),
  },
}));

const mockedStoreDeviceUseCase = storeDeviceUseCase as jest.Mocked<
  typeof storeDeviceUseCase
>;
const mockedCheckDeviceLinkUseCase = checkDeviceLinkUseCase as jest.Mocked<
  typeof checkDeviceLinkUseCase
>;

function StartupRouteProbe({ onRoute }: { onRoute: (route: AppRoute) => void }) {
  const route = useStartupRoute();

  useEffect(() => {
    onRoute(route);
  }, [onRoute, route]);

  return null;
}

async function flushStartupRouteUpdates() {
  await Promise.resolve();
  await Promise.resolve();
  await Promise.resolve();
  await Promise.resolve();
}

async function renderStartupRoute() {
  const routes: AppRoute[] = [];

  await ReactTestRenderer.act(async () => {
    ReactTestRenderer.create(
      <StartupRouteProbe onRoute={route => routes.push(route)} />,
    );
    await flushStartupRouteUpdates();
  });

  return routes;
}

function createDeferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>(promiseResolve => {
    resolve = promiseResolve;
  });

  return { promise, resolve };
}

beforeEach(() => {
  jest.clearAllMocks();
});

test('fresh device with no linked user routes to register', async () => {
  mockedStoreDeviceUseCase.storeDevice.mockResolvedValue(true);
  mockedCheckDeviceLinkUseCase.execute.mockResolvedValue({
    linked: false,
    reason: 'NOT_LINKED',
  });

  const routes = await renderStartupRoute();

  expect(routes.at(-1)).toEqual({
    name: 'REGISTER',
    deviceReady: true,
    deviceMessage: '',
  });
});

test('linked device routes to login with known identity', async () => {
  mockedStoreDeviceUseCase.storeDevice.mockResolvedValue(true);
  mockedCheckDeviceLinkUseCase.execute.mockResolvedValue({
    linked: true,
    userId: 'user-1',
    username: 'testuser',
    phoneNumber: '9876543210',
  });

  const routes = await renderStartupRoute();

  expect(routes.at(-1)).toEqual({
    name: 'LOGIN',
    mode: 'KNOWN_IDENTITY',
    knownIdentity: {
      userId: 'user-1',
      username: 'testuser',
      phoneNumber: '9876543210',
    },
  });
});

test('device not found restores device then checks link again', async () => {
  const forceStoreResult = createDeferred<boolean>();
  const routes: AppRoute[] = [];

  mockedStoreDeviceUseCase.storeDevice.mockResolvedValue(true);
  mockedStoreDeviceUseCase.forceStoreDevice.mockReturnValue(forceStoreResult.promise);
  mockedCheckDeviceLinkUseCase.execute
    .mockResolvedValueOnce({
      linked: false,
      reason: 'DEVICE_NOT_FOUND',
    })
    .mockResolvedValueOnce({
      linked: false,
      reason: 'NOT_LINKED',
    });

  await ReactTestRenderer.act(async () => {
    ReactTestRenderer.create(
      <StartupRouteProbe onRoute={route => routes.push(route)} />,
    );
    await flushStartupRouteUpdates();
  });

  expect(routes).toContainEqual({
    name: 'BOOTSTRAPPING',
    message: 'Restoring device connection...',
  });
  expect(mockedStoreDeviceUseCase.forceStoreDevice).toHaveBeenCalledTimes(1);

  await ReactTestRenderer.act(async () => {
    forceStoreResult.resolve(true);
    await flushStartupRouteUpdates();
  });

  expect(mockedCheckDeviceLinkUseCase.execute).toHaveBeenCalledTimes(2);
  expect(routes.at(-1)).toEqual({
    name: 'REGISTER',
    deviceReady: true,
    deviceMessage: '',
  });
});

test('missing device id routes to register without force storing', async () => {
  mockedStoreDeviceUseCase.storeDevice.mockResolvedValue(true);
  mockedCheckDeviceLinkUseCase.execute.mockResolvedValue({
    linked: false,
    reason: 'DEVICE_ID_MISSING',
  });

  const routes = await renderStartupRoute();

  expect(mockedStoreDeviceUseCase.forceStoreDevice).not.toHaveBeenCalled();
  expect(routes.at(-1)).toEqual({
    name: 'REGISTER',
    deviceReady: true,
    deviceMessage: '',
  });
});
