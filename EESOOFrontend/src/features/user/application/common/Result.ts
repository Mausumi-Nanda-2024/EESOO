import {FieldError} from "./FieldError";

export type Result<T> = 
    | { success: true; data: T }
    | { success: false; errors: FieldError[] };