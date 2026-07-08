import { DeviceApiService } from "../../application/interface/DeviceApiService";
import { Device } from "../../domain/model/entity/device";
import { CheckDeviceResponseDTO } from "../../application/dto/CheckDeviceResponseDTO";
import { CheckDeviceLinkHttpMapper } from "../mapper/CheckDeviceLinkHttpMapper";
import { CheckDeviceLinkHttpRequestDTO } from "../dto/CheckDeviceLinkHttpRequestDTO";
import { CheckDeviceLinkHttpResponseDTO } from "../dto/CheckDeviceLinkHttpResponseDTO";
import { DeviceHttpStoreRequestDto } from "../dto/DeviceHttpStoreRequestDto";
import { DeviceHttpStoreResponseDto } from "../dto/DeviceHttpStoreResponseDto";
import axiosInstance from "../../../../../config/axiosInstance";

export class DeviceApiServiceImpl implements DeviceApiService {

    private readonly STORE_ENDPOINT = "/devices/store";
    private readonly CHECK_LINK_ENDPOINT = "/devices/check";

    async storeDevice(device: Device): Promise<void> {
        const dto: DeviceHttpStoreRequestDto = {
            deviceId: device.getId(),
            installId: device.getInstallId(),
            osVersion: device.getOsVersion(),
            platform: device.getPlatform(),
            deviceIdNullableReason: device.getDeviceIdNullableReason(),
        };

        try {
            await axiosInstance.post<DeviceHttpStoreResponseDto>(this.STORE_ENDPOINT, dto);
        } catch (error: any) {
            const message = error?.response?.data?.message || "Failed to store device";
            throw new Error(message);
        }
    }

    async checkDeviceLink(deviceId: string): Promise<CheckDeviceResponseDTO> {
        // Safety check: Should never happen if use case is correct
        if (!deviceId || deviceId.trim() === '') {
            throw new Error('DeviceId is required to check device link');
        }

        const httpRequest: CheckDeviceLinkHttpRequestDTO = CheckDeviceLinkHttpMapper.toHttpRequest(deviceId);

        try {
            const response = await axiosInstance.post<{ status: string; message: string; data: CheckDeviceLinkHttpResponseDTO }>(this.CHECK_LINK_ENDPOINT, httpRequest);

            console.log("Raw API Response:", response.data);

            return CheckDeviceLinkHttpMapper.toResponse(response.data.data);
        } catch (error: any) {
            const message = error?.response?.data?.message || "Device check failed";
            throw new Error(message);
        }
    }
}
