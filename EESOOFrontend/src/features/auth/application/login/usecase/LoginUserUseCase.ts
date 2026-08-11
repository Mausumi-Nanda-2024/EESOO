import { DeviceRepository } from '../../../../shared/device/domain/repository/DeviceRepository';
import { AuthOperationResult } from '../../common/model/AuthOperationResult';
import { AuthTokenPair } from '../../../domain/model/valueobject/AuthTokenPair';
import { LoginCredentials } from '../../../domain/model/valueobject/LoginCredentials';
import { AuthTokenStorage } from '../../session/interface/AuthTokenStorage';
import { LoginUserRequestDTO } from '../dto/LoginUserRequestDTO';
import { AuthApiService } from '../interface/AuthApiService';
import { LoginResult } from '../model/LoginResult';

export class LoginUserUseCase {
  constructor(
    private readonly authApiService: AuthApiService,
    private readonly deviceRepository: DeviceRepository,
    private readonly authTokenStorage: AuthTokenStorage,
  ) {}

  async loginUser(
    rawCredentials: Omit<
      LoginUserRequestDTO,
      'deviceId' | 'installId'
    >,
  ): Promise<AuthOperationResult<LoginResult>> {
    const credentialsResult = LoginCredentials.create(
      rawCredentials.phoneNumber,
      rawCredentials.pin,
    );

    if (!credentialsResult.ok) {
      return {
        ok: false,
        error: {
          reason: 'VALIDATION_FAILURE',
          message: credentialsResult.message,
        },
      };
    }

    const credentials = credentialsResult.value;
    const device = await this.deviceRepository.getDevice();

    if (!device) {
      return {
        ok: false,
        error: {
          reason: 'DEVICE_NOT_READY',
          message: 'Device is not initialized.',
        },
      };
    }

    if (!device.isStored()) {
      return {
        ok: false,
        error: {
          reason: 'DEVICE_NOT_READY',
          message: 'Device is not ready for login.',
        },
      };
    }

    const request: LoginUserRequestDTO = {
      phoneNumber: credentials.getPhoneNumber().getValue(),
      pin: credentials.getPin().getValue(),
      deviceId: device.getId(),
      installId: device.getInstallId(),
    };

    const apiResult = await this.authApiService.login(request);

    if (!apiResult.ok) {
      return apiResult;
    }

    const response = apiResult.value;
    const tokenPairResult = AuthTokenPair.create(
      response.accessToken,
      response.refreshToken,
    );

    if (!tokenPairResult.ok) {
      return {
        ok: false,
        error: {
          reason: 'TEMPORARY_FAILURE',
          message: tokenPairResult.message,
        },
      };
    }

    await this.authTokenStorage.saveTokenPair(
      tokenPairResult.value,
    );

    return {
      ok: true,
      value: {
        identity: {
          userId: response.userId,
          username: response.username,
        },
        deviceLinked: response.deviceLinked,
        deviceLinkFailureReason:
          response.deviceLinkFailureReason,
      },
    };
  }
}

