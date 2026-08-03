import { AuthenticatedIdentity } from './AuthenticatedIdentity';

export interface LoginResult {
  identity: AuthenticatedIdentity;
  deviceLinked: boolean;
  deviceLinkFailureReason: string | null;
}
