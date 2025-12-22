import { defineStore } from 'pinia';

export interface AuthState {
  token: string | null;
  username: string | null;
  role: string | null;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: null,
    username: null,
    role: null
  }),
  actions: {
    setAuth(token: string, username: string, role: string) {
      this.token = token;
      this.username = username;
      this.role = role;
      window.localStorage.setItem(
        'baotao_auth',
        JSON.stringify({ token, username, role })
      );
    },
    loadFromStorage() {
      const raw = window.localStorage.getItem('baotao_auth');
      if (!raw) return;
      try {
        const parsed = JSON.parse(raw);
        this.token = parsed.token;
        this.username = parsed.username;
        this.role = parsed.role;
      } catch {
        // ignore
      }
    },
    logout() {
      this.token = null;
      this.username = null;
      this.role = null;
      window.localStorage.removeItem('baotao_auth');
    }
  }
});


