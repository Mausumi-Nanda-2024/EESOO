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
            if (!dto.userId || !dto.username || !dto.phoneNumber) {
                throw new Error('Invalid response: missing user data for linked device');
            }

            return {
                linked: true,
                userId: dto.userId,
                username: dto.username,
                phoneNumber: dto.phoneNumber
            };
        }

        return {
            linked: false,
            reason: dto.reason
        };
    }

}
