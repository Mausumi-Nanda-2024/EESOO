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
        console.error("API Error:",  error?.response?.data || error.message);
        return Promise.reject(error);
    }
);

export default axiosInstance;
