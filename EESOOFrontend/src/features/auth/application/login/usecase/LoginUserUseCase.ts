import { DeviceRepository } from '../../../../shared/device/domain/repository/DeviceRepository';
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
  ): Promise<LoginResult> {
    const credentialsResult = LoginCredentials.create(
      rawCredentials.phoneNumber,
      rawCredentials.pin,
    );

    if (!credentialsResult.ok) {
      throw new Error(credentialsResult.message);
    }

    const credentials = credentialsResult.value;
    const device = await this.deviceRepository.getDevice();

    if (!device) {
      throw new Error('Device is not initialized.');
    }

    if (!device.isStored()) {
      throw new Error('Device is not ready for login.');
    }

    const request: LoginUserRequestDTO = {
      phoneNumber: credentials.getPhoneNumber().getValue(),
      pin: credentials.getPin().getValue(),
      deviceId: device.getId(),
      installId: device.getInstallId(),
    };

    const response = await this.authApiService.login(request);
    const tokenPairResult = AuthTokenPair.create(
      response.accessToken,
      response.refreshToken,
    );

    if (!tokenPairResult.ok) {
      throw new Error(tokenPairResult.message);
    }

    await this.authTokenStorage.saveTokenPair(
      tokenPairResult.value,
    );

    return {
      identity: {
        userId: response.userId,
        username: response.username,
      },
      deviceLinked: response.deviceLinked,
      deviceLinkFailureReason:
        response.deviceLinkFailureReason,
    };
  }
}

