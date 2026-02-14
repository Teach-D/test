import client from '@/common/api/client';
import type { LoginRequest, LoginResponse } from '@/common/types/api';

export const authApi = {
  login: async (request: LoginRequest): Promise<LoginResponse> => {
    const response = await client.post<LoginResponse>(
      '/api/auth/login',
      request,
    );
    return response.data;
  },
};
