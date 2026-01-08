import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Container,
  Paper,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  Box
} from '@mui/material';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  // Obtener menús permitidos
  const menus = user?.menus || [];

  const handleMenuClick = (ruta) => {
    navigate(ruta);
  };

  return (
    <Container maxWidth="lg">
      <Box sx={{ my: 4 }}>
        <Paper elevation={3} sx={{ p: 3, mb: 4 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Box>
              <Typography variant="h4" gutterBottom>
                Sistema POS
              </Typography>
              <Typography variant="subtitle1" color="text.secondary">
                Bienvenido, {user?.nombreCompleto}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Rol: {user?.roles?.map(r => r.authority).join(', ') || 'Sin roles'}
              </Typography>
            </Box>
            <Button variant="outlined" onClick={logout}>
              Cerrar Sesión
            </Button>
          </Box>
        </Paper>

        <Typography variant="h5" gutterBottom sx={{ mb: 3 }}>
          Módulos del Sistema
        </Typography>

        <Grid container spacing={3}>
          {menus
            .filter(menu => menu.visible && menu.ruta) // Solo menús visibles con ruta
            .sort((a, b) => a.orden - b.orden) // Ordenar por orden
            .map((menu) => (
              <Grid item xs={12} sm={6} md={3} key={menu.id}>
                <Card 
                  sx={{ 
                    height: '100%',
                    cursor: 'pointer',
                    transition: 'transform 0.2s',
                    '&:hover': { transform: 'translateY(-4px)' }
                  }}
                  onClick={() => handleMenuClick(menu.ruta)}
                >
                  <CardContent sx={{ textAlign: 'center', p: 3 }}>
                    <Box sx={{ 
                      display: 'inline-flex',
                      p: 2,
                      borderRadius: '50%',
                      bgcolor: '#1976d220',
                      color: '#1976d2',
                      mb: 2
                    }}>
                      {menu.icono ? (
                        <Box component="span" sx={{ fontSize: 40 }}>
                          {menu.icono}
                        </Box>
                      ) : (
                        <Box component="span" sx={{ fontSize: 40 }}>📦</Box>
                      )}
                    </Box>
                    <Typography variant="h6" gutterBottom>
                      {menu.nombre}
                    </Typography>
                    {menu.badgeText && (
                      <Box sx={{ 
                        display: 'inline-block',
                        px: 1,
                        py: 0.5,
                        borderRadius: 1,
                        bgcolor: menu.badgeColor || '#f5f5f5',
                        color: '#000',
                        fontSize: '0.75rem'
                      }}>
                        {menu.badgeText}
                      </Box>
                    )}
                  </CardContent>
                </Card>
              </Grid>
            ))}
        </Grid>

        {menus.length === 0 && (
          <Paper elevation={2} sx={{ p: 3, mt: 4, textAlign: 'center' }}>
            <Typography variant="body1" color="text.secondary">
              No tienes acceso a ningún módulo. Contacta al administrador.
            </Typography>
          </Paper>
        )}
      </Box>
    </Container>
  );
};

export default Dashboard;
