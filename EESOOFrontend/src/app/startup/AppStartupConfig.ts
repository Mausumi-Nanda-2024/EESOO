import { restoreAuthSessionUseCase } from '../../features/auth/infrastructure/config/AuthInfrastructureConfig';
import {
  checkDeviceLinkUseCase,
  storeDeviceUseCase,
} from '../../features/shared/device/infrastructure/config/DeviceInfrastructureConfig';
import { AppStartupCoordinator } from './AppStartupCoordinator';
import { DeviceStartupCoordinator } from './device/DeviceStartupCoordinator';

export const deviceStartupCoordinator =
  new DeviceStartupCoordinator(
    storeDeviceUseCase,
    checkDeviceLinkUseCase,
  );

export const appStartupCoordinator =
  new AppStartupCoordinator(
    deviceStartupCoordinator,
    restoreAuthSessionUseCase,
  );
