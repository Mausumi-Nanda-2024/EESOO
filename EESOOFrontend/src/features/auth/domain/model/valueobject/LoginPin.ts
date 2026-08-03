import { ValueResult } from '../../../../shared/domain/common/ValueResult';

export class LoginPin {
  private static readonly PIN_REGEX = /^\d{4}$/;

  private constructor(private readonly value: string) {}

  static create(rawPin: string): ValueResult<LoginPin> {
    if (!rawPin) {
      return {
        ok: false,
        message: 'PIN cannot be empty.',
      };
    }

    if (!LoginPin.PIN_REGEX.test(rawPin)) {
      return {
        ok: false,
        message: 'PIN must be exactly 4 digits (numbers only).',
      };
    }

    return {
      ok: true,
      value: new LoginPin(rawPin),
    };
  }

  getValue(): string {
    return this.value;
  }

  toString(): string {
    return '*'.repeat(this.value.length);
  }

  equals(other: LoginPin): boolean {
    return this.value === other.value;
  }
}
