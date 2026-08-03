import { AuthApiService } from '../../application/login/interface/AuthApiService';
import { LoginUserUseCase } from '../../application/login/usecase/LoginUserUseCase';
import { AuthSessionApiService } from '../../application/session/interface/AuthSessionApiService';
import { AuthTokenStorage } from '../../application/session/interface/AuthTokenStorage';
import { LogoutUserUseCase } from '../../application/session/usecase/LogoutUserUseCase';
import { RestoreAuthSessionUseCase } from '../../application/session/usecase/RestoreAuthSessionUseCase';
import { deviceRepository } from '../../../shared/device/infrastructure/config/DeviceInfrastructureConfig';
import { AuthApiServiceImpl } from '../login/api/AuthApiServiceImpl';
import { AuthSessionApiServiceImpl } from '../session/api/AuthSessionApiServiceImpl';
import { AuthenticatedHttpClient } from '../session/http/AuthenticatedHttpClient';
import { KeychainAuthTokenStorage } from '../session/storage/KeychainAuthTokenStorage';

const authApiServiceInstance: AuthApiService =
  new AuthApiServiceImpl();

const authSessionApiServiceInstance: AuthSessionApiService =
  new AuthSessionApiServiceImpl();

export const authTokenStorage: AuthTokenStorage =
  new KeychainAuthTokenStorage();

export const loginUserUseCase = new LoginUserUseCase(
  authApiServiceInstance,
  deviceRepository,
  authTokenStorage,
);

export const restoreAuthSessionUseCase =
  new RestoreAuthSessionUseCase(
    authTokenStorage,
    authSessionApiServiceInstance,
  );

export const logoutUserUseCase = new LogoutUserUseCase(
  authTokenStorage,
  authSessionApiServiceInstance,
);

export const authenticatedHttpClient =
  new AuthenticatedHttpClient(
    authTokenStorage,
    restoreAuthSessionUseCase,
  );

export const authenticatedAxiosInstance =
  authenticatedHttpClient.axiosInstance;
