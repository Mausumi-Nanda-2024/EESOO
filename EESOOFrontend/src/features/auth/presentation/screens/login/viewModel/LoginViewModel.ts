import {
  LoginErrors,
  LoginFormFields,
} from '../models/LoginFormModel';

export interface LoginViewModel {
  form: LoginFormFields;
  errors: LoginErrors;
  loading: boolean;
  canSubmit: boolean;
  onFieldChange(
    field: keyof LoginFormFields,
    value: string,
  ): void;
  onLoginPress(): Promise<void>;
}
