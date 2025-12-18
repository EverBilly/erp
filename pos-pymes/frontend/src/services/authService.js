import api from './api';

const authService = {
  login: async (username, password) => {
    const response = await api.post('/auth/login', { username, password });
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      return JSON.parse(userStr);
    }
    return null;
  },

  getAuthHeader: () => {
    const token = localStorage.getItem('token');
    if (token) {
      return { Authorization: 'Bearer ' + token };
    }
    return {};
  },

  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  }
};

export default authService;