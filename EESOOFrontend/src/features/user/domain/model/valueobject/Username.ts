export class Username {
  private constructor(private readonly value: string) {}

  public static fromBackend(value: string): Username {
    if (!value || value.trim() === '') {
      throw new Error('Username cannot be empty.');
    }

    // Optionally: validate allowed characters (e.g., letters+digits only)
    if (!/^[a-zA-Z][a-zA-Z0-9]{3,20}$/.test(value)) {
      throw new Error('Invalid username format.');
    }

    return new Username(value);
  }

  public getValue(): string {
    return this.value;
  }

  public toString(): string {
    return this.value;
  }

  public equals(other: Username): boolean {
    return this.value === other.value;
  }
}
