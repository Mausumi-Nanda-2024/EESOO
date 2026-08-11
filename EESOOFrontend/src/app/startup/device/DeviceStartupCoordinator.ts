import { CheckDeviceLinkUseCase } from '../../../features/shared/device/application/CheckDeviceLinkUseCase';
import { CheckDeviceResponseDTO } from '../../../features/shared/device/application/dto/CheckDeviceResponseDTO';
import { StoreDeviceUseCase } from '../../../features/shared/device/application/StoreDeviceUsecase';
import { DeviceStartupResult } from './DeviceStartupResult';

export class DeviceStartupCoordinator {
  constructor(
    private readonly storeDeviceUseCase: StoreDeviceUseCase,
    private readonly checkDeviceLinkUseCase: CheckDeviceLinkUseCase,
  ) {}

  async execute(): Promise<DeviceStartupResult> {
    try {
      const deviceStored =
        await this.storeDeviceUseCase.storeDevice();

      if (!deviceStored) {
        return {
          status: 'NOT_READY',
          message:
            'Unable to register this device right now. Please try again.',
        };
      }

      let checkResult =
        await this.checkDeviceLinkUseCase.execute();

      if (this.isDeviceMissingInBackend(checkResult)) {
        const deviceRestored =
          await this.storeDeviceUseCase.forceStoreDevice();

        if (!deviceRestored) {
          return {
            status: 'NOT_READY',
            message:
              'Unable to restore this device right now. Please try again.',
          };
        }

        checkResult =
          await this.checkDeviceLinkUseCase.execute();
      }

      return this.toDeviceStartupResult(checkResult);
    } catch {
      return {
        status: 'NOT_READY',
        message:
          'Unable to prepare this device right now. Please try again.',
      };
    }
  }

  private isDeviceMissingInBackend(
    checkResult: CheckDeviceResponseDTO,
  ): boolean {
    return (
      !checkResult.linked &&
      (checkResult.reason === 'DEVICE_NOT_FOUND' ||
        checkResult.reason === 'DEVICE_NOT_REGISTERED')
    );
  }

  private toDeviceStartupResult(
    checkResult: CheckDeviceResponseDTO,
  ): DeviceStartupResult {
    if (checkResult.linked) {
      return {
        status: 'LINKED',
        identity: {
          userId: checkResult.userId,
          username: checkResult.username,
          phoneNumber: checkResult.phoneNumber,
        },
      };
    }

    return {
      status: 'READY_NOT_LINKED',
      reason: checkResult.reason,
    };
  }
}
