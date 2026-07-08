import { ValueResult } from "../../common/ValueResult";

export class Email {

  private constructor(private readonly value: string | null) { }

  private static readonly EMAIL_PATTERN =
    /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;

  static create(
    raw: string | null | undefined
  ): ValueResult<Email> {

    if (raw === null || raw === undefined || raw.trim() === "") {
      return {
        ok: true,
        value: new Email(null)
      };
    }


    const trimmed = raw.trim().toLowerCase();


    if (!Email.EMAIL_PATTERN.test(trimmed)) {
      return {
        ok: false,
        message: "Invalid email format"
      };
    }


    return {
      ok: true,
      value: new Email(trimmed)
    };
  }

  getValue(): string | null {
    return this.value;
  }

  isPresent(): boolean {
    return this.value !== null;
  }

  toString(): string {
    return this.value ?? "(no email)";
  }

  equals(other: Email): boolean {
    return this.value === other.value;
  }
}
