export interface DeviceStartupIdentity {
  userId: string;
  username: string;
  phoneNumber: string;
}

export type DeviceStartupResult =
  | {
      status: 'LINKED';
      identity: DeviceStartupIdentity;
    }
  | {
      status: 'READY_NOT_LINKED';
      reason: string;
    }
  | {
      status: 'NOT_READY';
      message: string;
    };
