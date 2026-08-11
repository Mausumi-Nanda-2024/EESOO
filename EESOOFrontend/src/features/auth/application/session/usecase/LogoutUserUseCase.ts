import { AuthTokenPair } from '../../../domain/model/valueobject/AuthTokenPair';
import { AuthSessionApiService } from '../interface/AuthSessionApiService';
import { AuthTokenStorage } from '../interface/AuthTokenStorage';
import { LogoutResult } from '../model/LogoutResult';

export class LogoutUserUseCase {
  constructor(
    private readonly authTokenStorage: AuthTokenStorage,
    private readonly authSessionApiService: AuthSessionApiService,
  ) {}

  async execute(): Promise<LogoutResult> {
    let storedTokenPair: AuthTokenPair | null;

    try {
      storedTokenPair =
        await this.authTokenStorage.getTokenPair();
    } catch {
      return {
        status: 'RETRY_REQUIRED',
        message:
          'Unable to read the stored session. Please try again.',
      };
    }

    if (!storedTokenPair) {
      return {
        status: 'LOGGED_OUT',
      };
    }

    const logoutResult =
      await this.authSessionApiService.logout({
        accessToken:
          storedTokenPair.getAccessToken(),
      });

    if (logoutResult.ok) {
      return this.clearLocalSession();
    }

    if (
      logoutResult.reason === 'TEMPORARY_FAILURE'
    ) {
      return {
        status: 'RETRY_REQUIRED',
        message: logoutResult.message,
      };
    }

    return this.refreshAndRetryLogout(
      storedTokenPair,
    );
  }

  private async refreshAndRetryLogout(
    storedTokenPair: AuthTokenPair,
  ): Promise<LogoutResult> {
    const refreshResult =
      await this.authSessionApiService.refresh({
        refreshToken:
          storedTokenPair.getRefreshToken(),
      });

    if (!refreshResult.ok) {
      if (
        refreshResult.reason ===
        'TEMPORARY_FAILURE'
      ) {
        return {
          status: 'RETRY_REQUIRED',
          message: refreshResult.message,
        };
      }

      return this.clearLocalSession();
    }

    const newTokenPairResult = AuthTokenPair.create(
      refreshResult.value.accessToken,
      refreshResult.value.refreshToken,
    );

    if (!newTokenPairResult.ok) {
      return this.clearLocalSession();
    }

    try {
      await this.authTokenStorage.saveTokenPair(
        newTokenPairResult.value,
      );
    } catch {
      return {
        status: 'RETRY_REQUIRED',
        message:
          'Unable to securely update the session. Please try again.',
      };
    }

    const retryLogoutResult =
      await this.authSessionApiService.logout({
        accessToken:
          newTokenPairResult.value.getAccessToken(),
      });

    if (retryLogoutResult.ok) {
      return this.clearLocalSession();
    }

    if (
      retryLogoutResult.reason ===
      'UNAUTHORIZED'
    ) {
      return this.clearLocalSession();
    }

    return {
      status: 'RETRY_REQUIRED',
      message: retryLogoutResult.message,
    };
  }

  private async clearLocalSession(): Promise<LogoutResult> {
    try {
      await this.authTokenStorage.clearTokenPair();

      return {
        status: 'LOGGED_OUT',
      };
    } catch {
      return {
        status: 'RETRY_REQUIRED',
        message:
          'Unable to clear the local session. Please try again.',
      };
    }
  }
}