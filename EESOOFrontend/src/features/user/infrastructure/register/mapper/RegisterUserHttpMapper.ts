import { RegisterUserRequestDTO } from "../../../application/register/dto/RegisterUserRequestDTO";
import { RegisterUserResponseDTO } from "../../../application/register/dto/RegisterUserResponseDTO";
import { RegisterUserHttpRequestDTO } from "../dto/RegisterUserHttpRequestDTO";
import { RegisterUserHttpResponseDTO } from "../dto/RegisterUserHttpResponseDTO";

export class RegisterUserHttpMapper {
    static toHttpRequest(dto: RegisterUserRequestDTO): RegisterUserHttpRequestDTO {
        return {
            firstName: dto.firstName,
            lastName: dto.lastName,
            phoneNumber: dto.phoneNumber,
            pin: dto.pin,
            email: dto.email,
            deviceId: dto.deviceId,
            installId: dto.installId,
        };
    }

    static toResponse(dto: RegisterUserHttpResponseDTO): RegisterUserResponseDTO {
        return {
            userId: dto.userId,
            username: dto.username,
            firstName: dto.firstName,
            lastName: dto.lastName,
            phoneNumber: dto.phoneNumber,
            email: dto.email,
            status: dto.status,
            registeredAt: dto.registeredAt,
        };
    }
}
