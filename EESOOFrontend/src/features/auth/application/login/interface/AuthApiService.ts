import { AuthOperationResult } from '../../common/model/AuthOperationResult';
import { LoginUserRequestDTO } from '../dto/LoginUserRequestDTO';
import { LoginUserResponseDTO } from '../dto/LoginUserResponseDTO';

export interface AuthApiService {
  login(
    request: LoginUserRequestDTO,
  ): Promise<AuthOperationResult<LoginUserResponseDTO>>;
}
