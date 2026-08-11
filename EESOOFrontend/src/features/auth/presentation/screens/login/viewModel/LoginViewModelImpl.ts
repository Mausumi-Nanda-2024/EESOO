import { useEffect, useRef, useState } from 'react';
import { AuthOperationFailure } from '../../../../application/common/model/AuthOperationResult';
import { LoginResult } from '../../../../application/login/model/LoginResult';
import { LoginUserUseCase } from '../../../../application/login/usecase/LoginUserUseCase';
import { ConfirmPinResetMobileUseCase } from '../../../../application/pinReset/usecase/ConfirmPinResetMobileUseCase';
import { IssuePinResetUseCase } from '../../../../application/pinReset/usecase/IssuePinResetUseCase';
import { useLoginForm } from '../hooks/useLoginForm';
import {
  LoginErrors,
  LoginFormFields,
} from '../models/LoginFormModel';
import {
  PinResetUiState,
  resolvePinResetLoginTransition,
} from '../models/PinResetUiModel';
import { LoginViewModel } from './LoginViewModel';

type LoginOperation =
  | 'LOGIN'
  | 'CONFIRMING_MOBILE'
  | 'ISSUING_PIN'
  | null;

function formatPhoneNumberInput(text: string): string {
  return text.replace(/[^0-9]/g, '').slice(0, 10);
}

