export interface RegisterFormFields{

    firstName: string;
    lastName: string;
    phoneNumber: string;
    pin: string;
    confirmPin: string;
    email: string;
}

export type RegisterErrors = Partial<Record<keyof RegisterFormFields, string>> & {
    general?: string;
};
