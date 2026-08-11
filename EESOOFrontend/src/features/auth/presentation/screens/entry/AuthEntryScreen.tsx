import React from 'react';
import { Pressable, Text, View } from 'react-native';

interface AuthEntryScreenProps {
  onLoginPress: () => void;
  onRegisterPress: () => void;
}

export default function AuthEntryScreen({
  onLoginPress,
  onRegisterPress,
}: AuthEntryScreenProps) {
  return (
    <View className="flex-1 justify-center bg-slate-100 px-6">
      <Text className="text-3xl font-extrabold text-slate-900">
        Welcome to EESOO
      </Text>
      <Text className="mt-3 text-base text-slate-600">
        Log in if you already have an account, or create a new one.
      </Text>

      <Pressable
        onPress={onLoginPress}
        className="mt-8 items-center rounded-xl bg-teal-600 px-4 py-3"
      >
        <Text className="text-base font-bold text-white">
          Login
        </Text>
      </Pressable>

      <Pressable
        onPress={onRegisterPress}
        className="mt-3 items-center rounded-xl border border-teal-600 bg-white px-4 py-3"
      >
        <Text className="text-base font-bold text-teal-700">
          Create account
        </Text>
      </Pressable>
    </View>
  );
}
