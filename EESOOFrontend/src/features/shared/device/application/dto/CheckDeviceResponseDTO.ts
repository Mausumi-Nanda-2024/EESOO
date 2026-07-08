export interface DeviceLinked {
    linked: true;
    userId: string;
    username: string;
    phoneNumber: string;
}

export interface DeviceNotLinked {
    linked: false;
    reason: string;
}

export type CheckDeviceResponseDTO = DeviceLinked | DeviceNotLinked;