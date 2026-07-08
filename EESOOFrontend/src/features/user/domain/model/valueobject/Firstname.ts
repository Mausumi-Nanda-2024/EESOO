import { ValueResult } from "../../common/ValueResult";

export class FirstName {
  private constructor(private readonly value: string) {}

  static readonly MIN_LENGTH = 2;
  static readonly MAX_LENGTH = 30;

  static create(raw: string): ValueResult<FirstName> {
    if (!raw || raw.trim().length === 0) {
      return {
        ok: false,
        message: "First name cannot be empty",
      };
    }

    const trimmed = raw.trim();

    if (trimmed.length < FirstName.MIN_LENGTH) {
      return {
        ok: false,
        message: `First name must be at least ${FirstName.MIN_LENGTH} characters`,
      };
    }

    if (trimmed.length > FirstName.MAX_LENGTH) {
      return {
        ok: false,
        message: `First name must not exceed ${FirstName.MAX_LENGTH} characters`,
      };
    }

    if (!/^[A-Za-z]+$/.test(trimmed)) {
      return {
        ok: false,
        message:
          "First name must contain only alphabets (no numbers or special characters)",
      };
    }

    const formatted = FirstName.format(trimmed);

    return {
      ok: true,
      value: new FirstName(formatted),
    };
  }

  private static format(input: string): string {
    const lower = input.toLowerCase();
    return lower.charAt(0).toUpperCase() + lower.slice(1);
  }

  getValue(): string {
    return this.value;
  }

  toString(): string {
    return this.value;
  }

  equals(other: FirstName): boolean {
    return this.value === other.value;
  }
}
