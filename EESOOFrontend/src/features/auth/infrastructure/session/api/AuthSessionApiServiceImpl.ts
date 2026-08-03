import axios from 'axios';
import axiosInstance from '../../../../../config/axiosInstance';
import { LogoutRequestDTO } from '../../../application/session/dto/LogoutRequestDTO';
import { RefreshTokenRequestDTO } from '../../../application/session/dto/RefreshTokenRequestDTO';
import { AuthSessionApiService } from '../../../application/session/interface/AuthSessionApiService';
import { LogoutApiResult } from '../../../application/session/model/LogoutApiResult';
import { RefreshTokenApiResult } from '../../../application/session/model/RefreshTokenApiResult';
import {
  LogoutHttpErrorResponseDTO,
  LogoutHttpSuccessResponseDTO,
} from '../dto/LogoutHttpResponseDTO';
import { RefreshTokenHttpErrorResponseDTO } from '../dto/RefreshTokenHttpErrorResponseDTO';
import { RefreshTokenHttpRequestDTO } from '../dto/RefreshTokenHttpRequestDTO';
import { RefreshTokenHttpSuccessResponseDTO } from '../dto/RefreshTokenHttpResponseDTO';
import { RefreshTokenHttpMapper } from '../mapper/RefreshTokenHttpMapper';

export class AuthSessionApiServiceImpl implements AuthSessionApiService {
  private readonly REFRESH_ENDPOINT = '/auth/refresh';
  private readonly LOGOUT_ENDPOINT = '/auth/logout';

  async refresh(
    request: RefreshTokenRequestDTO,
  ): Promise<RefreshTokenApiResult> {
    const httpRequest: RefreshTokenHttpRequestDTO =
      RefreshTokenHttpMapper.toHttpRequest(request);

    try {
      const response =
        await axiosInstance.post<RefreshTokenHttpSuccessResponseDTO>(
          this.REFRESH_ENDPOINT,
          httpRequest,
        );

      const refreshTokenResponse =
        RefreshTokenHttpMapper.toResponse(
          response.data.data,
        );

      return {
        ok: true,
        value: refreshTokenResponse,
      };
    } catch (error: unknown) {
      if (
        axios.isAxiosError<RefreshTokenHttpErrorResponseDTO>(
          error,
        )
      ) {
        if (error.response?.status === 401) {
          return {
            ok: false,
            reason: 'INVALID_SESSION',
            message:
              'Your session has expired. Please log in again.',
          };
        }

        if (error.code === 'ECONNABORTED') {
          return {
            ok: false,
            reason: 'TEMPORARY_FAILURE',
            message:
              'Session restoration timed out. Please try again.',
          };
        }

        if (!error.response) {
          return {
            ok: false,
            reason: 'TEMPORARY_FAILURE',
            message:
              'Unable to connect to the server. Check your network connection.',
          };
        }

        if (error.response.status >= 500) {
          return {
            ok: false,
            reason: 'TEMPORARY_FAILURE',
            message:
              'The server is temporarily unavailable. Please try again.',
          };
        }

        return {
          ok: false,
          reason: 'TEMPORARY_FAILURE',
          message:
            'Unable to restore your session right now.',
        };
      }

      return {
        ok: false,
        reason: 'TEMPORARY_FAILURE',
        message:
          'An unexpected session restoration error occurred.',
      };
    }
  }

  async logout(
    request: LogoutRequestDTO,
  ): Promise<LogoutApiResult> {
    try {
      await axiosInstance.post<LogoutHttpSuccessResponseDTO>(
        this.LOGOUT_ENDPOINT,
        undefined,
        {
          headers: {
            Authorization: `Bearer ${request.accessToken}`,
          },
        },
      );

      return {
        ok: true,
      };
    } catch (error: unknown) {
      if (
        axios.isAxiosError<LogoutHttpErrorResponseDTO>(
          error,
        )
      ) {
        if (error.response?.status === 401) {
          return {
            ok: false,
            reason: 'UNAUTHORIZED',
            message:
              'The access token is no longer authorized.',
          };
        }

        if (error.code === 'ECONNABORTED') {
          return {
            ok: false,
            reason: 'TEMPORARY_FAILURE',
            message:
              'Logout request timed out. Please try again.',
          };
        }

        if (!error.response) {
          return {
            ok: false,
            reason: 'TEMPORARY_FAILURE',
            message:
              'An internet connection is required to log out.',
          };
        }

        if (error.response.status >= 500) {
          return {
            ok: false,
            reason: 'TEMPORARY_FAILURE',
            message:
              'The server is temporarily unavailable. Please try again.',
          };
        }

        return {
          ok: false,
          reason: 'TEMPORARY_FAILURE',
          message:
            'Unable to log out right now. Please try again.',
        };
      }

      return {
        ok: false,
        reason: 'TEMPORARY_FAILURE',
        message:
          'An unexpected logout error occurred.',
      };
    }
  }
}
