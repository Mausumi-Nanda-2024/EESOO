import { ValueResult } from '../../../../shared/domain/common/ValueResult';
import { LoginPhoneNumber } from './LoginPhoneNumber';
import { LoginPin } from './LoginPin';

export class LoginCredentials {
  private constructor(
    private readonly phoneNumber: LoginPhoneNumber,
    private readonly pin: LoginPin,
  ) {}

  static create(
    rawPhoneNumber: string,
    rawPin: string,
  ): ValueResult<LoginCredentials> {
    const phoneNumberResult = LoginPhoneNumber.create(rawPhoneNumber);

    if (!phoneNumberResult.ok) {
      return phoneNumberResult;
    }

    const pinResult = LoginPin.create(rawPin);

    if (!pinResult.ok) {
      return pinResult;
    }

    return {
      ok: true,
      value: new LoginCredentials(phoneNumberResult.value, pinResult.value),
    };
  }

  getPhoneNumber(): LoginPhoneNumber {
    return this.phoneNumber;
  }

  getPin(): LoginPin {
    return this.pin;
  }
}
