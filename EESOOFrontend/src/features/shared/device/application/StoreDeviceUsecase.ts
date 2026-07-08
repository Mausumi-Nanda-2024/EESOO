import { Device } from "../domain/model/entity/device";
import { DeviceRepository } from "../domain/repository/DeviceRepository";
import { DeviceApiService } from "./interface/DeviceApiService";

export class StoreDeviceUseCase {
    private readonly MAX_RETRIES = 3;

    constructor(
        private deviceRepository: DeviceRepository,
        private deviceApiService: DeviceApiService
    ) {}

    async storeDevice(): Promise<boolean> {
        const storedDevice = await this.deviceRepository.getDevice();

        if (!storedDevice) {
            const newDevice = await this.deviceRepository.createDevice();
            return this.syncDevice(newDevice);
        }

        const currentDevice = await this.deviceRepository.getCurrentDeviceInfo(
            storedDevice.getInstallId()
        );

        const hasChanged = !storedDevice.hasDeviceInfoChanged(currentDevice);

        if (!hasChanged && storedDevice.isStored()) {
            return true;
        }

        let deviceToSync = storedDevice;

        if (hasChanged) {
            currentDevice.resetSyncState();
            await this.deviceRepository.saveDevice(currentDevice);
            deviceToSync = currentDevice;
        }

        return this.syncDevice(deviceToSync);
    }

    async forceStoreDevice(): Promise<boolean> {
        const storedDevice = await this.deviceRepository.getDevice();

        if (!storedDevice) {
            const newDevice = await this.deviceRepository.createDevice();
            return this.syncDevice(newDevice);
        }

        storedDevice.resetSyncState();
        await this.deviceRepository.saveDevice(storedDevice);

        return this.syncDevice(storedDevice);
    }

    private async syncDevice(device: Device): Promise<boolean> {
        if (device.isPermanentlyFailed()) {
            return false;
        }

        if (!device.canRetry(this.MAX_RETRIES)) {
            device.markPermanentFailure();
            await this.deviceRepository.saveDevice(device);
            return false;
        }

        try {
            await this.deviceApiService.storeDevice(device);
            device.markAsStoredInBackend();
            await this.deviceRepository.saveDevice(device);
            return true;
        } catch {
            device.markRetry();

            if (!device.canRetry(this.MAX_RETRIES)) {
                device.markPermanentFailure();
            }

            await this.deviceRepository.saveDevice(device);
            return false;
        }
    }
}
