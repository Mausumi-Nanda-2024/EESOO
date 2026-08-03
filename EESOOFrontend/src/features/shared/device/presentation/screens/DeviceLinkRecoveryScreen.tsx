import React, { useState } from 'react';
import { Pressable, Text, View } from 'react-native';

export interface DeviceRecoveryAttemptResult {
  recovered: boolean;
  message?: string;
}

interface DeviceLinkRecoveryScreenProps {
  reason: string | null;
  onRetry: () => Promise<DeviceRecoveryAttemptResult>;
  onLogout: () => Promise<string | null>;
}

export default function DeviceLinkRecoveryScreen({
  reason,
  onRetry,
  onLogout,
}: DeviceLinkRecoveryScreenProps) {
  const [activeAction, setActiveAction] = useState<
    'retry' | 'logout' | null
  >(null);
  const [message, setMessage] = useState<string | null>(null);

  async function handleRetry(): Promise<void> {
    if (activeAction) {
      return;
    }

    setActiveAction('retry');
    setMessage(null);

    try {
      const result = await onRetry();

      if (!result.recovered) {
        setMessage(
          result.message ??
            'The device is still not linked. Please try again.',
        );
      }
    } finally {
      setActiveAction(null);
    }
  }

  async function handleLogout(): Promise<void> {
    if (activeAction) {
      return;
    }

    setActiveAction('logout');
    setMessage(null);

    try {
      const failureMessage = await onLogout();

      if (failureMessage) {
        setMessage(failureMessage);
      }
    } finally {
      setActiveAction(null);
    }
  }

  return (
    <View className="flex-1 justify-center bg-slate-100 px-6">
      <Text className="text-3xl font-extrabold text-slate-900">
        Device link needs attention
      </Text>
      <Text className="mt-3 text-base text-slate-600">
        Your login succeeded, but this installation is not linked to your account.
      </Text>

      {reason ? (
        <View className="mt-5 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3">
          <Text className="text-sm font-semibold text-amber-800">
            Reason: {reason}
          </Text>
        </View>
      ) : null}

      {message ? (
        <View className="mt-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3">
          <Text className="text-sm font-medium text-rose-700">
            {message}
          </Text>
        </View>
      ) : null}

      <Pressable
        onPress={handleRetry}
        disabled={activeAction !== null}
        className={`mt-8 items-center rounded-xl px-4 py-3 ${
          activeAction ? 'bg-slate-300' : 'bg-teal-600'
        }`}
      >
        <Text className="text-base font-bold text-white">
          {activeAction === 'retry'
            ? 'Checking...'
            : 'Recheck device link'}
        </Text>
      </Pressable>

      <Pressable
        onPress={handleLogout}
        disabled={activeAction !== null}
        className="mt-3 items-center px-4 py-3"
      >
        <Text className="text-base font-semibold text-rose-700">
          {activeAction === 'logout'
            ? 'Logging out...'
            : 'Logout and sign in again'}
        </Text>
      </Pressable>
    </View>
  );
}
