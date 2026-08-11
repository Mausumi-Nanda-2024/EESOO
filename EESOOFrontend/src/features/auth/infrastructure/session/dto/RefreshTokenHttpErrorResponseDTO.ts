export interface RefreshTokenHttpErrorDataDTO {
  code?: string;
}

export interface RefreshTokenHttpErrorResponseDTO {
  status: 'error';
  message: string;
  data: RefreshTokenHttpErrorDataDTO | null;
}
