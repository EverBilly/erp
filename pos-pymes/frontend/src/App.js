import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';

// Components
import PrivateRoute from './components/PrivateRoute';
import Layout from './components/Layout';
import { NotificationProvider } from './context/NotificationContext';

// Pages
import Login from './pages/Login';
import MainMenu from './pages/MainMenu';
import Dashboard from './pages/Dashboard';
import UsuariosView from './pages/usuarios/UsuariosView';

// Theme
import { theme } from './theme';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <NotificationProvider> 
        <Router>
          <AuthProvider>
            <Routes>
              <Route path="/login" element={<Login />} />
              <Route path="/" element={<PrivateRoute><MainMenu /></PrivateRoute>} />
              
              <Route
                element={
                  <PrivateRoute>
                    <Layout />
                  </PrivateRoute>
                }
              >

                {/* <Route index element={<Dashboard />} /> */}
                <Route path="usuarios/*" element={<UsuariosView />} />
              </Route>
              
              <Route path="*" element={<Navigate to="/" />} />
            </Routes>
          </AuthProvider>
        </Router>
      </NotificationProvider>
    </ThemeProvider>
  );
}

export default App;