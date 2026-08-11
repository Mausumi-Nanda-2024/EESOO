import { useState } from "react";
import { useRegisterForm } from "../hooks/useRegisterForm";
import { RegisterErrors } from "../models/RegisterFormModel";
import { RegisterViewModel } from "./RegisterViewModel";
import { RegisterUserUseCase } from "../../../../application/register/usecase/RegisterUserUseCase";
import { RegisterUserResponseDTO } from "../../../../application/register/dto/RegisterUserResponseDTO";


export function useRegisterViewModel(
  registerUserUseCase: RegisterUserUseCase,
  onRegistered?: (response: RegisterUserResponseDTO) => void
): RegisterViewModel {

  // form + live UI filtering handled here
  const { form, updateField } = useRegisterForm();

  // UI state
  const [errors, setErrors] = useState<RegisterErrors>({});
  const [loading, setLoading] = useState<boolean>(false);

  // user typing handler
  function onFieldChange(field: keyof typeof form, value: string) {
    updateField(field, value);

    // clear only that field error while typing
    setErrors(prev => ({
      ...prev,
      [field]: undefined,
      general: undefined,
    }));
  }

  // register button action
  async function onClickRegister(): Promise<void> {
    setLoading(true);
    setErrors({});

    if (form.confirmPin.length !== 4 || form.confirmPin !== form.pin) {
    setErrors({
      confirmPin:
        form.confirmPin.length !== 4
          ? 'Confirm PIN must be 4 digits.'
          : 'Confirm PIN does not match.',
    });
    setLoading(false);
    return;
  }

    const result = await registerUserUseCase.registerNewUser({
      firstName: form.firstName,
      lastName: form.lastName,
      phoneNumber: form.phoneNumber,
      pin: form.pin,
      email: form.email,
    });

    // domain validation errors returned
    if (!result.success) {
      const fieldErrors: RegisterErrors = {};

      for (const error of result.errors) {
        if (error.field === "general") {
          fieldErrors.general = error.message;
        } else {
          fieldErrors[error.field as keyof typeof form] = error.message;
        }
      }

      setErrors(fieldErrors);
      setLoading(false);
      return;
    }

    // success case
    setLoading(false);
    onRegistered?.(result.data);
  }

  return {
    form,
    errors,
    loading,
    onFieldChange,
    onClickRegister,
  };
}
