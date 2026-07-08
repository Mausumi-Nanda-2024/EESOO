import { ValueResult } from "../../common/ValueResult";

export class PhoneNumber {
  private readonly value: string;

  private static readonly DIGITS_ONLY_REGEX = /^\d+$/;
  private static readonly INDIAN_MOBILE_REGEX = /^[6-9]\d{9}$/;

  private constructor(value: string) {
    this.value = value;
  }

  static create(rawNumber: string): ValueResult<PhoneNumber> {
    if (!rawNumber || rawNumber === "") {
      return {
        ok: false,
        message: "Phone number cannot be empty."
      };
    }

    if (!PhoneNumber.DIGITS_ONLY_REGEX.test(rawNumber)) {
      return {
        ok: false,
        message: "Phone number must contain only digits with no spaces or special characters."
      };
    }

    if (rawNumber.length !== 10) {
      return {
        ok: false,
        message: "Phone number must be exactly 10 digits."
      };
    }

    if (!PhoneNumber.INDIAN_MOBILE_REGEX.test(rawNumber)) {
      return {
        ok: false,
        message: "Phone number must start with 6, 7, 8, or 9."
      };
    }

    return {
      ok: true,
      value: new PhoneNumber(rawNumber)
    };
  }

  getValue(): string {
    return this.value;
  }

  toString(): string {
    return this.value;
  }

  equals(other: PhoneNumber): boolean {
    return this.value === other.value;
  }
}
