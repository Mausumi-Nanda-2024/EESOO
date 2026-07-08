import { Device } from "../model/entity/device";

export interface DeviceRepository {

    getDevice(): Promise<Device | null>;

    saveDevice(device: Device): Promise<void>;

    createDevice(): Promise<Device>;

    getCurrentDeviceInfo(installId: string): Promise<Device>;
}