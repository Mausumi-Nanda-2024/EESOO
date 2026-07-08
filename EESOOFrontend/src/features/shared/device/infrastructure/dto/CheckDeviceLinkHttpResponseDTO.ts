export interface CheckDeviceLinkHttpResponseDTO {
    status: "LINKED" | "NOT_LINKED";
    userId?: string;
    username?: string;
    phoneNumber?: string;
    reason?: string; 
}