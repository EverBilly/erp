import React, { createContext, useState, useContext, useEffect } from 'react';
import authService from '../services/authService';
import { buildMenuTree } from '../utils/menuUtils';

const AuthContext = createContext({});

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [menuTree, setMenuTree] = useState([]);

  // Funcion para cargar el usuario actual desde el token
  const loadCurrentUser = async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      setLoading(false);
      return;
    }
    try {
      const userData = await authService.getCurrentUser();
      setUser(userData);

      // Cargar y construir el menú
      const menusFlat = await authService.getMenu();
      setMenuTree(buildMenuTree(menusFlat));

    } catch (error) { 
      console.error('Error al cargar usuario actual:', error);
      authService.logout(); // Limpia si el token es inválido
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCurrentUser();
  }, []);

  const login = async (username, password) => {
    try {
      // 1. Login Normal
      const userData = await authService.login(username, password);
      setUser(userData);

      // 2. Cargar y construir el menú
      const menusFlat = await authService.getMenu();
      setMenuTree(buildMenuTree(menusFlat));

      return { success: true };
    } catch (error) {
      return { 
        success: false, 
        message: error.response?.data?.message || 'Error de autenticación' 
      };
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setMenuTree([]); // Limpiar menú al cerrar sesión
  };

  const value = {
    user,
    login,
    logout,
    loading,
    isAuthenticated: !!user,
    menuTree
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export default AuthContext;