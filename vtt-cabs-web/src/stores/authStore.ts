import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '@/types';

interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  accessToken: string | null;
  isLoading: boolean;
  
  // Actions
  login: (user: User, accessToken: string) => void;
  logout: () => void;
  setUser: (user: User) => void;
  setLoading: (loading: boolean) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      isAuthenticated: false,
      user: null,
      accessToken: null,
      isLoading: true,
      
      login: (user, accessToken) =>
        set({
          isAuthenticated: true,
          user,
          accessToken,
          isLoading: false,
        }),
      
      logout: () =>
        set({
          isAuthenticated: false,
          user: null,
          accessToken: null,
          isLoading: false,
        }),
      
      setUser: (user) => set({ user }),
      
      setLoading: (isLoading) => set({ isLoading }),
    }),
    {
      name: 'vtt-auth',
      partialize: (state) => ({
        isAuthenticated: state.isAuthenticated,
        user: state.user,
        accessToken: state.accessToken,
      }),
    }
  )
);
