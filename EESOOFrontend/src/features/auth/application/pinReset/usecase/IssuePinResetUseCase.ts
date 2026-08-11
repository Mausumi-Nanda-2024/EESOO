import { DeviceRepository } from '../../../../shared/device/domain/repository/DeviceRepository';
import { AuthOperationResult } from '../../common/model/AuthOperationResult';
import { IssuePinResetResponseDTO } from '../dto/IssuePinResetResponseDTO';
import { PinResetApiService } from '../interface/PinResetApiService';

export class IssuePinResetUseCase {
  constructor(
    private readonly pinResetApiService: PinResetApiService,
    private readonly deviceRepository: DeviceRepository,
  ) {}

  async execute(
    pinResetAttemptId: string,
  ): Promise<AuthOperationResult<IssuePinResetResponseDTO>> {
    if (!pinResetAttemptId.trim()) {
      return {
        ok: false,
        error: {
          reason: 'VALIDATION_FAILURE',
          message: 'The PIN reset attempt is missing.',
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

    return this.pinResetApiService.issuePin({
      pinResetAttemptId,
      installId: device.getInstallId(),
    });
  }
}
