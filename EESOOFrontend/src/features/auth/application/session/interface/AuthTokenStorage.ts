import { AuthTokenPair } from '../../../domain/model/valueobject/AuthTokenPair';

export interface AuthTokenStorage {
  saveTokenPair(tokenPair: AuthTokenPair): Promise<void>;

  getTokenPair(): Promise<AuthTokenPair | null>;

  clearTokenPair(): Promise<void>;
}
