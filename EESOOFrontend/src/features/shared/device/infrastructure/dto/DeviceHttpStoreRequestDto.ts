import { DeviceIdNullableReason } from "../../domain/model/enum/DeviceIdNullableReason";

export interface DeviceHttpStoreRequestDto {
    deviceId: string | null;
    installId: string;
    osVersion: string;
    platform: string;
    deviceIdNullableReason: DeviceIdNullableReason | null;
}
