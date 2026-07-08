import { DeviceIdDiagnostics } from "../model/entity/DeviceIdDiagnostics";

export interface DeviceIdProvider {
    fetch(): Promise<DeviceIdDiagnostics>;
}
