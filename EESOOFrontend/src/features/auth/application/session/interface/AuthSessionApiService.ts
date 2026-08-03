import { LogoutRequestDTO } from '../dto/LogoutRequestDTO';
import { RefreshTokenRequestDTO } from '../dto/RefreshTokenRequestDTO';
import { LogoutApiResult } from '../model/LogoutApiResult';
import { RefreshTokenApiResult } from '../model/RefreshTokenApiResult';

export interface AuthSessionApiService {
  refresh(
    request: RefreshTokenRequestDTO,
  ): Promise<RefreshTokenApiResult>;

  logout(
    request: LogoutRequestDTO,
  ): Promise<LogoutApiResult>;
}
