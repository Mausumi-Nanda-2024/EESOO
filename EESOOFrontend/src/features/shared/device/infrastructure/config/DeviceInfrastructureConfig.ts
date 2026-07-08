import { DeviceRepository } from "../../domain/repository/DeviceRepository";
import { StoreDeviceUseCase } from "../../application/StoreDeviceUsecase";
import { CheckDeviceLinkUseCase } from "../../application/CheckDeviceLinkUseCase";
import { DeviceRepositoryImpl } from "../repository/DeviceRepositoryImpl";
import { DeviceApiServiceImpl } from "../api/DeviceApiServiceImpl";

// Infrastructure layer
export const deviceRepository: DeviceRepository = new DeviceRepositoryImpl();
export const deviceApiService = new DeviceApiServiceImpl();

// Application layer (use cases)
export const storeDeviceUseCase = new StoreDeviceUseCase(
    deviceRepository,
    deviceApiService
);

export const checkDeviceLinkUseCase = new CheckDeviceLinkUseCase(
    deviceRepository,
    deviceApiService
);
