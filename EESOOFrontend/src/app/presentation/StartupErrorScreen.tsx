import React from 'react';
import { Pressable, Text, View } from 'react-native';

interface StartupErrorScreenProps {
  title: string;
  message: string;
  onRetry: () => void;
}

export default function StartupErrorScreen({
  title,
  message,
  onRetry,
}: StartupErrorScreenProps) {
  return (
    <View className="flex-1 justify-center bg-slate-100 px-6">
      <Text className="text-2xl font-extrabold text-slate-900">
        {title}
      </Text>
      <Text className="mt-3 text-base text-slate-600">
        {message}
      </Text>
      <Pressable
        onPress={onRetry}
        className="mt-8 items-center rounded-xl bg-teal-600 px-4 py-3"
      >
        <Text className="text-base font-bold text-white">
          Try again
        </Text>
      </Pressable>
    </View>
  );
}
