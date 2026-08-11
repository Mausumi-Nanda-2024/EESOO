import React, { useEffect, useState } from 'react';
import { Text, View } from 'react-native';
import { LoginResult } from '../../features/auth/application/login/model/LoginResult';
import {
  authenticatedHttpClient,
  logoutUserUseCase,
} from '../../features/auth/infrastructure/config/AuthInfrastructureConfig';
import AuthEntryScreen from '../../features/auth/presentation/screens/entry/AuthEntryScreen';
import LoginScreen from '../../features/auth/presentation/screens/login/LoginScreen';
import AuthenticatedScreen from '../../features/auth/presentation/screens/session/AuthenticatedScreen';
import { checkDeviceLinkUseCase } from '../../features/shared/device/infrastructure/config/DeviceInfrastructureConfig';
import DeviceLinkRecoveryScreen, {
  DeviceRecoveryAttemptResult,
} from '../../features/shared/device/presentation/screens/DeviceLinkRecoveryScreen';
import { RegisterUserResponseDTO } from '../../features/user/application/register/dto/RegisterUserResponseDTO';
import RegisterScreen from '../../features/user/presentation/screens/register/RegisterScreen';
import RegisterSuccessScreen from '../../features/user/presentation/screens/register/RegisterSuccessScreen';
import StartupErrorScreen from '../presentation/StartupErrorScreen';
import { deviceStartupCoordinator } from '../startup/AppStartupConfig';
import { AppRoute } from './AppRoute';
import { useStartupRoute } from './useStartupRoute';

export default function AppRouter() {
  const { route, retryStartup } = useStartupRoute();
  const [currentRoute, setCurrentRoute] =
    useState<AppRoute | null>(null);
  const activeRoute = currentRoute ?? route;

  useEffect(() => {
    authenticatedHttpClient.setSessionInvalidatedHandler(() => {
      setCurrentRoute(null);
      retryStartup();
    });

    return () => {
      authenticatedHttpClient.setSessionInvalidatedHandler(null);
    };
  }, [retryStartup]);

  async function handleRegistered(
    response: RegisterUserResponseDTO,
  ): Promise<void> {
    try {
      const checkResult =
        await checkDeviceLinkUseCase.execute();

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
    } catch {
      // Registration succeeded. The user can still continue with manual login.
    }

    setCurrentRoute({
      name: 'REGISTER_SUCCESS',
      username: response.username,
    });
  }

  function handleLoggedIn(result: LoginResult): void {
    if (result.deviceLinked) {
      setCurrentRoute({
        name: 'AUTHENTICATED',
        identity: result.identity,
      });
      return;
    }

    setCurrentRoute({
      name: 'DEVICE_LINK_RECOVERY',
      reason: result.deviceLinkFailureReason,
    });
  }

  async function handleLogout(): Promise<string | null> {
    const result = await logoutUserUseCase.execute();

    if (result.status === 'RETRY_REQUIRED') {
      return result.message;
    }

    setCurrentRoute(null);
    retryStartup();
    return null;
  }

  async function handleDeviceRecovery(): Promise<DeviceRecoveryAttemptResult> {
    const deviceResult =
      await deviceStartupCoordinator.execute();

    if (deviceResult.status === 'LINKED') {
      setCurrentRoute({
        name: 'AUTHENTICATED',
        identity: {
          userId: deviceResult.identity.userId,
          username: deviceResult.identity.username,
        },
      });

      return { recovered: true };
    }

    if (deviceResult.status === 'NOT_READY') {
      return {
        recovered: false,
        message: deviceResult.message,
      };
    }

    return {
      recovered: false,
      message:
        'The device is registered but still not linked. Log out and sign in again to retry the backend login-linking process.',
    };
  }

  if (activeRoute.name === 'BOOTSTRAPPING') {
    return (
      <View className="flex-1 items-center justify-center bg-slate-100 px-6">
        <Text className="text-base font-semibold text-slate-700">
          {activeRoute.message}
        </Text>
      </View>
    );
  }

  if (activeRoute.name === 'DEVICE_STARTUP_ERROR') {
    return (
      <StartupErrorScreen
        title="Device setup failed"
        message={activeRoute.message}
        onRetry={retryStartup}
      />
    );
  }

  if (activeRoute.name === 'SESSION_RESTORE_ERROR') {
    return (
      <StartupErrorScreen
        title="Session check failed"
        message={activeRoute.message}
        onRetry={retryStartup}
      />
    );
  }

  if (activeRoute.name === 'AUTH_ENTRY') {
    return (
      <AuthEntryScreen
        onLoginPress={() =>
          setCurrentRoute({
            name: 'LOGIN',
            mode: 'MANUAL',
          })
        }
        onRegisterPress={() =>
          setCurrentRoute({
            name: 'REGISTER',
            deviceReady: true,
            deviceMessage: '',
          })
        }
      />
    );
  }

  if (activeRoute.name === 'LOGIN') {
    return (
      <LoginScreen
        mode={activeRoute.mode}
        knownIdentity={
          activeRoute.mode === 'KNOWN_IDENTITY'
            ? activeRoute.knownIdentity
            : undefined
        }
        onLoggedIn={handleLoggedIn}
        onCreateAccountPress={() =>
          setCurrentRoute({
            name: 'REGISTER',
            deviceReady: true,
            deviceMessage: '',
          })
        }
        onUseDifferentAccountPress={() =>
          setCurrentRoute({
            name: 'LOGIN',
            mode: 'MANUAL',
          })
        }
      />
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

  if (activeRoute.name === 'REGISTER') {
    return (
      <RegisterScreen
        deviceReady={activeRoute.deviceReady}
        deviceMessage={activeRoute.deviceMessage}
        onRegistered={handleRegistered}
        onLoginPress={() =>
          setCurrentRoute({
            name: 'LOGIN',
            mode: 'MANUAL',
          })
        }
      />
    );
  }

  if (activeRoute.name === 'DEVICE_LINK_RECOVERY') {
    return (
      <DeviceLinkRecoveryScreen
        reason={activeRoute.reason}
        onRetry={handleDeviceRecovery}
        onLogout={handleLogout}
      />
    );
  }

  return (
    <AuthenticatedScreen
      identity={activeRoute.identity}
      onLogout={handleLogout}
    />
  );
}
