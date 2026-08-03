export interface LoginUserHttpRequestDTO {
  phoneNumber: string;
  pin: string;
  deviceId: string | null;
  installId: string;
}
