import React, { useEffect } from 'react';
import ReactTestRenderer from 'react-test-renderer';
import { appStartupCoordinator } from '../src/app/startup/AppStartupConfig';
import { AppRoute } from '../src/app/routing/AppRoute';
import { useStartupRoute } from '../src/app/routing/useStartupRoute';

jest.mock('../src/app/startup/AppStartupConfig', () => ({
  appStartupCoordinator: {
    execute: jest.fn(),
  },
}));

const mockedAppStartupCoordinator =
  appStartupCoordinator as jest.Mocked<
    typeof appStartupCoordinator
  >;

interface StartupRouteSnapshot {
  route: AppRoute;
  retryStartup: () => void;
}

function StartupRouteProbe({
  onSnapshot,
}: {
  onSnapshot: (snapshot: StartupRouteSnapshot) => void;
}) {
  const startupRoute = useStartupRoute();

  useEffect(() => {
    onSnapshot(startupRoute);
  }, [onSnapshot, startupRoute]);

  return null;
}

async function flushUpdates() {
  await Promise.resolve();
  await Promise.resolve();
}

beforeEach(() => {
  jest.clearAllMocks();
});

test('returns the route selected by the startup coordinator', async () => {
  const snapshots: StartupRouteSnapshot[] = [];
  const selectedRoute: AppRoute = {
    name: 'AUTH_ENTRY',
  };

  mockedAppStartupCoordinator.execute.mockResolvedValue(
    selectedRoute,
  );

  await ReactTestRenderer.act(async () => {
    ReactTestRenderer.create(
      <StartupRouteProbe
        onSnapshot={snapshot => snapshots.push(snapshot)}
      />,
    );
    await flushUpdates();
  });

  expect(snapshots[0].route).toEqual({
    name: 'BOOTSTRAPPING',
    message: 'Preparing application...',
  });
  expect(snapshots.at(-1)?.route).toEqual(selectedRoute);
});

test('runs startup again when retryStartup is called', async () => {
  const snapshots: StartupRouteSnapshot[] = [];

  mockedAppStartupCoordinator.execute
    .mockResolvedValueOnce({
      name: 'SESSION_RESTORE_ERROR',
      message: 'Network unavailable.',
    })
    .mockResolvedValueOnce({
      name: 'AUTH_ENTRY',
    });

  await ReactTestRenderer.act(async () => {
    ReactTestRenderer.create(
      <StartupRouteProbe
        onSnapshot={snapshot => snapshots.push(snapshot)}
      />,
    );
    await flushUpdates();
  });

  await ReactTestRenderer.act(async () => {
    snapshots.at(-1)?.retryStartup();
    await flushUpdates();
  });

  expect(mockedAppStartupCoordinator.execute).toHaveBeenCalledTimes(
    2,
  );
  expect(snapshots.at(-1)?.route).toEqual({
    name: 'AUTH_ENTRY',
  });
});
