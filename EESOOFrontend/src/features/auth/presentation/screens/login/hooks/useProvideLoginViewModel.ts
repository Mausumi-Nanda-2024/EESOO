import { LoginResult } from '../../../../application/login/model/LoginResult';
import { loginUserUseCase } from '../../../../infrastructure/config/AuthInfrastructureConfig';
import { useLoginViewModel } from '../viewModel/LoginViewModelImpl';

export function useProvideLoginViewModel(
  initialPhoneNumber: string,
  onLoggedIn: (result: LoginResult) => void,
) {
  return useLoginViewModel(
    loginUserUseCase,
    initialPhoneNumber,
    onLoggedIn,
  );
}
