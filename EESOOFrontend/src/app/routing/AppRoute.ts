import { LoginMode } from '../../features/auth/application/login/model/LoginMode';
import { AuthenticatedIdentity } from '../../features/auth/application/login/model/AuthenticatedIdentity';

export type KnownIdentity = {
  userId: string;
  username: string;
  phoneNumber: string;
};

export type AppRoute =
  | {
      name: 'BOOTSTRAPPING';
      message: string;
    }
  | {
      name: 'AUTH_ENTRY';
    }
  | {
      name: 'REGISTER';
      deviceReady: boolean;
      deviceMessage: string;
    }
  | {
      name: 'REGISTER_SUCCESS';
      username: string;
    }
  | {
      name: 'LOGIN';
      mode: Extract<LoginMode, 'KNOWN_IDENTITY'>;
      knownIdentity: KnownIdentity;
    }
  | {
      name: 'LOGIN';
      mode: Extract<LoginMode, 'MANUAL'>;
    }
  | {
      name: 'AUTHENTICATED';
      identity: AuthenticatedIdentity;
    }
  | {
      name: 'DEVICE_LINK_RECOVERY';
      reason: string | null;
    }
  | {
      name: 'SESSION_RESTORE_ERROR';
      message: string;
    }
  | {
      name: 'DEVICE_STARTUP_ERROR';
      message: string;
    };
