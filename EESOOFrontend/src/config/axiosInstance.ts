import axios from 'axios';
import { API_BASE_URL } from './apiConfig';

const axiosInstance = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
});

axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error?.response?.status;
        const isUnexpectedError = !status || status >= 500;

        if (isUnexpectedError) {
            console.error("Unexpected API failure", {
                method: error?.config?.method,
                url: error?.config?.url,
                status,
            });
        }

        return Promise.reject(error);
    }
);

export default axiosInstance;
