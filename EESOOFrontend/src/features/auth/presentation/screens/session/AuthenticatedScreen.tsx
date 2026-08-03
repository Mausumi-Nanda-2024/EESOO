import React, { useState } from 'react';
import { Pressable, Text, View } from 'react-native';
import { AuthenticatedIdentity } from '../../../application/login/model/AuthenticatedIdentity';

interface AuthenticatedScreenProps {
  identity: AuthenticatedIdentity;
  onLogout: () => Promise<string | null>;
}

export default function AuthenticatedScreen({
  identity,
  onLogout,
}: AuthenticatedScreenProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleLogout(): Promise<void> {
    if (loading) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const failureMessage = await onLogout();

      if (failureMessage) {
        setError(failureMessage);
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <View className="flex-1 justify-center bg-slate-100 px-6">
      <Text className="text-3xl font-extrabold text-slate-900">
        You are logged in
      </Text>
      <Text className="mt-3 text-base text-slate-600">
        Welcome, {identity.username}.
      </Text>

      {error ? (
        <View className="mt-5 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3">
          <Text className="text-sm font-medium text-rose-700">
            {error}
          </Text>
        </View>
      ) : null}

      <Pressable
        onPress={handleLogout}
        disabled={loading}
        className={`mt-8 items-center rounded-xl px-4 py-3 ${
          loading ? 'bg-slate-300' : 'bg-rose-600'
        }`}
      >
        <Text className="text-base font-bold text-white">
          {loading ? 'Logging out...' : 'Logout'}
        </Text>
      </Pressable>
    </View>
  );
}
