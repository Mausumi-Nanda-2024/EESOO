
import { registerUserUseCase } from "../../../../infrastructure/config/UserInfrastructureConfig";
import { useRegisterViewModel as useRegisterViewModelImpl } from "../viewModel/RegisterViewModelImpl";

export function useRegisterViewModel() {
  return useRegisterViewModelImpl(registerUserUseCase);
}