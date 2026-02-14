import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { ApiResponse } from '../types/api';

const client = axios.create({
  baseURL: '',
  timeout: 10000,
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
    // ApiResponse 래퍼에서 data 추출
    const apiResponse = response.data as ApiResponse<unknown>;
    if (apiResponse.status === 'SUCCESS' && apiResponse.data !== null) {
      return { ...response, data: apiResponse.data };
    }
    return response;
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // 인증 실패 시 토큰 제거 및 로그인 페이지로 리다이렉트
      localStorage.removeItem('accessToken');
      localStorage.removeItem('memberId');
      localStorage.removeItem('nickname');
      localStorage.removeItem('role');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  },
);

export default client;
