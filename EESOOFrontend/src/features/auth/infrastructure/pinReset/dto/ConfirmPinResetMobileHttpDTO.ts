export interface ConfirmPinResetMobileHttpRequestDTO {
  pinResetAttemptId: string;
  phoneNumber: string;
  installId: string;
}

export interface ConfirmPinResetMobileHttpResponseDTO {
  pinResetAttemptId: string;
  status: 'MOBILE_CONFIRMED';
  mobileConfirmedAt: string;
}

export interface ConfirmPinResetMobileHttpSuccessResponseDTO {
  status: 'success';
  message: string;
  data: ConfirmPinResetMobileHttpResponseDTO;
}
