import { ValueResult } from '../../../../shared/domain/common/ValueResult';

export class Pin {
  private constructor(private readonly value: string) {}

  private static readonly PIN_REGEX = /^\d{4}$/;

  static create(raw: string): ValueResult<Pin> {
    if (!raw || raw === '') {
      return { ok: false, message: 'PIN cannot be empty.' };
    }

    if (!Pin.PIN_REGEX.test(raw)) {
      return {
        ok: false,
        message: 'PIN must be exactly 4 digits (numbers only).',
      };
    }

    return { ok: true, value: new Pin(raw) };
  }

  getValue(): string {
    return this.value;
  }

  toString(): string {
    return '*'.repeat(this.value.length);
  }

  equals(other: Pin): boolean {
    return this.value === other.value;
  }
}
