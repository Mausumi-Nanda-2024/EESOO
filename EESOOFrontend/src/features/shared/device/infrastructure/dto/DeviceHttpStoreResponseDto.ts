export interface DeviceHttpStoreResponseDto {
    deviceId: string;
    alreadyExists: boolean;
    linkedToUser: boolean;
    linkedUserId: string | null;
}
