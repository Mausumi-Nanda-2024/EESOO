import { DeviceRepository } from "../../../../shared/device/domain/repository/DeviceRepository";
import { User } from "../../../domain/model/entity/User";
import { Email } from "../../../domain/model/valueobject/Email";
import { FirstName } from "../../../domain/model/valueobject/Firstname";
import { LastName } from "../../../domain/model/valueobject/Lastname";
import { Pin } from "../../../domain/model/valueobject/Pin";
import { PhoneNumber } from "../../../domain/model/valueobject/PhoneNumber";
import { FieldError } from "../../common/FieldError";
import { Result } from "../../common/Result";
import { RegisterUserRequestDTO } from "../dto/RegisterUserRequestDTO";
import { RegisterUserResponseDTO } from "../dto/RegisterUserResponseDTO";
import { userRegistrationApiService as UserRegistrationApiService } from "../interface/UserRegistrationApiService";

export class RegisterUserUseCase {
    constructor(
        private readonly registrationApiService: UserRegistrationApiService,
        private readonly deviceRepository: DeviceRepository
    ) {}

    async registerNewUser(
        data: Omit<RegisterUserRequestDTO, "deviceId" | "installId">
    ): Promise<Result<RegisterUserResponseDTO>> {
        const errors: FieldError[] = [];

        const firstNameResult = FirstName.create(data.firstName);
        if (!firstNameResult.ok) {
            errors.push({ field: "firstName", message: firstNameResult.message });
        }

        const lastNameResult = LastName.create(data.lastName);
        if (!lastNameResult.ok) {
            errors.push({ field: "lastName", message: lastNameResult.message });
        }

        const phoneNumberResult = PhoneNumber.create(data.phoneNumber);
        if (!phoneNumberResult.ok) {
            errors.push({ field: "phoneNumber", message: phoneNumberResult.message });
        }

        const pinResult = Pin.create(data.pin);
        if (!pinResult.ok) {
            errors.push({ field: "password", message: pinResult.message });
        }

        const emailResult = Email.create(data.email);
        if (!emailResult.ok) {
            errors.push({ field: "email", message: emailResult.message });
        }

        if (errors.length > 0) {
            return { success: false, errors };
        }

        if (
            !firstNameResult.ok ||
            !lastNameResult.ok ||
            !phoneNumberResult.ok ||
            !pinResult.ok ||
            !emailResult.ok
        ) {
            throw new Error("Unexpected invalid ValueObject state");
        }

        const user = User.register(
            firstNameResult.value,
            lastNameResult.value,
            phoneNumberResult.value,
            pinResult.value,
            emailResult.value
        );

        const device = await this.deviceRepository.getDevice();

        if (!device) {
            throw new Error("Device not initialized");
        }

        const response = await this.registrationApiService.sendRegistrationRequest({
            firstName: user.getFirstName().getValue(),
            lastName: user.getLastName().getValue(),
            phoneNumber: user.getPhoneNumber().getValue(),
            pin: user.getPin().getValue(),
            email: user.getEmail()?.getValue() ?? null,
            deviceId: device.getId(),
            installId: device.getInstallId(),
        });

        return { success: true, data: response };
    }
}
