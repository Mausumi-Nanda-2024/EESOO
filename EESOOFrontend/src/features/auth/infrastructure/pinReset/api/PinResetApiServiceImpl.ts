import axiosInstance from '../../../../../config/axiosInstance';
import { AuthOperationResult } from '../../../application/common/model/AuthOperationResult';
import { ConfirmPinResetMobileRequestDTO } from '../../../application/pinReset/dto/ConfirmPinResetMobileRequestDTO';
import { ConfirmPinResetMobileResponseDTO } from '../../../application/pinReset/dto/ConfirmPinResetMobileResponseDTO';
import { IssuePinResetRequestDTO } from '../../../application/pinReset/dto/IssuePinResetRequestDTO';
import { IssuePinResetResponseDTO } from '../../../application/pinReset/dto/IssuePinResetResponseDTO';
import { PinResetApiService } from '../../../application/pinReset/interface/PinResetApiService';
import { AuthHttpErrorMapper } from '../../common/mapper/AuthHttpErrorMapper';
import { ConfirmPinResetMobileHttpSuccessResponseDTO } from '../dto/ConfirmPinResetMobileHttpDTO';
import { IssuePinResetHttpSuccessResponseDTO } from '../dto/IssuePinResetHttpDTO';
import { PinResetHttpMapper } from '../mapper/PinResetHttpMapper';

export class PinResetApiServiceImpl
  implements PinResetApiService
{
  private readonly CONFIRM_MOBILE_ENDPOINT =
    '/auth/pin-reset/confirm-mobile';
  private readonly ISSUE_PIN_ENDPOINT = '/auth/pin-reset/issue';

  async confirmMobile(
    request: ConfirmPinResetMobileRequestDTO,
  ): Promise<
    AuthOperationResult<ConfirmPinResetMobileResponseDTO>
  > {
    try {
      const response =
        await axiosInstance.post<ConfirmPinResetMobileHttpSuccessResponseDTO>(
          this.CONFIRM_MOBILE_ENDPOINT,
          PinResetHttpMapper.toConfirmMobileHttpRequest(request),
        );

      return {
        ok: true,
        value: PinResetHttpMapper.toConfirmMobileResponse(
          response.data.data,
        ),
      };
    } catch (error: unknown) {
      return {
        ok: false,
        error: AuthHttpErrorMapper.fromUnknown(
          error,
          'Unable to confirm the mobile number.',
        ),
      };
    }
  }

  async issuePin(
    request: IssuePinResetRequestDTO,
  ): Promise<AuthOperationResult<IssuePinResetResponseDTO>> {
    try {
      const response =
        await axiosInstance.post<IssuePinResetHttpSuccessResponseDTO>(
          this.ISSUE_PIN_ENDPOINT,
          PinResetHttpMapper.toIssuePinHttpRequest(request),
        );

      return {
        ok: true,
        value: PinResetHttpMapper.toIssuePinResponse(
          response.data.data,
        ),
      };
    } catch (error: unknown) {
      return {
        ok: false,
        error: AuthHttpErrorMapper.fromUnknown(
          error,
          'Unable to issue a replacement PIN.',
        ),
      };
    }
  }
}