export function useLoginViewModel(
  loginUserUseCase: LoginUserUseCase,
  confirmPinResetMobileUseCase: ConfirmPinResetMobileUseCase,
  issuePinResetUseCase: IssuePinResetUseCase,
  initialPhoneNumber: string,
  onLoggedIn: (result: LoginResult) => void,
): LoginViewModel {
  const { form, updateField, clearPin } = useLoginForm(
    initialPhoneNumber,
  );
  const [errors, setErrors] = useState<LoginErrors>({});
  const [operation, setOperation] =
    useState<LoginOperation>(null);
  const operationRef = useRef<LoginOperation>(null);
  const previousInitialPhoneNumber = useRef(
    initialPhoneNumber,
  );
  const [resetState, setResetState] =
    useState<PinResetUiState>('LOGIN');
  const [pinResetAttemptId, setPinResetAttemptId] =
    useState<string | null>(null);
  const [recoveryPhoneNumber, setRecoveryPhoneNumber] =
    useState('');
  const [issuedPin, setIssuedPin] = useState<string | null>(
    null,
  );

  const loading = operation !== null;
  const canSubmit =
    !loading &&
    form.phoneNumber.length === 10 &&
    form.pin.length === 4 &&
    ![
      'RESET_AVAILABLE',
      'CONFIRMING_MOBILE',
      'MOBILE_CONFIRMED',
      'ISSUING_PIN',
    ].includes(resetState);
  const canConfirmMobile =
    !loading &&
    resetState === 'RESET_AVAILABLE' &&
    recoveryPhoneNumber.length === 10 &&
    pinResetAttemptId !== null;
  const canIssuePin =
    !loading &&
    resetState === 'MOBILE_CONFIRMED' &&
    pinResetAttemptId !== null;

  useEffect(() => {
    if (
      previousInitialPhoneNumber.current === initialPhoneNumber
    ) {
      return;
    }

    previousInitialPhoneNumber.current = initialPhoneNumber;
    operationRef.current = null;
    setOperation(null);
    setErrors({});
    setResetState('LOGIN');
    setPinResetAttemptId(null);
    setRecoveryPhoneNumber('');
    setIssuedPin(null);
  }, [initialPhoneNumber]);

  function beginOperation(nextOperation: LoginOperation): boolean {
    if (operationRef.current !== null) {
      return false;
    }

    operationRef.current = nextOperation;
    setOperation(nextOperation);
    return true;
  }

  function finishOperation(): void {
    operationRef.current = null;
    setOperation(null);
  }

  function clearRecoveryState(): void {
    setResetState('LOGIN');
    setPinResetAttemptId(null);
    setRecoveryPhoneNumber('');
    setIssuedPin(null);
  }

  function returnToSafeLogin(message: string): void {
    clearRecoveryState();
    clearPin();
    setErrors({ general: message });
  }

  function handleRecoveryUnavailable(
    error: AuthOperationFailure,
  ): boolean {
    if (error.code !== 'PIN_RESET_NOT_AVAILABLE') {
      return false;
    }

    returnToSafeLogin(
      'This PIN reset attempt is no longer available. Please start again.',
    );
    return true;
  }

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

  function onRecoveryPhoneChange(value: string): void {
    setRecoveryPhoneNumber(formatPhoneNumberInput(value));
    setErrors(previousErrors => ({
      ...previousErrors,
      recoveryPhoneNumber: undefined,
      general: undefined,
    }));
  }

  async function onLoginPress(): Promise<void> {
    if (operationRef.current !== null) {
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

    if (!beginOperation('LOGIN')) {
      return;
    }
    setErrors({});

    try {
      const result = await loginUserUseCase.loginUser({
        phoneNumber: form.phoneNumber,
        pin: form.pin,
      });

      if (result.ok) {
        clearPin();
        clearRecoveryState();
        finishOperation();
        onLoggedIn(result.value);
        return;
      }

      const transition = resolvePinResetLoginTransition(
        result.error,
      );

      if (transition) {
        setResetState(transition.state);

        if (transition.pinResetAttemptId) {
          setPinResetAttemptId(
            transition.pinResetAttemptId,
          );
        }

        if (transition.state === 'RESET_AVAILABLE') {
          setRecoveryPhoneNumber('');
          setIssuedPin(null);
        }

        setErrors({ general: result.error.message });
      } else {
        setErrors({ general: result.error.message });
      }

      clearPin();
    } catch {
      setErrors({
        general:
          'An unexpected login error occurred. Please try again.',
      });
      clearPin();
    } finally {
      finishOperation();
    }
  }

  async function onConfirmMobilePress(): Promise<void> {
    if (!canConfirmMobile || !pinResetAttemptId) {
      if (recoveryPhoneNumber.length !== 10) {
        setErrors({
          recoveryPhoneNumber:
            'Enter the complete registered mobile number.',
        });
      }
      return;
    }

    if (!beginOperation('CONFIRMING_MOBILE')) {
      return;
    }
    setResetState('CONFIRMING_MOBILE');
    setErrors({});

    try {
      const result =
        await confirmPinResetMobileUseCase.execute(
          pinResetAttemptId,
          recoveryPhoneNumber,
        );

      if (result.ok) {
        setPinResetAttemptId(
          result.value.pinResetAttemptId,
        );
        setResetState('MOBILE_CONFIRMED');
        return;
      }

      if (handleRecoveryUnavailable(result.error)) {
        return;
      }

      setResetState('RESET_AVAILABLE');

      if (
        result.error.code ===
        'PIN_RESET_MOBILE_NOT_VERIFIED'
      ) {
        setErrors({
          recoveryPhoneNumber:
            'The registered mobile number could not be verified.',
        });
        return;
      }

      setErrors({
        recoveryPhoneNumber:
          result.error.fieldErrors?.phoneNumber,
        general:
          result.error.fieldErrors?.phoneNumber
            ? undefined
            : result.error.message,
      });
    } catch {
      setResetState('RESET_AVAILABLE');
      setErrors({
        general:
          'An unexpected mobile confirmation error occurred.',
      });
    } finally {
      finishOperation();
    }
  }

  async function onIssuePinPress(): Promise<void> {
    if (!canIssuePin || !pinResetAttemptId) {
      return;
    }

    if (!beginOperation('ISSUING_PIN')) {
      return;
    }
    setResetState('ISSUING_PIN');
    setErrors({});

    try {
      const result = await issuePinResetUseCase.execute(
        pinResetAttemptId,
      );

      if (result.ok) {
        setPinResetAttemptId(
          result.value.pinResetAttemptId,
        );
        setIssuedPin(result.value.newPin);
        setResetState('PIN_ISSUED');
        clearPin();
        return;
      }

      if (handleRecoveryUnavailable(result.error)) {
        return;
      }

      setResetState('MOBILE_CONFIRMED');
      setErrors({ general: result.error.message });
    } catch {
      setResetState('MOBILE_CONFIRMED');
      setErrors({
        general:
          'An unexpected PIN reset error occurred.',
      });
    } finally {
      finishOperation();
    }
  }

  return {
    form,
    errors,
    resetState,
    recoveryPhoneNumber,
    issuedPin,
    loading,
    canSubmit,
    canConfirmMobile,
    canIssuePin,
    onFieldChange,
    onRecoveryPhoneChange,
    onLoginPress,
    onConfirmMobilePress,
    onIssuePinPress,
  };
}
