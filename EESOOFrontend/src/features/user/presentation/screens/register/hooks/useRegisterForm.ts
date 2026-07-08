import { useState } from 'react';
import { RegisterFormFields } from '../models/RegisterFormModel';


const initialForm: RegisterFormFields = {
    firstName: "",
    lastName: "",
    phoneNumber: "",
    pin: "",
    confirmPin: "",
    email: "",
};

function formatNameInput(text: string): string {
    let value = text.replace(/\s/g, '');
    value = value.replace(/[^a-zA-Z]/g, '');

    if (value.length > 0) {
        value = value.charAt(0).toUpperCase() + value.slice(1);
    }

    return value;
}

function formatPhoneNumberInput(text: string): string {
    
    let digits = text.replace(/[^0-9]/g, '');

    
    digits = digits.slice(0, 10);

    return digits;
}

function formatPinInput(text: string): string {
    
    let digits = text.replace(/[^0-9]/g, '');

    
    digits = digits.slice(0, 4);

    return digits;
}

function formatEmailInput(text: string): string {
    let value = text.trim();

    value = value.toLowerCase();

    value = value.replace(/\s/g, '');

    return value;
}

const fieldFormatters: Partial<
    Record<keyof RegisterFormFields, (value: string) => string>
> = {
    firstName: formatNameInput,
    lastName: formatNameInput,
    phoneNumber: formatPhoneNumberInput,
    pin: formatPinInput,
    confirmPin: formatPinInput,
    email: formatEmailInput,
};


export function useRegisterForm() {
    const [form, setForm] = useState<RegisterFormFields>(initialForm);

    function updateField(field: keyof RegisterFormFields, value: string) {

        const formatter = fieldFormatters[field];
        if (formatter) {
            value = formatter(value);
        }

        setForm(prev => ({ ...prev, [field]: value }));
    }

    return {
        form,
        updateField,
    };
}
