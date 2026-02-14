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

export interface RouletteStatusResponse {
  hasPlayedToday: boolean;
  remainingBudget: number;
  canPlay: boolean;
}

export interface RouletteResultResponse {
  point: number;
  playedAt: string;
}

export interface BudgetResponse {
  date: string;
  totalBudget: number;
  usedBudget: number;
  remainingBudget: number;
}
