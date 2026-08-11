import {
  LoginErrors,
  LoginFormFields,
} from '../models/LoginFormModel';
import { PinResetUiState } from '../models/PinResetUiModel';

export interface LoginViewModel {
  form: LoginFormFields;
  errors: LoginErrors;
  resetState: PinResetUiState;
  recoveryPhoneNumber: string;
  issuedPin: string | null;
  loading: boolean;
  canSubmit: boolean;
  canConfirmMobile: boolean;
  canIssuePin: boolean;
  onFieldChange(
    field: keyof LoginFormFields,
    value: string,
  ): void;
  onRecoveryPhoneChange(value: string): void;
  onLoginPress(): Promise<void>;
  onConfirmMobilePress(): Promise<void>;
  onIssuePinPress(): Promise<void>;
}
