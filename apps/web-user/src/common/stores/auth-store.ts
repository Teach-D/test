import { create } from 'zustand';
import type { LoginResponse } from '../types/api';

interface AuthState {
  accessToken: string | null;
  memberId: number | null;
  nickname: string | null;
  role: string | null;
  isAuthenticated: boolean;
  login: (response: LoginResponse) => void;
  logout: () => void;
  initialize: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  memberId: null,
  nickname: null,
  role: null,
  isAuthenticated: false,

  login: (response: LoginResponse) => {
    // localStorage에 저장
    localStorage.setItem('accessToken', response.accessToken);
    localStorage.setItem('memberId', response.memberId.toString());
    localStorage.setItem('nickname', response.nickname);
    localStorage.setItem('role', response.role);

    // 스토어 업데이트
    set({
      accessToken: response.accessToken,
      memberId: response.memberId,
      nickname: response.nickname,
      role: response.role,
      isAuthenticated: true,
    });
  },

  logout: () => {
    // localStorage 정리
    localStorage.removeItem('accessToken');
    localStorage.removeItem('memberId');
    localStorage.removeItem('nickname');
    localStorage.removeItem('role');

    // 스토어 초기화
    set({
      accessToken: null,
      memberId: null,
      nickname: null,
      role: null,
      isAuthenticated: false,
    });
  },

  initialize: () => {
    // 앱 시작 시 localStorage에서 인증 정보 복원
    const accessToken = localStorage.getItem('accessToken');
    const memberId = localStorage.getItem('memberId');
    const nickname = localStorage.getItem('nickname');
    const role = localStorage.getItem('role');

    if (accessToken && memberId && nickname && role) {
      set({
        accessToken,
        memberId: parseInt(memberId, 10),
        nickname,
        role,
        isAuthenticated: true,
      });
    }
  },
}));
