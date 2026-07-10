import React, { useState } from 'react';
import { Text, View } from 'react-native';
import RegisterScreen from '../../features/user/presentation/screens/register/RegisterScreen';
import { useStartupRoute } from './useStartupRoute';
import { RegisterUserResponseDTO } from '../../features/user/application/register/dto/RegisterUserResponseDTO';
import { checkDeviceLinkUseCase } from '../../features/shared/device/infrastructure/config/DeviceInfrastructureConfig';
import { AppRoute } from './AppRoute';
import RegisterSuccessScreen from '../../features/user/presentation/screens/register/RegisterSuccessScreen';

const FORCE_NOT_LINKED_AFTER_REGISTER = true;

export default function AppRouter() {
  const route = useStartupRoute();
  const [currentRoute , setCurrentRoute] = useState<AppRoute | null>(null);
  const activeRoute = currentRoute ?? route;

  async function handleRegistered(response: RegisterUserResponseDTO) {
    if (FORCE_NOT_LINKED_AFTER_REGISTER) {
      setCurrentRoute({
        name: 'REGISTER_SUCCESS',
        username: response.username,
      });

      return;
    }

    const checkResult = await checkDeviceLinkUseCase.execute();

    if (checkResult.linked) {
      setCurrentRoute({
        name: 'LOGIN',
        mode: 'KNOWN_IDENTITY',
        knownIdentity: {
          userId: checkResult.userId,
          username: checkResult.username,
          phoneNumber: checkResult.phoneNumber,
        },
      });

      return;
    }

    setCurrentRoute({
      name: 'REGISTER_SUCCESS',
      username: response.username,
    });
  }


  if (route.name === 'BOOTSTRAPPING') {
    return (
      <View className="flex-1 items-center justify-center bg-slate-100 px-6">
        <Text className="text-base font-semibold text-slate-700">
          {route.message}
        </Text>
      </View>
    );
  }

   if (activeRoute.name === 'LOGIN') {
    const message =
      activeRoute.mode === 'KNOWN_IDENTITY'
        ? `Welcome back, ${activeRoute.knownIdentity.username}`
        : 'Enter your phone number and PIN';

    return (
      <View className="flex-1 items-center justify-center bg-slate-100 px-6">
        <Text className="text-xl font-bold text-slate-900">Login Screen</Text>
        <Text className="mt-2 text-center text-sm text-slate-600">
          {message}
        </Text>
      </View>
    );
  }

   if (activeRoute.name === 'REGISTER_SUCCESS') {
    return (
      <RegisterSuccessScreen
        username={activeRoute.username}
        onLoginPress={() =>
          setCurrentRoute({
            name: 'LOGIN',
            mode: 'MANUAL',
          })
        }
      />
    );
  }

  return (
    <RegisterScreen
      deviceReady={route.name === 'REGISTER' ? route.deviceReady : false}
      deviceMessage={route.name === 'REGISTER' ? route.deviceMessage : ''}
      onRegistered={handleRegistered}
    />
  );
}