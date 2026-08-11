export interface AuthHttpErrorDataDTO {
  code?: unknown;
  resetStatus?: unknown;
  remainingAttempts?: unknown;
  pinResetAttemptId?: unknown;
  [field: string]: unknown;
}

export interface AuthHttpErrorResponseDTO {
  status?: string;
  message?: string;
  data?: AuthHttpErrorDataDTO;
}
