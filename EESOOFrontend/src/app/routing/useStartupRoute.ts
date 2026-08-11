import { useCallback, useEffect, useState } from 'react';
import { appStartupCoordinator } from '../startup/AppStartupConfig';
import { AppRoute } from './AppRoute';

export function useStartupRoute() {
  const [startupAttempt, setStartupAttempt] = useState(0);
  const [route, setRoute] = useState<AppRoute>({
    name: 'BOOTSTRAPPING',
    message: 'Preparing application...',
  });

  useEffect(() => {
    let isMounted = true;

    const decideStartupRoute = async () => {
      setRoute({
        name: 'BOOTSTRAPPING',
        message: 'Preparing application...',
      });

      const startupRoute =
        await appStartupCoordinator.execute();

      if (!isMounted) {
        return;
      }

      setRoute(startupRoute);
    };

    decideStartupRoute();

    return () => {
      isMounted = false;
    };
  }, [startupAttempt]);

  const retryStartup = useCallback(() => {
    setStartupAttempt(previousAttempt =>
      previousAttempt + 1,
    );
  }, []);

  return {
    route,
    retryStartup,
  };
}
