import { LoginMode } from '../../features/auth/application/model/LoginMode';

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
      mode: LoginMode;
      knownIdentity: KnownIdentity;
    }

  | {
    name: 'LOGIN';
    mode: 'MANUAL';
    
  };
