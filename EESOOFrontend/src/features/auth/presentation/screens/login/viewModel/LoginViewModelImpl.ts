import { useState } from 'react';
import { LoginResult } from '../../../../application/login/model/LoginResult';
import { LoginUserUseCase } from '../../../../application/login/usecase/LoginUserUseCase';
import { useLoginForm } from '../hooks/useLoginForm';
import {
  LoginErrors,
  LoginFormFields,
} from '../models/LoginFormModel';
import { LoginViewModel } from './LoginViewModel';

export function useLoginViewModel(
  loginUserUseCase: LoginUserUseCase,
  initialPhoneNumber: string,
  onLoggedIn: (result: LoginResult) => void,
): LoginViewModel {
  const { form, updateField, clearPin } = useLoginForm(
    initialPhoneNumber,
  );
  const [errors, setErrors] = useState<LoginErrors>({});
  const [loading, setLoading] = useState(false);

  const canSubmit =
    !loading &&
    form.phoneNumber.length === 10 &&
    form.pin.length === 4;

  function onFieldChange(
    field: keyof LoginFormFields,
    value: string,
  ): void {
    updateField(field, value);
    setErrors(previousErrors => ({
      ...previousErrors,
      [field]: undefined,
      general: undefined,
    }));
  }

  async function onLoginPress(): Promise<void> {
    if (loading) {
      return;
    }

    const validationErrors: LoginErrors = {};

    if (form.phoneNumber.length !== 10) {
      validationErrors.phoneNumber =
        'Enter a valid 10-digit phone number.';
    }

    if (form.pin.length !== 4) {
      validationErrors.pin = 'Enter your 4-digit PIN.';
    }

    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    setErrors({});

    try {
      const result = await loginUserUseCase.loginUser({
        phoneNumber: form.phoneNumber,
        pin: form.pin,
      });

      clearPin();
      setLoading(false);
      onLoggedIn(result);
    } catch (error: unknown) {
      const message =
        error instanceof Error
          ? error.message
          : 'Unable to log in. Please try again.';

      setErrors({ general: message });
      clearPin();
      setLoading(false);
    }
  }

  return {
    form,
    errors,
    loading,
    canSubmit,
    onFieldChange,
    onLoginPress,
  };
}
