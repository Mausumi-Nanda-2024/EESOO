import { DeviceIdNullableReason } from "../enum/DeviceIdNullableReason";

export interface DeviceIdDiagnostics {
    deviceId: string | null;
    deviceIdNullableReason: DeviceIdNullableReason | null;
   
}
