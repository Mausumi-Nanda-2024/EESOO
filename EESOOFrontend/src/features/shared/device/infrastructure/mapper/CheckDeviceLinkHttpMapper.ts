import { CheckDeviceLinkHttpRequestDTO } from "../dto/CheckDeviceLinkHttpRequestDTO";
import { CheckDeviceLinkHttpResponseDTO } from "../dto/CheckDeviceLinkHttpResponseDTO";


export class CheckDeviceLinkHttpMapper {

    static toHttpRequest(deviceId: string): CheckDeviceLinkHttpRequestDTO {
        return{
            deviceId
        };
    }

    static toResponse(dto: CheckDeviceLinkHttpResponseDTO): any {
        if (dto.status === 'LINKED') {
            if (!dto.user_id || !dto.username || !dto.phone_number) {
                throw new Error('Invalid response: missing user data for linked device');
            }

            return {
                linked: true,
                userId: dto.user_id,
                username: dto.username,
                phoneNumber: dto.phone_number
            };
        }

        return {
            linked: false,
            reason: dto.failure_reason
        };
    }

}
