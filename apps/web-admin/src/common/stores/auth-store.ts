import { create } from 'zustand';

interface AuthState {
  accessToken: string | null;
  memberId: number | null;
  nickname: string | null;
  role: string | null;
  isLoggedIn: boolean;
  login: (token: string, memberId: number, nickname: string, role: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: localStorage.getItem('accessToken'),
  memberId: localStorage.getItem('memberId') ? Number(localStorage.getItem('memberId')) : null,
  nickname: localStorage.getItem('nickname'),
  role: localStorage.getItem('role'),
  isLoggedIn: !!localStorage.getItem('accessToken'),

  login: (token, memberId, nickname, role) => {
    localStorage.setItem('accessToken', token);
    localStorage.setItem('memberId', String(memberId));
    localStorage.setItem('nickname', nickname);
    localStorage.setItem('role', role);
    set({ accessToken: token, memberId, nickname, role, isLoggedIn: true });
  },

  logout: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('memberId');
    localStorage.removeItem('nickname');
    localStorage.removeItem('role');
    set({ accessToken: null, memberId: null, nickname: null, role: null, isLoggedIn: false });
  },
}));
