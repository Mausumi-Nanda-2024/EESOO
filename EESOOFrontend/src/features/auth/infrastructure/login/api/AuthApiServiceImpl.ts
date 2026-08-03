import axios from 'axios';

import axiosInstance from '../../../../../config/axiosInstance';
import { LoginUserRequestDTO } from '../../../application/login/dto/LoginUserRequestDTO';
import { LoginUserResponseDTO } from '../../../application/login/dto/LoginUserResponseDTO';
import { AuthApiService } from '../../../application/login/interface/AuthApiService';
import { LoginUserHttpRequestDTO } from '../dto/LoginUserHttpRequestDTO';
import { LoginUserHttpSuccessResponseDTO } from '../dto/LoginUserHttpResponseDTO';
import { LoginUserHttpMapper } from '../mapper/LoginUserHttpMapper';

type AuthErrorHttpResponse = {
  status?: string;
  message?: string;
  data?: unknown;
};

export class AuthApiServiceImpl implements AuthApiService {
  private readonly ENDPOINT = '/auth/login';

  async login(
    request: LoginUserRequestDTO,
  ): Promise<LoginUserResponseDTO> {
    const httpRequest: LoginUserHttpRequestDTO =
      LoginUserHttpMapper.toHttpRequest(request);

    try {
      const response =
        await axiosInstance.post<LoginUserHttpSuccessResponseDTO>(
          this.ENDPOINT,
          httpRequest,
        );

      return LoginUserHttpMapper.toResponse(
        response.data.data,
      );
    } catch (error: unknown) {
      if (
        axios.isAxiosError<AuthErrorHttpResponse>(error)
      ) {
        if (error.code === 'ECONNABORTED') {
          throw new Error(
            'Login request timed out. Please try again.',
          );
        }

        if (!error.response) {
          throw new Error(
            'Unable to connect to the server. Check your network connection.',
          );
        }

        const backendMessage =
          error.response.data?.message;

        throw new Error(
          backendMessage ||
            'Login failed. Please try again.',
        );
      }

      throw new Error(
        'An unexpected login error occurred.',
      );
    }
  }
}