export interface RegisterUserHttpRequestDTO {
    firstName: string;
    lastName: string;
    phoneNumber: string;
    pin: string;
    email: string | null;
    deviceId: string | null;
    installId: string;
}
