export interface LogoutHttpSuccessResponseDTO {
  status: 'success';
  message: string;
  data: null;
}

export interface LogoutHttpErrorResponseDTO {
  status: 'error';
  message: string;
  data: unknown;
}
