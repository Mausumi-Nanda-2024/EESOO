
export interface LoginFormFields {

    phoneNumber: string;
    pin: string;

}

export type LoginErrors = Partial<Record<keyof LoginFormFields , string>> & {
        general?: string;
    };