export interface CheckDeviceLinkHttpResponseDTO {
    status: "LINKED" | "NOT_LINKED";
    user_id?: string;
    username?: string;
    phone_number?: string;
    failure_reason?: string;
}