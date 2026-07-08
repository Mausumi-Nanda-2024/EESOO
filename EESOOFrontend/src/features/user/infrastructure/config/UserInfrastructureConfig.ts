import { deviceRepository } from "../../../shared/device/infrastructure/config/DeviceInfrastructureConfig";
import { userRegistrationApiService } from "../../application/register/interface/UserRegistrationApiService";
import { RegisterUserUseCase } from "../../application/register/usecase/RegisterUserUseCase";
import { userRegistrationApiServiceImpl } from "../register/api/UserRegistrationAPIServiceImpl";

const userRegistrationApiServiceInstance: userRegistrationApiService =
  new userRegistrationApiServiceImpl();

export const registerUserUseCase = new RegisterUserUseCase(
  userRegistrationApiServiceInstance,
  deviceRepository
);
