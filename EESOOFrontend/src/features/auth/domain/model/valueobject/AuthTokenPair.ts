import { ValueResult } from '../../../../shared/domain/common/ValueResult';

export class AuthTokenPair {
  private constructor(
    private readonly accessToken: string,
    private readonly refreshToken: string,
  ) {}

  static create(
    accessToken: string,
    refreshToken: string,
  ): ValueResult<AuthTokenPair> {
    if (!accessToken || accessToken.trim() === '') {
      return {
        ok: false,
        message: 'Access token cannot be empty.',
      };
    }

    if (!refreshToken || refreshToken.trim() === '') {
      return {
        ok: false,
        message: 'Refresh token cannot be empty.',
      };
    }

    return {
      ok: true,
      value: new AuthTokenPair(
        accessToken,
        refreshToken,
      ),
    };
  }

  getAccessToken(): string {
    return this.accessToken;
  }

  getRefreshToken(): string {
    return this.refreshToken;
  }
}
