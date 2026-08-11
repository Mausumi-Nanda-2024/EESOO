import axiosInstance from '../../../../../config/axiosInstance';
import { AuthOperationResult } from '../../../application/common/model/AuthOperationResult';
import { LoginUserRequestDTO } from '../../../application/login/dto/LoginUserRequestDTO';
import { LoginUserResponseDTO } from '../../../application/login/dto/LoginUserResponseDTO';
import { AuthApiService } from '../../../application/login/interface/AuthApiService';
import { AuthHttpErrorMapper } from '../../common/mapper/AuthHttpErrorMapper';
import { LoginUserHttpRequestDTO } from '../dto/LoginUserHttpRequestDTO';
import { LoginUserHttpSuccessResponseDTO } from '../dto/LoginUserHttpResponseDTO';
import { LoginUserHttpMapper } from '../mapper/LoginUserHttpMapper';

export class AuthApiServiceImpl implements AuthApiService {
  private readonly ENDPOINT = '/auth/login';

  async login(
    request: LoginUserRequestDTO,
  ): Promise<AuthOperationResult<LoginUserResponseDTO>> {
    const httpRequest: LoginUserHttpRequestDTO =
      LoginUserHttpMapper.toHttpRequest(request);

    try {
      const response =
        await axiosInstance.post<LoginUserHttpSuccessResponseDTO>(
          this.ENDPOINT,
          httpRequest,
        );

      return {
        ok: true,
        value: LoginUserHttpMapper.toResponse(
          response.data.data,
        ),
      };
    } catch (error: unknown) {
      return {
        ok: false,
        error: AuthHttpErrorMapper.fromUnknown(
          error,
          'Login failed. Please try again.',
        ),
      };
    }
  }
}
