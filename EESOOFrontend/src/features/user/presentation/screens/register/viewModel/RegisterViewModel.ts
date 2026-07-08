import { RegisterErrors, RegisterFormFields } from "../models/RegisterFormModel";

export interface RegisterViewModel {
  form: RegisterFormFields;
  errors: RegisterErrors;
  loading: boolean;


  onFieldChange: (field: keyof RegisterFormFields, value: string) => void;
  onClickRegister: () => Promise<void>;
}
