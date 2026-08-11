
import { RegisterUserResponseDTO } from "../../../../application/register/dto/RegisterUserResponseDTO";
import { registerUserUseCase } from "../../../../infrastructure/config/UserInfrastructureConfig";
import { useRegisterViewModel as useRegisterViewModelImpl } from "../viewModel/RegisterViewModelImpl";

export function useRegisterViewModel(
  onRegistered?:(response: RegisterUserResponseDTO) => void
) {
  return useRegisterViewModelImpl(registerUserUseCase , onRegistered);
}