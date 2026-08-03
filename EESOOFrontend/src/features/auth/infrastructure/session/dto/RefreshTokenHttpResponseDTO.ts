export interface RefreshTokenHttpResponseDataDTO {
  accessToken: string;
  refreshToken: string;
}

export interface RefreshTokenHttpSuccessResponseDTO {
  status: 'success';
  message: string;
  data: RefreshTokenHttpResponseDataDTO;
}