import { RegisterUserRequestDTO } from "../dto/RegisterUserRequestDTO";
import { RegisterUserResponseDTO } from "../dto/RegisterUserResponseDTO";

export interface userRegistrationApiService {
      sendRegistrationRequest(data: RegisterUserRequestDTO): Promise<RegisterUserResponseDTO>;

}