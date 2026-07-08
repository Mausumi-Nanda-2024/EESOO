export interface RegisterUserResponseDTO {
  userId: string;
  username: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  email: string | null;
  status: string;
  registeredAt: string;
}