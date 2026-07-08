import { RegisterUserRequestDTO } from "../../../application/register/dto/RegisterUserRequestDTO";
import { RegisterUserResponseDTO } from "../../../application/register/dto/RegisterUserResponseDTO";
import { userRegistrationApiService } from "../../../application/register/interface/UserRegistrationApiService";
import { RegisterUserHttpMapper } from "../mapper/RegisterUserHttpMapper";
import { RegisterUserHttpRequestDTO } from "../dto/RegisterUserHttpRequestDTO";
import { RegisterUserHttpResponseDTO } from "../dto/RegisterUserHttpResponseDTO";
import axiosInstance from "../../../../../config/axiosInstance";


export class userRegistrationApiServiceImpl implements userRegistrationApiService {

    private readonly ENDPOINT = "/users/register"; // path only, base URL is handled by axiosInstance

    async sendRegistrationRequest(data: RegisterUserRequestDTO): Promise<RegisterUserResponseDTO> {
        const httpRequest: RegisterUserHttpRequestDTO = RegisterUserHttpMapper.toHttpRequest(data);

        try {
            const response = await axiosInstance.post<{ status: string; message: string; data: RegisterUserHttpResponseDTO }>(this.ENDPOINT, httpRequest);

            console.log("Raw API Response:", response.data);

            // 🔑 Extract nested "data"
            return RegisterUserHttpMapper.toResponse(response.data.data);
        } catch (error: any) {
            // Handle and rethrow with a user-friendly message
            const message = error?.response?.data?.message || "User registration failed";
            throw new Error(message);
        }
    }


}