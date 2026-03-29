// src/services/authService.js
import api from './api';

export const login = async (credentials) => {
  const response = await api.post('/auth/login', credentials);
  const { token, ...userData } = response.data;

  // Guardar token
  localStorage.setItem('token', token);

  // Cargar menú (CORREGIDO - ahora llama al endpoint correcto)
  const menuResponse = await api.get('/menus');
  const menuTree = menuResponse.data;

  return { token, ...userData, menuTree };
};

export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
};

export const getCurrentUser = () => {
  const token = localStorage.getItem('token');
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload;
  } catch (e) {
    console.error('Error decodificando token:', e);
    return null;
  }
};