import React, { useState } from 'react';
import {
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
          {isKnownIdentity ? 'Welcome back' : 'Login'}
        </Text>
        <Text className="mt-2 text-sm text-slate-600">
          Enter your four-digit PIN to continue.
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
                Phone number
              </Text>
              <View className="mt-2 rounded-xl border border-slate-200 bg-slate-100 px-4 py-3">
                <Text className="text-base text-slate-700">
                  {maskPhoneNumber(knownIdentity.phoneNumber)}
                </Text>
              </View>
            </>
          ) : (
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
          )}

          <View className="mt-5 flex-row items-center justify-between">
            <Text className="text-sm font-semibold text-slate-700">
              PIN
            </Text>
            <Pressable
              onPress={() =>
                setIsPinVisible(previousValue => !previousValue)
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
            onChangeText={value => vm.onFieldChange('pin', value)}
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

          {vm.errors.general ? (
            <View className="mt-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3">
              <Text className="text-sm font-medium text-rose-700">
                {vm.errors.general}
              </Text>
            </View>
          ) : null}

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

          {!isKnownIdentity ? (
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
          )}
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}
