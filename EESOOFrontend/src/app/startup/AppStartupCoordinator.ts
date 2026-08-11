import { RestoreAuthSessionUseCase } from '../../features/auth/application/session/usecase/RestoreAuthSessionUseCase';
import { AppRoute } from '../routing/AppRoute';
import { DeviceStartupCoordinator } from './device/DeviceStartupCoordinator';

export class AppStartupCoordinator {
  constructor(
    private readonly deviceStartupCoordinator:
      DeviceStartupCoordinator,
    private readonly restoreAuthSessionUseCase:
      RestoreAuthSessionUseCase,
  ) {}

  async execute(): Promise<AppRoute> {
    const deviceResult =
      await this.deviceStartupCoordinator.execute();

    if (deviceResult.status === 'NOT_READY') {
      return {
        name: 'DEVICE_STARTUP_ERROR',
        message: deviceResult.message,
      };
    }

    try {
      const sessionResult =
        await this.restoreAuthSessionUseCase.execute();

      if (
        sessionResult.status === 'RETRY_REQUIRED'
      ) {
        return {
          name: 'SESSION_RESTORE_ERROR',
          message: sessionResult.message,
        };
      }

      if (sessionResult.status === 'RESTORED') {
        if (deviceResult.status === 'LINKED') {
          return {
            name: 'AUTHENTICATED',
            identity: {
              userId: deviceResult.identity.userId,
              username: deviceResult.identity.username,
            },
          };
        }

        return {
          name: 'DEVICE_LINK_RECOVERY',
          reason: deviceResult.reason,
        };
      }

      if (deviceResult.status === 'LINKED') {
        return {
          name: 'LOGIN',
          mode: 'KNOWN_IDENTITY',
          knownIdentity: {
            userId: deviceResult.identity.userId,
            username: deviceResult.identity.username,
            phoneNumber:
              deviceResult.identity.phoneNumber,
          },
        };
      }

      return {
        name: 'AUTH_ENTRY',
      };
    } catch {
      return {
        name: 'SESSION_RESTORE_ERROR',
        message:
          'Unable to determine your session status. Please try again.',
      };
    }
  }
}
