import { Device } from "../../domain/model/entity/device";
import { CheckDeviceResponseDTO } from "../dto/CheckDeviceResponseDTO";

export interface DeviceApiService {
    storeDevice(device: Device): Promise<void>;
    checkDeviceLink(deviceId: string): Promise<CheckDeviceResponseDTO>;
}