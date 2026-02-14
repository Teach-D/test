export interface ApiResponse<T> {
  status: string;
  data: T | null;
  errorCode: string | null;
  message: string | null;
}

export interface LoginRequest {
  nickname: string;
}

export interface LoginResponse {
  accessToken: string;
  memberId: number;
  nickname: string;
  role: string;
}
