import { DeviceRepository } from "../domain/repository/DeviceRepository";
import { DeviceApiService } from "./interface/DeviceApiService";
import { CheckDeviceResponseDTO } from "./dto/CheckDeviceResponseDTO";

export class CheckDeviceLinkUseCase {
    constructor(
        private deviceRepository: DeviceRepository,
        private deviceApiService: DeviceApiService
    ) {}

    async execute(): Promise<CheckDeviceResponseDTO> {
        const device = await this.deviceRepository.getDevice();

        if (!device) {
            return {
                linked: false,
                reason: 'DEVICE_NOT_FOUND'
            };
        }

        const deviceId = device.getId();

        if (!deviceId) {
            return {
                linked: false,
                reason: 'DEVICE_ID_MISSING'
            };
        }

        return await this.deviceApiService.checkDeviceLink(deviceId);
    }
}
