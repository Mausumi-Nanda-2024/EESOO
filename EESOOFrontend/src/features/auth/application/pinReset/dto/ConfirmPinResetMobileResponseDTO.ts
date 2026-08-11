export interface ConfirmPinResetMobileResponseDTO {
  pinResetAttemptId: string;
  status: 'MOBILE_CONFIRMED';
  mobileConfirmedAt: string;
}
