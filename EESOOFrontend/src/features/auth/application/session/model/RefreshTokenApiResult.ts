import { RefreshTokenResponseDTO } from "../dto/RefreshTokenResponseDTO";



export type RefreshTokenApiResult = 
| {
    ok: true;
    value: RefreshTokenResponseDTO;
}
|{
    ok: false;
    reason: 'INVALID_SESSION' | 'TEMPORARY_FAILURE';
    message: string;
};