
export type ValueResult<T> = 
  | { ok: true; value: T }           // Success case
  | { ok: false; message: string }   // Failure case