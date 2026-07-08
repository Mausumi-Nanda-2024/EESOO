import AsyncStorage from '@react-native-async-storage/async-storage';
import { Platform } from 'react-native';
import DeviceInfo from 'react-native-device-info';
import { Device } from '../../domain/model/entity/device';
import { DeviceRepository } from '../../domain/repository/DeviceRepository';
import { DeviceIdProviderImpl } from '../service/DeviceIdProviderImpl';

const DEVICE_STORAGE_KEY = 'DEVICE_STATE';

const generateInstallId = (): string => {
    return `install_${Date.now()}_${Math.random().toString(36).slice(2, 11)}`;
};

export class DeviceRepositoryImpl implements DeviceRepository {
    private readonly deviceIdProvider = new DeviceIdProviderImpl();

    async getDevice(): Promise<Device | null> {
        const raw = await AsyncStorage.getItem(DEVICE_STORAGE_KEY);

        if (!raw) {
            return null;
        }

        const state = JSON.parse(raw);

        if (!state.installId || !state.osVersion || !state.platform) {
            return null;
        }

        return Device.rehydrate(
            state.id ?? null,
            state.installId,
            state.osVersion,
            state.platform,
            state.deviceIdNullableReason ?? null,
            state.deviceStoredInBackend ?? false,
            state.retryCount ?? 0,
            state.permanentlyFailed ?? false
        );
    }

    async saveDevice(device: Device): Promise<void> {
        await AsyncStorage.setItem(DEVICE_STORAGE_KEY, JSON.stringify(device.getState()));
    }

    async createDevice(): Promise<Device> {
        const diagnostics = await this.deviceIdProvider.fetch();
        const installId = generateInstallId();
        const osVersion = DeviceInfo.getSystemVersion();
        const platform = Platform.OS;

        return Device.create(
            diagnostics.deviceId,
            installId,
            osVersion,
            platform,
            diagnostics.deviceIdNullableReason
        );
    }

    async getCurrentDeviceInfo(installId: string): Promise<Device> {
        const diagnostics = await this.deviceIdProvider.fetch();
        const osVersion = DeviceInfo.getSystemVersion();
        const platform = Platform.OS;

        return Device.create(
            diagnostics.deviceId,
            installId,
            osVersion,
            platform,
            diagnostics.deviceIdNullableReason
        );
    }
}
