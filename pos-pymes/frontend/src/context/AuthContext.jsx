// src/context/AuthContext.js (el archivo corregido que te di)
import React, { createContext, useContext, useReducer, useEffect } from 'react';
import { login as loginService, logout as logoutService } from '../services/authService';

const AuthContext = createContext();

const authReducer = (state, action) => {
  switch (action.type) {
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

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      // Opcional: recuperar sesión si hay token
      // Puedes decidir si hacer login automático o no
    } else {
      dispatch({ type: 'LOGIN_FAILURE', payload: null });
    }
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