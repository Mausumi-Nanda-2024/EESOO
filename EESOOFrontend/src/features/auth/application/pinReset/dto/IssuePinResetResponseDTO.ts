export interface IssuePinResetResponseDTO {
  pinResetAttemptId: string;
  status: 'PIN_ISSUED';
  newPin: string;
  pinIssuedAt: string;
}
