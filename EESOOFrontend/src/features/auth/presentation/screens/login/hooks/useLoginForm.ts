import { useEffect, useState } from 'react';
import { LoginFormFields } from '../models/LoginFormModel';

function formatPhoneNumberInput(text: string): string {
  return text.replace(/[^0-9]/g, '').slice(0, 10);
}

function formatPinInput(text: string): string {
  return text.replace(/[^0-9]/g, '').slice(0, 4);
}

const fieldFormatters: Record<
  keyof LoginFormFields,
  (value: string) => string
> = {
  phoneNumber: formatPhoneNumberInput,
  pin: formatPinInput,
};

export function useLoginForm(initialPhoneNumber = '') {
  const [form, setForm] = useState<LoginFormFields>(() => ({
    phoneNumber: formatPhoneNumberInput(initialPhoneNumber),
    pin: '',
  }));

  useEffect(() => {
    setForm({
      phoneNumber: formatPhoneNumberInput(
        initialPhoneNumber,
      ),
      pin: '',
    });
  }, [initialPhoneNumber]);

  function updateField(
    field: keyof LoginFormFields,
    value: string,
  ): void {
    const formattedValue = fieldFormatters[field](value);

    setForm(previousForm => ({
      ...previousForm,
      [field]: formattedValue,
    }));
  }

  function clearPin(): void {
    setForm(previousForm => ({
      ...previousForm,
      pin: '',
    }));
  }

  return {
    form,
    updateField,
    clearPin,
  };
}
