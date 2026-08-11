import { DeviceRepository } from '../../../../shared/device/domain/repository/DeviceRepository';
import { LoginPhoneNumber } from '../../../domain/model/valueobject/LoginPhoneNumber';
import { AuthOperationResult } from '../../common/model/AuthOperationResult';
import { ConfirmPinResetMobileResponseDTO } from '../dto/ConfirmPinResetMobileResponseDTO';
import { PinResetApiService } from '../interface/PinResetApiService';

export class ConfirmPinResetMobileUseCase {
  constructor(
    private readonly pinResetApiService: PinResetApiService,
    private readonly deviceRepository: DeviceRepository,
  ) {}

  async execute(
    pinResetAttemptId: string,
    rawPhoneNumber: string,
  ): Promise<AuthOperationResult<ConfirmPinResetMobileResponseDTO>> {
    if (!pinResetAttemptId.trim()) {
      return {
        ok: false,
        error: {
          reason: 'VALIDATION_FAILURE',
          message: 'The PIN reset attempt is missing.',
        },
      };
    }

    const phoneNumberResult =
      LoginPhoneNumber.create(rawPhoneNumber);

    if (!phoneNumberResult.ok) {
      return {
        ok: false,
        error: {
          reason: 'VALIDATION_FAILURE',
          message: phoneNumberResult.message,
          fieldErrors: {
            phoneNumber: phoneNumberResult.message,
          },
        },
      };
    }

    const device = await this.deviceRepository.getDevice();

    if (!device || !device.isStored()) {
      return {
        ok: false,
        error: {
          reason: 'DEVICE_NOT_READY',
          message: 'Device is not ready for PIN reset.',
        },
      };
    }

    return this.pinResetApiService.confirmMobile({
      pinResetAttemptId,
      phoneNumber: phoneNumberResult.value.getValue(),
      installId: device.getInstallId(),
    });
  }
}
