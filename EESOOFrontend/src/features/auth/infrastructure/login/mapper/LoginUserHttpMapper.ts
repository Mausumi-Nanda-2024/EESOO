import { LoginUserRequestDTO } from '../../../application/login/dto/LoginUserRequestDTO';
import { LoginUserResponseDTO } from '../../../application/login/dto/LoginUserResponseDTO';
import { LoginUserHttpRequestDTO } from '../dto/LoginUserHttpRequestDTO';
import { LoginUserHttpResponseDTO } from '../dto/LoginUserHttpResponseDTO';

export class LoginUserHttpMapper {
  static toHttpRequest(
    dto: LoginUserRequestDTO,
  ): LoginUserHttpRequestDTO {
    return {
      phoneNumber: dto.phoneNumber,
      pin: dto.pin,
      deviceId: dto.deviceId,
      installId: dto.installId,
    };
  }

  static toResponse(
    dto: LoginUserHttpResponseDTO,
  ): LoginUserResponseDTO {
    return {
      userId: dto.userId,
      username: dto.username,
      accessToken: dto.accessToken,
      refreshToken: dto.refreshToken,
      deviceLinked: dto.deviceLinked,
      deviceLinkFailureReason: dto.deviceLinkFailureReason,
    };
  }
}
