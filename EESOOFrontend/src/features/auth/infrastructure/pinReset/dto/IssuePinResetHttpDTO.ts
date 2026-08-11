export interface IssuePinResetHttpRequestDTO {
  pinResetAttemptId: string;
  installId: string;
}

export interface IssuePinResetHttpResponseDTO {
  pinResetAttemptId: string;
  status: 'PIN_ISSUED';
  newPin: string;
  pinIssuedAt: string;
}

export interface IssuePinResetHttpSuccessResponseDTO {
  status: 'success';
  message: string;
  data: IssuePinResetHttpResponseDTO;
}
