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

export interface PointBalanceResponse {
  totalBalance: number;
  expiringSoonBalance: number;
}

export interface PointResponse {
  id: number;
  amount: number;
  remainingAmount: number;
  earnedAt: string;
  expiresAt: string;
  isExpired: boolean;
}

export interface ProductResponse {
  id: number;
  name: string;
  description: string | null;
  price: number;
  stock: number;
  isActive: boolean;
}

export interface OrderRequest {
  productId: number;
}

export interface OrderResponse {
  id: number;
  productId: number;
  productName: string;
  usedPoint: number;
  status: string;
  orderedAt: string;
}
