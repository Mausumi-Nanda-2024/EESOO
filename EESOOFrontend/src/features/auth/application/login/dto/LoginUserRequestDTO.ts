export interface LoginUserRequestDTO {
    phoneNumber: string;
    pin: string;
    deviceId: string | null;
    installId: string;
}