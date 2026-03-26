// src/context/AuthContext.js (el archivo corregido que te di)
import React, { createContext, useContext, useReducer, useEffect } from 'react';
import { login as loginService, logout as logoutService } from '../services/authService';

const AuthContext = createContext();

const authReducer = (state, action) => {
  switch (action.type) {
    case 'INIT_START':
      return { ...state, loading: true };
    case 'INIT_SUCCESS':
      return { ...state, loading: false, isAuthenticated: true, user: action.payload.user, menuTree: action.payload.menuTree };
    case 'INIT_FAILURE':
      return { ...state, loading: false, isAuthenticated: false, user: null, menuTree: [], error: 'Sesión inválida o expirada' };
    case 'LOGIN_START':
      return { ...state, loading: true, error: null };
    case 'LOGIN_SUCCESS':
      return { 
        ...state, 
        loading: false, 
        isAuthenticated: true, 
        user: action.payload.user,
        menuTree: action.payload.menuTree, // ✅ Asegúrate de guardar menuTree
        error: null 
      };
    case 'LOGIN_FAILURE':
      return { 
        ...state, 
        loading: false, 
        isAuthenticated: false, 
        user: null, 
        menuTree: [],
        error: action.payload 
      };
    case 'LOGOUT':
      return { 
        ...state, 
        isAuthenticated: false, 
        user: null, 
        menuTree: [],
        error: null 
      };
    default:
      return state;
  }
};

export const AuthProvider = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, {
    user: null,
    menuTree: [], // ✅ Agregado
    isAuthenticated: false,
    loading: true,
    error: null,
  });

  // Función para inicializar sesión desde token guardado
  const initAuth = async () => {
    dispatch({ type: 'INIT_START' });
    const token = localStorage.getItem('token');
    if (!token) {
      dispatch({ type: 'INIT_FAILURE' });
      return;
    }

    try {
      // Intentamos una llamada al backend para validar el token
      const response = await fetch(`${process.env.REACT_APP_API_URL || 'http://localhost:8080/api'}/menus`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        // Si no es 200, asumimos que el token no es válido
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const menuTree = await response.json();

      // También podríamos traer info del usuario aquí si no lo tenemos guardado
      // Por ahora, asumiremos que el token es suficientemente confiable para la info de usuario
      const user = JSON.parse(localStorage.getItem('user')) || {}; // Si guardas el user en localStorage

      dispatch({
        type: 'INIT_SUCCESS',
        payload: { user, menuTree }
      });
    } catch (error) {
      console.error('Token inválido o expirado:', error);
      // Token no válido, limpiar sesión
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      dispatch({ type: 'INIT_FAILURE' });
    }
  };

  useEffect(() => {
    initAuth();
  }, []);

  const login = async (credentials) => {
    dispatch({ type: 'LOGIN_START' });
    try {
      const data = await loginService(credentials);
      dispatch({
        type: 'LOGIN_SUCCESS',
        payload: {
          user: data,
          menuTree: data.menuTree || []
        }
      });
      return { success: true };
    } catch (error) {
      dispatch({
        type: 'LOGIN_FAILURE',
        payload: error.message || 'Error de autenticación'
      });
      return { success: false, message: error.message || 'Error de autenticación' };
    }
  };

  const logout = () => {
    logoutService();
    dispatch({ type: 'LOGOUT' });
  };

  return (
    <AuthContext.Provider value={{
      ...state,
      login,
      logout
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};