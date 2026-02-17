import client from '../../../common/api/client';
import type { ApiResponse } from '../../../common/types/api';

interface LoginRequest {
  nickname: string;
}

interface LoginResponse {
  accessToken: string;
  memberId: number;
  nickname: string;
  role: string;
}

export async function login(data: LoginRequest): Promise<LoginResponse> {
  const response = await client.post<ApiResponse<LoginResponse>>('/api/auth/admin-login', data);
  return response.data.data!;
}
