import { ConfirmPinResetMobileRequestDTO } from '../../../application/pinReset/dto/ConfirmPinResetMobileRequestDTO';
import { ConfirmPinResetMobileResponseDTO } from '../../../application/pinReset/dto/ConfirmPinResetMobileResponseDTO';
import { IssuePinResetRequestDTO } from '../../../application/pinReset/dto/IssuePinResetRequestDTO';
import { IssuePinResetResponseDTO } from '../../../application/pinReset/dto/IssuePinResetResponseDTO';
import {
  ConfirmPinResetMobileHttpRequestDTO,
  ConfirmPinResetMobileHttpResponseDTO,
} from '../dto/ConfirmPinResetMobileHttpDTO';
import {
  IssuePinResetHttpRequestDTO,
  IssuePinResetHttpResponseDTO,
} from '../dto/IssuePinResetHttpDTO';

export class PinResetHttpMapper {
  static toConfirmMobileHttpRequest(
    dto: ConfirmPinResetMobileRequestDTO,
  ): ConfirmPinResetMobileHttpRequestDTO {
    return { ...dto };
  }

  static toConfirmMobileResponse(
    dto: ConfirmPinResetMobileHttpResponseDTO,
  ): ConfirmPinResetMobileResponseDTO {
    return { ...dto };
  }

  static toIssuePinHttpRequest(
    dto: IssuePinResetRequestDTO,
  ): IssuePinResetHttpRequestDTO {
    return { ...dto };
  }

  static toIssuePinResponse(
    dto: IssuePinResetHttpResponseDTO,
  ): IssuePinResetResponseDTO {
    return { ...dto };
  }
}
