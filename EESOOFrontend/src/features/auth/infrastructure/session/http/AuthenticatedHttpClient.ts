import axios, {
  AxiosError,
  AxiosInstance,
  InternalAxiosRequestConfig,
} from 'axios';
import { API_BASE_URL } from '../../../../../config/apiConfig';
import { AuthTokenStorage } from '../../../application/session/interface/AuthTokenStorage';
import { RestoreAuthSessionUseCase } from '../../../application/session/usecase/RestoreAuthSessionUseCase';

type RetriableRequestConfig = InternalAxiosRequestConfig & {
  authRetryAttempted?: boolean;
};

export class AuthenticatedHttpClient {
  readonly axiosInstance: AxiosInstance;

  private refreshPromise: Promise<string | null> | null = null;
  private sessionInvalidatedHandler: (() => void) | null = null;

  constructor(
    private readonly authTokenStorage: AuthTokenStorage,
    private readonly restoreAuthSessionUseCase: RestoreAuthSessionUseCase,
  ) {
    this.axiosInstance = axios.create({
      baseURL: API_BASE_URL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.configureInterceptors();
  }

  setSessionInvalidatedHandler(
    handler: (() => void) | null,
  ): void {
    this.sessionInvalidatedHandler = handler;
  }

  private configureInterceptors(): void {
    this.axiosInstance.interceptors.request.use(
      async config => {
        const tokenPair =
          await this.authTokenStorage.getTokenPair();

        if (tokenPair) {
          config.headers.Authorization = `Bearer ${tokenPair.getAccessToken()}`;
        }

        return config;
      },
    );

    this.axiosInstance.interceptors.response.use(
      response => response,
      async (error: unknown) => {
        if (!axios.isAxiosError(error)) {
          return Promise.reject(error);
        }

        return this.handleResponseError(error);
      },
    );
  }

  private async handleResponseError(
    error: AxiosError,
  ): Promise<unknown> {
    const requestConfig =
      error.config as RetriableRequestConfig | undefined;

    if (
      error.response?.status !== 401 ||
      !requestConfig ||
      requestConfig.authRetryAttempted
    ) {
      return Promise.reject(error);
    }

    requestConfig.authRetryAttempted = true;

    try {
      const accessToken = await this.refreshAccessToken();

      if (!accessToken) {
        return Promise.reject(error);
      }

      requestConfig.headers.Authorization =
        `Bearer ${accessToken}`;

      return this.axiosInstance.request(requestConfig);
    } catch (refreshError: unknown) {
      return Promise.reject(refreshError);
    }
  }

  private refreshAccessToken(): Promise<string | null> {
    if (!this.refreshPromise) {
      this.refreshPromise = this.performRefresh().finally(() => {
        this.refreshPromise = null;
      });
    }

    return this.refreshPromise;
  }

  private async performRefresh(): Promise<string | null> {
    const result = await this.restoreAuthSessionUseCase.execute();

    if (result.status === 'RETRY_REQUIRED') {
      throw new Error(result.message);
    }

    if (result.status === 'NO_SESSION') {
      this.sessionInvalidatedHandler?.();
      return null;
    }

    const tokenPair =
      await this.authTokenStorage.getTokenPair();

    if (!tokenPair) {
      this.sessionInvalidatedHandler?.();
      return null;
    }

    return tokenPair.getAccessToken();
  }
}
