import { AuthTokenPair } from '../../../domain/model/valueobject/AuthTokenPair';
import { AuthSessionApiService } from '../interface/AuthSessionApiService';
import { AuthTokenStorage } from '../interface/AuthTokenStorage';
import { SessionRestorationResult } from '../model/SessionRestorationResult';

export class RestoreAuthSessionUseCase {
  constructor(
    private readonly authTokenStorage: AuthTokenStorage,
    private readonly authSessionApiService: AuthSessionApiService,
  ) {}

  async execute(): Promise<SessionRestorationResult> {
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
        status: 'NO_SESSION',
      };
    }

    const refreshResult =
      await this.authSessionApiService.refresh({
        refreshToken:
          storedTokenPair.getRefreshToken(),
      });

    if (!refreshResult.ok) {
      if (
        refreshResult.reason === 'TEMPORARY_FAILURE'
      ) {
        return {
          status: 'RETRY_REQUIRED',
          message: refreshResult.message,
        };
      }

      const tokensCleared =
        await this.clearTokenPairSafely();

      if (!tokensCleared) {
        return {
          status: 'RETRY_REQUIRED',
          message:
            'Unable to clear the expired session. Please try again.',
        };
      }

      return {
        status: 'NO_SESSION',
      };
    }

    const newTokenPairResult = AuthTokenPair.create(
      refreshResult.value.accessToken,
      refreshResult.value.refreshToken,
    );

    if (!newTokenPairResult.ok) {
      const tokensCleared =
        await this.clearTokenPairSafely();

      if (!tokensCleared) {
        return {
          status: 'RETRY_REQUIRED',
          message:
            'Unable to clear the invalid session. Please try again.',
        };
      }

      return {
        status: 'NO_SESSION',
      };
    }

    try {
      await this.authTokenStorage.saveTokenPair(
        newTokenPairResult.value,
      );
    } catch {
      const tokensCleared =
        await this.clearTokenPairSafely();

      if (!tokensCleared) {
        return {
          status: 'RETRY_REQUIRED',
          message:
            'Unable to update the stored session. Please try again.',
        };
      }

      return {
        status: 'NO_SESSION',
      };
    }

    return {
      status: 'RESTORED',
    };
  }

  private async clearTokenPairSafely(): Promise<boolean> {
    try {
      await this.authTokenStorage.clearTokenPair();
      return true;
    } catch {
      return false;
    }
  }
}