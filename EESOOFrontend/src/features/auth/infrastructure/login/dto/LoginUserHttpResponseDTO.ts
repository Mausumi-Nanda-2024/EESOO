export interface LoginUserHttpResponseDTO {
  userId: string;
  username: string;
  accessToken: string;
  refreshToken: string;
  deviceLinked: boolean;
  deviceLinkFailureReason: string | null;
}

export interface LoginUserHttpSuccessResponseDTO {
  status: 'success';
  message: string;
  data: LoginUserHttpResponseDTO;
}
