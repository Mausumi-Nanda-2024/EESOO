import { DeviceIdNullableReason } from "../enum/DeviceIdNullableReason";

export class Device {
    private constructor(
        private readonly id: string | null,
        private readonly installId: string,
        private readonly osVersion: string,
        private readonly platform: string,
        private readonly deviceIdNullableReason: DeviceIdNullableReason | null,
        private deviceStoredInBackend: boolean,
        private retryCount: number,
        private permanentlyFailed: boolean
    ) {}

    static create(
        id: string | null,
        installId: string,
        osVersion: string,
        platform: string,
        deviceIdNullableReason: DeviceIdNullableReason | null
    ): Device {
        return new Device(id, installId, osVersion, platform, deviceIdNullableReason, false, 0, false);
    }

    static rehydrate(
        id: string | null,
        installId: string,
        osVersion: string,
        platform: string,
        deviceIdNullableReason: DeviceIdNullableReason | null,
        deviceStoredInBackend: boolean,
        retryCount: number,
        permanentlyFailed: boolean
    ): Device {
        return new Device(
            id,
            installId,
            osVersion,
            platform,
            deviceIdNullableReason,
            deviceStoredInBackend,
            retryCount,
            permanentlyFailed
        );
    }

    isStored(): boolean {
        return this.deviceStoredInBackend;
    }

    isPermanentlyFailed(): boolean {
        return this.permanentlyFailed;
    }

    getRetryCount(): number {
        return this.retryCount;
    }

    getId(): string | null {
        return this.id;
    }

    getInstallId(): string {
        return this.installId;
    }

    getOsVersion(): string {
        return this.osVersion;
    }

    getPlatform(): string {
        return this.platform;
    }

    getDeviceIdNullableReason(): DeviceIdNullableReason | null {
        return this.deviceIdNullableReason;
    }

    markAsStoredInBackend(): void {
        this.deviceStoredInBackend = true;
        this.retryCount = 0;
        this.permanentlyFailed = false;
    }

    markRetry(): void {
        this.retryCount += 1;
    }

    markPermanentFailure(): void {
        this.permanentlyFailed = true;
    }

    canRetry(maxRetries: number): boolean {
        return this.retryCount < maxRetries && !this.permanentlyFailed;
    }

    hasDeviceInfoChanged(other: Device): boolean {
        return (
            this.id === other.id &&
            this.installId === other.installId &&
            this.osVersion === other.osVersion &&
            this.platform === other.platform &&
            this.deviceIdNullableReason === other.deviceIdNullableReason
        );
    }

    resetSyncState(): void {
        this.deviceStoredInBackend = false;
        this.retryCount = 0;
        this.permanentlyFailed = false;
    }

    getState() {
        return {
            id: this.id,
            installId: this.installId,
            osVersion: this.osVersion,
            platform: this.platform,
            deviceIdNullableReason: this.deviceIdNullableReason,
            deviceStoredInBackend: this.deviceStoredInBackend,
            retryCount: this.retryCount,
            permanentlyFailed: this.permanentlyFailed,
        };
    }
}
