import { ValueResult } from "../../common/ValueResult";

export class LastName {
  private constructor(private readonly value: string) {}

  static readonly MIN_LENGTH = 2;
  static readonly MAX_LENGTH = 30;

  static create(raw: string): ValueResult<LastName> {
    if (!raw || raw.trim().length === 0) {
      return {
        ok: false,
        message: "Last name cannot be empty",
      };
    }

    const trimmed = raw.trim();

    if (trimmed.length < LastName.MIN_LENGTH) {
      return {
        ok: false,
        message: `Last name must be at least ${LastName.MIN_LENGTH} characters`,
      };
    }

    if (trimmed.length > LastName.MAX_LENGTH) {
      return {
        ok: false,
        message: `Last name must not exceed ${LastName.MAX_LENGTH} characters`,
      };
    }

    if (!/^[A-Za-z]+$/.test(trimmed)) {
      return {
        ok: false,
        message:
          "Last name must contain only alphabets (no numbers or special characters)",
      };
    }

    const formatted = LastName.format(trimmed);

    return {
      ok: true,
      value: new LastName(formatted),
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

  equals(other: LastName): boolean {
    return this.value === other.value;
  }
}
