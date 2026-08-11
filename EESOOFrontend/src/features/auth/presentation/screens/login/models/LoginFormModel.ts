export interface LoginFormFields {
  phoneNumber: string;
  pin: string;
}

export type LoginErrors = Partial<
  Record<keyof LoginFormFields, string>
> & {
  recoveryPhoneNumber?: string;
  general?: string;
};
