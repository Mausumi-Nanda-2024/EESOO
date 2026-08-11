import { AuthOperationResult } from '../../common/model/AuthOperationResult';
import { ConfirmPinResetMobileRequestDTO } from '../dto/ConfirmPinResetMobileRequestDTO';
import { ConfirmPinResetMobileResponseDTO } from '../dto/ConfirmPinResetMobileResponseDTO';
import { IssuePinResetRequestDTO } from '../dto/IssuePinResetRequestDTO';
import { IssuePinResetResponseDTO } from '../dto/IssuePinResetResponseDTO';

export interface PinResetApiService {
  confirmMobile(
    request: ConfirmPinResetMobileRequestDTO,
  ): Promise<AuthOperationResult<ConfirmPinResetMobileResponseDTO>>;

  issuePin(
    request: IssuePinResetRequestDTO,
  ): Promise<AuthOperationResult<IssuePinResetResponseDTO>>;
}
