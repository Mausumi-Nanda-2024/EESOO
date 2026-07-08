import React from 'react';
import { Text, View } from 'react-native';
import RegisterScreen from '../../features/user/presentation/screens/register/RegisterScreen';
import { useStartupRoute } from './useStartupRoute';

export default function AppRouter() {
  const route = useStartupRoute();

  if (route.name === 'BOOTSTRAPPING') {
    return (
      <View className="flex-1 items-center justify-center bg-slate-100 px-6">
        <Text className="text-base font-semibold text-slate-700">
          {route.message}
        </Text>
      </View>
    );
  }

  if (route.name === 'LOGIN') {
    return (
      <View className="flex-1 items-center justify-center bg-slate-100 px-6">
        <Text className="text-xl font-bold text-slate-900">Login Screen</Text>
        <Text className="mt-2 text-center text-sm text-slate-600">
          Welcome back, {route.knownIdentity.username}
        </Text>
      </View>
    );
  }

  return (
    <RegisterScreen
      deviceReady={route.deviceReady}
      deviceMessage={route.deviceMessage}
    />
  );
}