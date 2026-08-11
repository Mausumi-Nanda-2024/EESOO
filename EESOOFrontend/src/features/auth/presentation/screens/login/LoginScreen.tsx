import React, { useEffect, useState } from 'react';
import {
  Alert,
  BackHandler,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  Text,
  TextInput,
  View,
} from 'react-native';
import { LoginResult } from '../../../application/login/model/LoginResult';
import { LoginMode } from '../../../application/login/model/LoginMode';
import { useProvideLoginViewModel } from './hooks/useProvideLoginViewModel';
import { isProtectedPinResetState } from './models/PinResetUiModel';

interface KnownLoginIdentity {
  userId: string;
  username: string;
  phoneNumber: string;
}

interface LoginScreenProps {
  mode: LoginMode;
  knownIdentity?: KnownLoginIdentity;
  onLoggedIn: (result: LoginResult) => void;
  onCreateAccountPress: () => void;
  onUseDifferentAccountPress: () => void;
}

function maskPhoneNumber(phoneNumber: string): string {
  if (phoneNumber.length <= 4) {
    return phoneNumber;
  }

  return `${'*'.repeat(
    phoneNumber.length - 4,
  )}${phoneNumber.slice(-4)}`;
}

export default function LoginScreen({
  mode,
  knownIdentity,
  onLoggedIn,
  onCreateAccountPress,
  onUseDifferentAccountPress,
}: LoginScreenProps) {
  const [isPinVisible, setIsPinVisible] = useState(false);
  const isKnownIdentity =
    mode === 'KNOWN_IDENTITY' && knownIdentity !== undefined;
  const initialPhoneNumber = isKnownIdentity
    ? knownIdentity.phoneNumber
    : '';
  const vm = useProvideLoginViewModel(
    initialPhoneNumber,
    onLoggedIn,
  );
  const isRecoveryProtected = isProtectedPinResetState(
    vm.resetState,
  );
  const showLoginInputs = [
    'LOGIN',
    'FIRST_FAILURE_WARNING',
    'PIN_ISSUED',
  ].includes(vm.resetState);
  const showMobileConfirmation = [
    'RESET_AVAILABLE',
    'CONFIRMING_MOBILE',
  ].includes(vm.resetState);
  const showIssueAction = [
    'MOBILE_CONFIRMED',
    'ISSUING_PIN',
  ].includes(vm.resetState);

  useEffect(() => {
    if (Platform.OS !== 'android' || !isRecoveryProtected) {
      return undefined;
    }

    const subscription = BackHandler.addEventListener(
      'hardwareBackPress',
      () => {
        Alert.alert(
          'PIN reset in progress',
          vm.resetState === 'PIN_ISSUED'
            ? 'Your replacement PIN is available only on this screen until you log in.'
            : 'Finish the PIN reset before leaving this screen.',
          [{ text: 'Stay here' }],
        );
        return true;
      },
    );

    return () => subscription.remove();
  }, [isRecoveryProtected, vm.resetState]);

  return (
    <KeyboardAvoidingView
      className="flex-1 bg-slate-100"
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView
        contentContainerClassName="flex-grow justify-center px-5 py-10"
        keyboardShouldPersistTaps="handled"
      >
        <Text className="text-3xl font-extrabold text-slate-900">
          {isRecoveryProtected
            ? 'Reset your PIN'
            : isKnownIdentity
              ? 'Welcome back'
              : 'Login'}
        </Text>
        <Text className="mt-2 text-sm text-slate-600">
          {showMobileConfirmation
            ? 'Enter the complete registered mobile number to confirm your account.'
            : showIssueAction
              ? 'Your mobile number is confirmed. Request your replacement PIN.'
              : vm.resetState === 'PIN_ISSUED'
                ? 'Enter the replacement PIN below to continue.'
                : 'Enter your four-digit PIN to continue.'}
        </Text>

        <View className="mt-6 rounded-2xl border border-slate-200 bg-white p-5">
          {isKnownIdentity ? (
            <>
              <Text className="text-sm font-semibold text-slate-700">
                Username
              </Text>
              <View className="mt-2 rounded-xl border border-slate-200 bg-slate-100 px-4 py-3">
                <Text className="text-base font-semibold text-slate-900">
                  {knownIdentity.username}
                </Text>
              </View>

              <Text className="mt-4 text-sm font-semibold text-slate-700">
                Account phone number
              </Text>
              <View className="mt-2 rounded-xl border border-slate-200 bg-slate-100 px-4 py-3">
                <Text className="text-base text-slate-700">
                  {maskPhoneNumber(knownIdentity.phoneNumber)}
                </Text>
              </View>
            </>
          ) : showLoginInputs ? (
            <>
              <Text className="text-sm font-semibold text-slate-700">
                Phone number
              </Text>
              <TextInput
                value={vm.form.phoneNumber}
                onChangeText={value =>
                  vm.onFieldChange('phoneNumber', value)
                }
                placeholder="10-digit mobile number"
                keyboardType="number-pad"
                maxLength={10}
                autoCorrect={false}
                autoCapitalize="none"
                editable={!vm.loading}
                className="mt-2 rounded-xl border border-slate-300 bg-slate-50 px-4 py-3 text-base text-slate-900"
              />
              {vm.errors.phoneNumber ? (
                <Text className="mt-2 text-sm font-medium text-rose-600">
                  {vm.errors.phoneNumber}
                </Text>
              ) : null}
            </>
          ) : (
            <>
              <Text className="text-sm font-semibold text-slate-700">
                Account phone number
              </Text>
              <View className="mt-2 rounded-xl border border-slate-200 bg-slate-100 px-4 py-3">
                <Text className="text-base text-slate-700">
                  {maskPhoneNumber(vm.form.phoneNumber)}
                </Text>
              </View>
            </>
          )}

          {vm.resetState === 'PIN_ISSUED' ? (
            <View className="mt-5 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-4">
              <Text className="text-sm font-bold text-emerald-800">
                PIN reset successful
              </Text>
              {vm.issuedPin ? (
                <>
                  <Text className="mt-2 text-sm text-emerald-800">
                    Your replacement PIN is:
                  </Text>
                  <Text
                    selectable={false}
                    className="mt-2 text-center text-3xl font-extrabold tracking-widest text-emerald-950"
                  >
                    {vm.issuedPin}
                  </Text>
                </>
              ) : (
                <Text className="mt-2 text-sm text-emerald-800">
                  A replacement PIN has already been issued. Enter the PIN shown earlier.
                </Text>
              )}
              <Text className="mt-3 text-xs text-emerald-800">
                This PIN is kept only on this screen and will be cleared after login.
              </Text>
            </View>
          ) : null}

          {showLoginInputs ? (
            <>
              <View className="mt-5 flex-row items-center justify-between">
                <Text className="text-sm font-semibold text-slate-700">
                  PIN
                </Text>
                <Pressable
                  onPress={() =>
                    setIsPinVisible(
                      previousValue => !previousValue,
                    )
                  }
                  disabled={vm.loading}
                >
                  <Text className="text-sm font-semibold text-teal-700">
                    {isPinVisible ? 'Hide' : 'Show'}
                  </Text>
                </Pressable>
              </View>
              <TextInput
                value={vm.form.pin}
                onChangeText={value =>
                  vm.onFieldChange('pin', value)
                }
                placeholder="4-digit PIN"
                keyboardType="number-pad"
                maxLength={4}
                secureTextEntry={!isPinVisible}
                autoCorrect={false}
                editable={!vm.loading}
                onSubmitEditing={vm.onLoginPress}
                className="mt-2 rounded-xl border border-slate-300 bg-slate-50 px-4 py-3 text-base text-slate-900"
              />
              {vm.errors.pin ? (
                <Text className="mt-2 text-sm font-medium text-rose-600">
                  {vm.errors.pin}
                </Text>
              ) : null}
            </>
          ) : null}

          {showMobileConfirmation ? (
            <>
              <Text className="mt-5 text-sm font-semibold text-slate-700">
                Confirm registered mobile number
              </Text>
              <TextInput
                value={vm.recoveryPhoneNumber}
                onChangeText={vm.onRecoveryPhoneChange}
                placeholder="Complete 10-digit mobile number"
                keyboardType="number-pad"
                maxLength={10}
                autoCorrect={false}
                autoCapitalize="none"
                editable={!vm.loading}
                className="mt-2 rounded-xl border border-slate-300 bg-slate-50 px-4 py-3 text-base text-slate-900"
              />
              {vm.errors.recoveryPhoneNumber ? (
                <Text className="mt-2 text-sm font-medium text-rose-600">
                  {vm.errors.recoveryPhoneNumber}
                </Text>
              ) : null}
            </>
          ) : null}

          {showIssueAction ? (
            <View className="mt-5 rounded-xl border border-teal-200 bg-teal-50 px-4 py-3">
              <Text className="text-sm font-semibold text-teal-800">
                Mobile number confirmed successfully
              </Text>
              <Text className="mt-1 text-xs text-teal-700">
                Press Reset PIN once to generate and save your replacement PIN.
              </Text>
            </View>
          ) : null}

          {vm.errors.general ? (
            <View className="mt-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3">
              <Text className="text-sm font-medium text-rose-700">
                {vm.errors.general}
              </Text>
            </View>
          ) : null}

          {showLoginInputs ? (
            <Pressable
              onPress={vm.onLoginPress}
              disabled={!vm.canSubmit}
              className={`mt-6 items-center rounded-xl px-4 py-3 ${
                vm.canSubmit ? 'bg-teal-600' : 'bg-slate-300'
              }`}
            >
              <Text className="text-base font-bold text-white">
                {vm.loading ? 'Logging in...' : 'Login'}
              </Text>
            </Pressable>
          ) : null}

          {showMobileConfirmation ? (
            <Pressable
              onPress={vm.onConfirmMobilePress}
              disabled={!vm.canConfirmMobile}
              className={`mt-6 items-center rounded-xl px-4 py-3 ${
                vm.canConfirmMobile
                  ? 'bg-teal-600'
                  : 'bg-slate-300'
              }`}
            >
              <Text className="text-base font-bold text-white">
                {vm.resetState === 'CONFIRMING_MOBILE'
                  ? 'Confirming...'
                  : 'Confirm mobile number'}
              </Text>
            </Pressable>
          ) : null}

          {showIssueAction ? (
            <Pressable
              onPress={vm.onIssuePinPress}
              disabled={!vm.canIssuePin}
              className={`mt-6 items-center rounded-xl px-4 py-3 ${
                vm.canIssuePin
                  ? 'bg-teal-600'
                  : 'bg-slate-300'
              }`}
            >
              <Text className="text-base font-bold text-white">
                {vm.resetState === 'ISSUING_PIN'
                  ? 'Resetting PIN...'
                  : 'Reset PIN'}
              </Text>
            </Pressable>
          ) : null}

          {!isRecoveryProtected ? (
            !isKnownIdentity ? (
              <Pressable
                onPress={onCreateAccountPress}
                disabled={vm.loading}
                className="mt-4 items-center px-4 py-2"
              >
                <Text className="text-sm font-semibold text-teal-700">
                  Create a new account
                </Text>
              </Pressable>
            ) : (
              <Pressable
                onPress={onUseDifferentAccountPress}
                disabled={vm.loading}
                className="mt-4 items-center px-4 py-2"
              >
                <Text className="text-sm font-semibold text-teal-700">
                  Use a different account
                </Text>
              </Pressable>
            )
          ) : null}
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}
