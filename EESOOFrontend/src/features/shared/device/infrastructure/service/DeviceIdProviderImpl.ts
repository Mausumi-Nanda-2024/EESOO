import DeviceInfo from "react-native-device-info";
import { DeviceIdDiagnostics } from "../../domain/model/entity/DeviceIdDiagnostics";
import { DeviceIdNullableReason } from "../../domain/model/enum/DeviceIdNullableReason";
import { DeviceIdProvider } from "../../domain/service/DeviceIdProvider";

const DEVICE_ID_FETCH_ATTEMPTS = 3;
const FORCE_DEVICE_ID_FAILURE = false; // Set to true to simulate device ID fetch failure for testing purposes
const FORCED_NULL_REASON = DeviceIdNullableReason.UNKNOWN;

const mapToNullableReason = (error: unknown): DeviceIdNullableReason => {
    const message = error instanceof Error ? error.message.toLowerCase() : String(error).toLowerCase();

    if (message.includes("not supported")) {
        return DeviceIdNullableReason.NOT_SUPPORTED;
    }


    if (message.includes("restricted")) {
        return DeviceIdNullableReason.OS_RESTRICTED;
    }

    if (message.includes("service")) {
        return DeviceIdNullableReason.SERVICES_MISSING;
    }


    return DeviceIdNullableReason.ERROR;
};

export class DeviceIdProviderImpl implements DeviceIdProvider {
    async fetch(): Promise<DeviceIdDiagnostics> {
        let lastReason: DeviceIdNullableReason = DeviceIdNullableReason.UNKNOWN;

        for (let attempt = 1; attempt <= DEVICE_ID_FETCH_ATTEMPTS; attempt += 1) {
            try {
                if (FORCE_DEVICE_ID_FAILURE) {
                    return {
                        deviceId: null,
                        deviceIdNullableReason: FORCED_NULL_REASON,
                    };
                }

                const rawValue = await DeviceInfo.getUniqueId();

                if (rawValue === null || rawValue === undefined) {
                    lastReason = DeviceIdNullableReason.UNKNOWN;
                    continue;
                }

                const deviceId = String(rawValue).trim();

                if (!deviceId) {
                    lastReason = DeviceIdNullableReason.UNKNOWN;
                    continue;
                }

                return {
                    deviceId,
                    deviceIdNullableReason: null,
                };
            } catch (error) {
                lastReason = mapToNullableReason(error);
            }
        }

        return {
            deviceId: null,
            deviceIdNullableReason: lastReason,
        };
    }
}
