export interface LoginUserResponseDTO {
  userId: string;
  username: string;
  accessToken: string;
  refreshToken: string;
  deviceLinked: boolean;
  deviceLinkFailureReason: string | null;
}