import axios from 'axios';
import {
  AuthOperationFailure,
  PinResetStatus,
} from '../../../application/common/model/AuthOperationResult';
import { AuthHttpErrorResponseDTO } from '../dto/AuthHttpErrorResponseDTO';

const RESET_STATUSES: PinResetStatus[] = [
  'FIRST_FAILURE',
  'RESET_AVAILABLE',
  'MOBILE_CONFIRMED',
  'PIN_ISSUED',
];

const RESET_METADATA_FIELDS = new Set([
  'code',
  'resetStatus',
  'remainingAttempts',
  'pinResetAttemptId',
]);

function toResetStatus(value: unknown): PinResetStatus | undefined {
  return typeof value === 'string' &&
    RESET_STATUSES.includes(value as PinResetStatus)
    ? (value as PinResetStatus)
    : undefined;
}

function toFieldErrors(
  data: AuthHttpErrorResponseDTO['data'],
): Record<string, string> | undefined {
  if (!data) {
    return undefined;
  }

  const entries = Object.entries(data).filter(
    ([field, value]) =>
      !RESET_METADATA_FIELDS.has(field) &&
      typeof value === 'string',
  ) as Array<[string, string]>;

  return entries.length > 0
    ? Object.fromEntries(entries)
    : undefined;
}

export class AuthHttpErrorMapper {
  static fromUnknown(
    error: unknown,
    fallbackMessage: string,
  ): AuthOperationFailure {
    if (!axios.isAxiosError<AuthHttpErrorResponseDTO>(error)) {
      return {
        reason: 'TEMPORARY_FAILURE',
        message: fallbackMessage,
      };
    }

    if (error.code === 'ECONNABORTED') {
      return {
        reason: 'TEMPORARY_FAILURE',
        message: 'The request timed out. Please try again.',
      };
    }

    if (!error.response) {
      return {
        reason: 'TEMPORARY_FAILURE',
        message:
          'Unable to connect to the server. Check your network connection.',
      };
    }

    const httpStatus = error.response.status;
    const responseBody = error.response.data;
    const responseData = responseBody?.data;

    if (httpStatus >= 500) {
      return {
        reason: 'TEMPORARY_FAILURE',
        message:
          'The server is temporarily unavailable. Please try again.',
        httpStatus,
      };
    }

    return {
      reason: 'BACKEND_REJECTION',
      message: responseBody?.message || fallbackMessage,
      httpStatus,
      code:
        typeof responseData?.code === 'string'
          ? responseData.code
          : undefined,
      resetStatus: toResetStatus(responseData?.resetStatus),
      remainingAttempts:
        typeof responseData?.remainingAttempts === 'number'
          ? responseData.remainingAttempts
          : undefined,
      pinResetAttemptId:
        typeof responseData?.pinResetAttemptId === 'string'
          ? responseData.pinResetAttemptId
          : undefined,
      fieldErrors: toFieldErrors(responseData),
    };
  }
}
