/**
 * @format
 */

import React from 'react';
import ReactTestRenderer from 'react-test-renderer';
import App from '../App';

jest.mock('../src/app/routing/useStartupRoute', () => ({
  useStartupRoute: () => ({
    route: {
      name: 'BOOTSTRAPPING',
      message: 'Preparing application...',
    },
    retryStartup: jest.fn(),
  }),
}));

jest.mock(
  '../src/features/auth/infrastructure/config/AuthInfrastructureConfig',
  () => ({
    authenticatedHttpClient: {
      setSessionInvalidatedHandler: jest.fn(),
    },
    loginUserUseCase: {},
    logoutUserUseCase: {
      execute: jest.fn(),
    },
  }),
);

jest.mock(
  '../src/features/shared/device/infrastructure/config/DeviceInfrastructureConfig',
  () => ({
    checkDeviceLinkUseCase: {
      execute: jest.fn(),
    },
    deviceRepository: {},
  }),
);

jest.mock('../src/app/startup/AppStartupConfig', () => ({
  deviceStartupCoordinator: {
    execute: jest.fn(),
  },
}));

test('renders correctly', async () => {
  await ReactTestRenderer.act(() => {
    ReactTestRenderer.create(<App />);
  });
});
