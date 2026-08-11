import { RefreshTokenRequestDTO } from '../../../application/session/dto/RefreshTokenRequestDTO';
import { RefreshTokenResponseDTO } from '../../../application/session/dto/RefreshTokenResponseDTO';
import { RefreshTokenHttpRequestDTO } from '../dto/RefreshTokenHttpRequestDTO';
import { RefreshTokenHttpResponseDataDTO } from '../dto/RefreshTokenHttpResponseDTO';

export class RefreshTokenHttpMapper {
  static toHttpRequest(
    dto: RefreshTokenRequestDTO,
  ): RefreshTokenHttpRequestDTO {
    return {
      refreshToken: dto.refreshToken,
    };
  }

  static toResponse(
    dto: RefreshTokenHttpResponseDataDTO,
  ): RefreshTokenResponseDTO {
    return {
      accessToken: dto.accessToken,
      refreshToken: dto.refreshToken,
    };
  }
}
