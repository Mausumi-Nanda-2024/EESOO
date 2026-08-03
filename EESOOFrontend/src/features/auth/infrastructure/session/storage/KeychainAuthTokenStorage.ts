import * as Keychain from 'react-native-keychain';
import { AuthTokenStorage } from '../../../application/session/interface/AuthTokenStorage';
import { AuthTokenPair } from '../../../domain/model/valueobject/AuthTokenPair';

const AUTH_TOKEN_SERVICE = 'com.eesoo.auth.tokens';
const AUTH_TOKEN_USERNAME = 'eesoo-auth-token-pair';

interface StoredTokenPair {
  accessToken: string;
  refreshToken: string;
}

function isStoredTokenPair(value: unknown): value is StoredTokenPair {
  if (typeof value !== 'object' || value === null) {
    return false;
  }

  const tokenPair = value as Record<string, unknown>;

  return (
    typeof tokenPair.accessToken === 'string' &&
    typeof tokenPair.refreshToken === 'string'
  );
}

export class KeychainAuthTokenStorage implements AuthTokenStorage {
  async saveTokenPair(tokenPair: AuthTokenPair): Promise<void> {
    const storedTokenPair: StoredTokenPair = {
      accessToken: tokenPair.getAccessToken(),
      refreshToken: tokenPair.getRefreshToken(),
    };

    const result = await Keychain.setGenericPassword(
      AUTH_TOKEN_USERNAME,
      JSON.stringify(storedTokenPair),
      {
        service: AUTH_TOKEN_SERVICE,
        accessible: Keychain.ACCESSIBLE.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
        securityLevel: Keychain.SECURITY_LEVEL.SECURE_SOFTWARE,
      },
    );

    if (!result) {
      throw new Error('Unable to securely store authentication tokens.');
    }
  }

  async getTokenPair(): Promise<AuthTokenPair | null> {
    const credentials = await Keychain.getGenericPassword({
      service: AUTH_TOKEN_SERVICE,
    });

    if (!credentials) {
      return null;
    }

    let storedTokenPair: unknown;

    try {
      storedTokenPair = JSON.parse(credentials.password);
    } catch {
      await this.clearTokenPair();
      return null;
    }

    if (!isStoredTokenPair(storedTokenPair)) {
      await this.clearTokenPair();
      return null;
    }

    const tokenPairResult = AuthTokenPair.create(
      storedTokenPair.accessToken,
      storedTokenPair.refreshToken,
    );

    if (!tokenPairResult.ok) {
      await this.clearTokenPair();
      return null;
    }

    return tokenPairResult.value;
  }

  async clearTokenPair(): Promise<void> {
    await Keychain.resetGenericPassword({
      service: AUTH_TOKEN_SERVICE,
    });
  }
}
