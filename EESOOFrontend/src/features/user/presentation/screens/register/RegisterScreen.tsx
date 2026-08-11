import React, { useEffect, useRef, useState } from 'react';
import {
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  Text,
  TextInput,
  View,
} from 'react-native';
import { useRegisterViewModel } from './hooks/useProvideRegisterViewModel';
import { RegisterViewModel } from './viewModel/RegisterViewModel';
import { RegisterUserResponseDTO } from '../../../application/register/dto/RegisterUserResponseDTO';

type Props = {
  deviceReady: boolean;
  deviceMessage: string;
  onRegistered: (response: RegisterUserResponseDTO) => void;
  onLoginPress?: () => void;
};

export default function RegisterScreen({
  deviceReady,
  deviceMessage,
  onRegistered,
  onLoginPress,
}: Props) {
  const vm: RegisterViewModel = useRegisterViewModel(onRegistered);

  const [isPinVisible, setIsPinVisible] = useState(false);
  const [isConfirmPinVisible, setIsConfirmPinVisible] = useState(false);

  const pinInputRef = useRef<TextInput>(null);
  const confirmPinInputRef = useRef<TextInput>(null);

  const pinDigits = Array.from({ length: 4 }, (_, index) => vm.form.pin[index] ?? '');
  const confirmPinDigits = Array.from(
    { length: 4 },
    (_, index) => vm.form.confirmPin[index] ?? '',
  );

  const isConfirmStarted = vm.form.confirmPin.length > 0;
  const isPinMatched = vm.form.confirmPin.length === 4 && vm.form.confirmPin === vm.form.pin;
  const isPinMismatched = isConfirmStarted && !isPinMatched;

  useEffect(() => {
    if (vm.form.pin.length === 4) {
      confirmPinInputRef.current?.focus();
    }
  }, [vm.form.pin.length]);

  const isRegisterDisabled =
    !deviceReady ||
    vm.loading ||
    vm.form.firstName.length === 0 ||
    vm.form.lastName.length === 0 ||
    vm.form.phoneNumber.length !== 10 ||
    vm.form.pin.length !== 4 ||
    vm.form.confirmPin.length !== 4 ||
    !isPinMatched;

  return (
    <KeyboardAvoidingView
      className="flex-1 bg-slate-100"
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      keyboardVerticalOffset={Platform.OS === 'ios' ? 24 : 0}
    >
      <ScrollView
        className="flex-1"
        contentContainerClassName="flex-grow px-5 pb-8 pt-14"
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        {!!deviceMessage && (
          <View className="mb-4 rounded-xl border border-amber-300 bg-amber-50 px-4 py-3">
            <Text className="text-sm font-medium text-amber-800">{deviceMessage}</Text>
          </View>
        )}

        <Text className="text-3xl font-extrabold tracking-tight text-slate-900">
          Create Account
        </Text>
        <Text className="mt-2 text-sm text-slate-600">Fill in your details to continue</Text>

        <View className="mt-6 rounded-2xl border border-slate-200 bg-white p-4">
          <Text className="mb-2 text-sm font-semibold text-slate-700">First Name</Text>
          <TextInput
            value={vm.form.firstName}
            placeholder="First name"
            onChangeText={text => vm.onFieldChange('firstName', text)}
            className="rounded-xl border border-slate-300 bg-slate-50 px-4 py-3 text-base text-slate-900"
            autoCapitalize="none"
            autoCorrect={false}
            editable={deviceReady}
          />

          <Text className="mb-2 mt-4 text-sm font-semibold text-slate-700">Last Name</Text>
          <TextInput
            value={vm.form.lastName}
            placeholder="Last name"
            onChangeText={text => vm.onFieldChange('lastName', text)}
            className="rounded-xl border border-slate-300 bg-slate-50 px-4 py-3 text-base text-slate-900"
            autoCapitalize="none"
            autoCorrect={false}
            editable={deviceReady}
          />

          <Text className="mb-2 mt-4 text-sm font-semibold text-slate-700">Email</Text>
          <TextInput
            value={vm.form.email}
            placeholder="Email address"
            onChangeText={text => vm.onFieldChange('email', text)}
            className="rounded-xl border border-slate-300 bg-slate-50 px-4 py-3 text-base text-slate-900"
            keyboardType="email-address"
            autoCapitalize="none"
            autoCorrect={false}
            editable={deviceReady}
          />

          <Text className="mb-2 mt-4 text-sm font-semibold text-slate-700">Phone Number</Text>
          <View className="flex-row items-center gap-2">
            <View className="rounded-xl border border-slate-300 bg-slate-100 px-4 py-3">
              <Text className="text-base font-medium text-slate-700">+91</Text>
            </View>
            <TextInput
              value={vm.form.phoneNumber}
              placeholder="10-digit mobile number"
              onChangeText={text => vm.onFieldChange('phoneNumber', text)}
              className="flex-1 rounded-xl border border-slate-300 bg-slate-50 px-4 py-3 text-base text-slate-900"
              keyboardType="number-pad"
              maxLength={10}
              autoCapitalize="none"
              autoCorrect={false}
              editable={deviceReady}
            />
          </View>

          <View className="mt-4 flex-row items-center justify-between">
            <Text className="text-sm font-semibold text-slate-700">Set PIN</Text>
            <Pressable
              onPress={() => setIsPinVisible(prev => !prev)}
              className="rounded-lg border border-slate-300 bg-slate-100 px-3 py-1"
            >
              <Text className="text-xs font-medium text-slate-700">
                {isPinVisible ? 'Hide' : 'Show'}
              </Text>
            </Pressable>
          </View>

          <Pressable onPress={() => pinInputRef.current?.focus()} className="mt-2">
            <View className="flex-row justify-between">
              {pinDigits.map((digit, index) => {
                const isFilled = digit.length > 0;
                return (
                  <View
                    key={index}
                    className={`h-12 w-12 items-center justify-center rounded-xl border ${
                      isFilled ? 'border-teal-500 bg-teal-50' : 'border-slate-300 bg-white'
                    }`}
                  >
                    <Text className="text-lg font-semibold text-slate-900">
                      {digit ? (isPinVisible ? digit : '*') : ''}
                    </Text>
                  </View>
                );
              })}
            </View>
          </Pressable>

          <Text className="mt-2 text-xs text-slate-500">PIN must be exactly 4 digits</Text>

          <View className="mt-4 flex-row items-center justify-between">
            <Text className="text-sm font-semibold text-slate-700">Confirm PIN</Text>
            <Pressable
              onPress={() => setIsConfirmPinVisible(prev => !prev)}
              className="rounded-lg border border-slate-300 bg-slate-100 px-3 py-1"
            >
              <Text className="text-xs font-medium text-slate-700">
                {isConfirmPinVisible ? 'Hide' : 'Show'}
              </Text>
            </Pressable>
          </View>

          <Pressable onPress={() => confirmPinInputRef.current?.focus()} className="mt-2">
            <View className="flex-row justify-between">
              {confirmPinDigits.map((digit, index) => {
                const isFilled = digit.length > 0;
                return (
                  <View
                    key={index}
                    className={`h-12 w-12 items-center justify-center rounded-xl border ${
                      isFilled ? 'border-teal-500 bg-teal-50' : 'border-slate-300 bg-white'
                    }`}
                  >
                    <Text className="text-lg font-semibold text-slate-900">
                      {digit ? (isConfirmPinVisible ? digit : '*') : ''}
                    </Text>
                  </View>
                );
              })}
            </View>
          </Pressable>

          {isConfirmStarted ? (
            <Text
              className={`mt-2 text-sm font-semibold ${
                isPinMismatched ? 'text-rose-600' : 'text-emerald-600'
              }`}
            >
              {isPinMismatched ? 'PIN does not match' : 'PIN matched'}
            </Text>
          ) : null}

          {vm.errors.confirmPin ? (
            <Text className="mt-2 text-sm font-medium text-rose-600">{vm.errors.confirmPin}</Text>
          ) : null}

          {vm.errors.general ? (
            <Text className="mt-3 text-sm font-medium text-rose-600">{vm.errors.general}</Text>
          ) : null}

          <TextInput
            ref={pinInputRef}
            value={vm.form.pin}
            onChangeText={text => vm.onFieldChange('pin', text)}
            keyboardType="number-pad"
            maxLength={4}
            className="h-0 w-0 opacity-0"
            autoCorrect={false}
            editable={deviceReady}
          />

          <TextInput
            ref={confirmPinInputRef}
            value={vm.form.confirmPin}
            onChangeText={text => vm.onFieldChange('confirmPin', text)}
            keyboardType="number-pad"
            maxLength={4}
            className="h-0 w-0 opacity-0"
            autoCorrect={false}
            editable={deviceReady}
          />

          <Pressable
            onPress={vm.onClickRegister}
            disabled={isRegisterDisabled}
            className={`mt-6 items-center rounded-xl px-4 py-3 ${
              isRegisterDisabled ? 'bg-slate-300' : 'bg-teal-600'
            }`}
          >
            <Text className="text-base font-bold text-white">
              {vm.loading ? 'Creating...' : 'Create Account'}
            </Text>
          </Pressable>

          {onLoginPress ? (
            <Pressable
              onPress={onLoginPress}
              disabled={vm.loading}
              className="mt-3 items-center px-4 py-2"
            >
              <Text className="text-sm font-semibold text-teal-700">
                Already have an account? Login
              </Text>
            </Pressable>
          ) : null}
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}
