import { ValueResult } from '../../../../shared/domain/common/ValueResult';

export class LoginPhoneNumber {
  private static readonly DIGITS_ONLY_REGEX = /^\d+$/;
  private static readonly INDIAN_MOBILE_REGEX = /^[6-9]\d{9}$/;

  private constructor(private readonly value: string) {}

  static create(rawPhoneNumber: string): ValueResult<LoginPhoneNumber> {
    if (!rawPhoneNumber) {
      return {
        ok: false,
        message: 'Phone number cannot be empty.',
      };
    }

    if (!LoginPhoneNumber.DIGITS_ONLY_REGEX.test(rawPhoneNumber)) {
      return {
        ok: false,
        message:
          'Phone number must contain only digits with no spaces or special characters.',
      };
    }

    if (rawPhoneNumber.length !== 10) {
      return {
        ok: false,
        message: 'Phone number must be exactly 10 digits.',
      };
    }

    if (!LoginPhoneNumber.INDIAN_MOBILE_REGEX.test(rawPhoneNumber)) {
      return {
        ok: false,
        message: 'Phone number must start with 6, 7, 8, or 9.',
      };
    }

    return {
      ok: true,
      value: new LoginPhoneNumber(rawPhoneNumber),
    };
  }

  getValue(): string {
    return this.value;
  }

  equals(other: LoginPhoneNumber): boolean {
    return this.value === other.value;
  }
}
