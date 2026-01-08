import api from './api';

const authService = {
  login: async (username, password) => {
    const response = await api.post('/auth/login', { username, password });
    const data = response.data;

    if (data.token) {
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(data));

      // Ahora cargamos el menu separado
      const menuResponse = await api.get('/usuarios/menu');
      const menuData = menuResponse.data;

      // Gurdamos el menu en el user
      const userWithMenu = { ...data, menus: menuData };
      localStorage.setItem('user', JSON.stringify(userWithMenu));
      return userWithMenu;
    }
    return data;
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