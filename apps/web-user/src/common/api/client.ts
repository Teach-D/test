import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { ApiResponse } from '../types/api';

const API_BASE_URL = import.meta.env.VITE_API_URL || '';

const client = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터: JWT 토큰 자동 첨부
client.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  },
);

// 응답 인터셉터: 401 처리 및 ApiResponse 언래핑
client.interceptors.response.use(
  (response) => {
    const apiResponse = response.data as ApiResponse<unknown>;
    if (apiResponse.status === 'SUCCESS') {
      return { ...response, data: apiResponse.data };
    }
    // ERROR 상태인 경우 에러 메시지로 reject
    return Promise.reject(new Error(apiResponse.message ?? '요청 처리에 실패했습니다.'));
  },
  (error: AxiosError<ApiResponse<unknown>>) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('memberId');
      localStorage.removeItem('nickname');
      localStorage.removeItem('role');
      window.location.href = '/login';
      return Promise.reject(new Error('인증이 필요합니다.'));
    }
    // 백엔드 에러 메시지 추출
    const apiMessage = error.response?.data?.message;
    return Promise.reject(new Error(apiMessage ?? error.message ?? '네트워크 오류가 발생했습니다.'));
  },
);

export default client;
