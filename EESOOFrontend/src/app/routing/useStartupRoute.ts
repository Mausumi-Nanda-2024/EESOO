import { useEffect, useState } from 'react';
import {
  checkDeviceLinkUseCase,
  storeDeviceUseCase,
} from '../../features/shared/device/infrastructure/config/DeviceInfrastructureConfig';
import { CheckDeviceResponseDTO } from '../../features/shared/device/application/dto/CheckDeviceResponseDTO';
import { AppRoute } from './AppRoute';

function routeFromCheckResult(checkResult: CheckDeviceResponseDTO): AppRoute {
  if (checkResult.linked) {
    return {
      name: 'LOGIN',
      mode: 'KNOWN_IDENTITY',
      knownIdentity: {
        userId: checkResult.userId,
        username: checkResult.username,
        phoneNumber: checkResult.phoneNumber,
      },
    };
  }

  return {
    name: 'REGISTER',
    deviceReady: true,
    deviceMessage: '',
  };
}

export function useStartupRoute() {
  const [route, setRoute] = useState<AppRoute>({
    name: 'BOOTSTRAPPING',
    message: 'Preparing device...',
  });

  useEffect(() => {
    let isMounted = true;

    const decideStartupRoute = async () => {
      setRoute({
        name: 'BOOTSTRAPPING',
        message: 'Preparing device...',
      });

      const deviceStored = await storeDeviceUseCase.storeDevice();

      if (!isMounted) {
        return;
      }

      if (!deviceStored) {
        setRoute({
          name: 'REGISTER',
          deviceReady: false,
          deviceMessage:
            'Unable to register this device right now. Please reopen the app and try again.',
        });
        return;
      }

      const checkResult = await checkDeviceLinkUseCase.execute();

      if (!isMounted) {
        return;
      }

      if (!checkResult.linked && checkResult.reason === 'DEVICE_NOT_FOUND') {
        setRoute({
          name: 'BOOTSTRAPPING',
          message: 'Restoring device connection...',
        });

        const restored = await storeDeviceUseCase.forceStoreDevice();

        if (!isMounted) {
          return;
        }

        if (!restored) {
          setRoute({
            name: 'REGISTER',
            deviceReady: false,
            deviceMessage:
              'Unable to restore this device right now. Please reopen the app and try again.',
          });
          return;
        }

        const retryCheckResult = await checkDeviceLinkUseCase.execute();

        if (!isMounted) {
          return;
        }

        setRoute(routeFromCheckResult(retryCheckResult));
        return;
      }

      if (!checkResult.linked && checkResult.reason === 'DEVICE_ID_MISSING') {
        setRoute({
          name: 'REGISTER',
          deviceReady: true,
          deviceMessage: '',
        });
        return;
      }

      setRoute(routeFromCheckResult(checkResult));
    };

    decideStartupRoute();

    return () => {
      isMounted = false;
    };
  }, []);

  return route;
}
