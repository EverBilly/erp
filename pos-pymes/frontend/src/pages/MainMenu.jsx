import React from 'react';
import {
  Container,
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
  Button,
  useTheme,
  CircularProgress,
  Chip
} from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getIconComponent } from '../utils/iconMapper';

const MainMenu = () => {
  const { user, menuTree, loading, logout } = useAuth();
  const navigate = useNavigate();
  const theme = useTheme();

  const menus = menuTree || [];

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  // Función para obtener color por categoría
  const getCategoryColor = (name) => {
    if (name.toLowerCase().includes('administración') || name.toLowerCase().includes('admin')) return '#1976d2';
    if (name.toLowerCase().includes('usuarios') || name.toLowerCase().includes('usuario')) return '#e91e63';
    if (name.toLowerCase().includes('configuración') || name.toLowerCase().includes('config')) return '#607d8b';
    if (name.toLowerCase().includes('reportes') || name.toLowerCase().includes('reporte')) return '#9c27b0';
    if (name.toLowerCase().includes('finanzas') || name.toLowerCase().includes('caja') || name.toLowerCase().includes('pago')) return '#4caf50';
    if (name.toLowerCase().includes('inventario') || name.toLowerCase().includes('stock')) return '#ff9800';
    if (name.toLowerCase().includes('ventas') || name.toLowerCase().includes('pedido')) return '#2196f3';
    return '#757575';
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4, minHeight: '100vh' }}>
      <Box sx={{ textAlign: 'center', mb: 6 }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#333', fontWeight: 600 }}>
          Panel de Control POS
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Bienvenido, <strong>{user?.fullName}</strong> • Selecciona un módulo para comenzar
        </Typography>
      </Box>

      {menus.length === 0 ? (
        <Box sx={{ textAlign: 'center', py: 10 }}>
          <Typography variant="h6" color="text.secondary">
            No tienes acceso a ningún módulo.
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Contacte al administrador para asignarle permisos.
          </Typography>
        </Box>
      ) : (
        <Grid container spacing={3}>
          {menus.map((item) => {
            const IconComponent = getIconComponent(item.icon);
            const categoryColor = getCategoryColor(item.name);

            return (
              <Grid item xs={12} sm={6} md={3} key={item.id}>
                <Button
                  component={Link}
                  to={item.path}
                  fullWidth
                  sx={{
                    textDecoration: 'none',
                    '& .MuiCard-root': {
                      height: '100%',
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'center',
                      alignItems: 'center',
                      textAlign: 'center',
                      padding: 3,
                      borderRadius: '16px',
                      border: `1px solid ${theme.palette.divider}`,
                      bgcolor: theme.palette.background.paper,
                      transition: 'all 0.3s cubic-bezier(0.17, 0.67, 0.88, 1.0)',
                      boxShadow: '0 6px 20px rgba(0,0,0,0.08)',
                      '&:hover': {
                        transform: 'translateY(-8px) scale(1.03)',
                        boxShadow: '0 12px 40px rgba(0,0,0,0.15)',
                        borderColor: categoryColor,
                        bgcolor: theme.palette.action.hover,
                      },
                    },
                    '& .MuiCardContent-root': {
                      p: 2,
                    },
                  }}
                >
                  <Card sx={{ width: '100%', height: '100%', border: 'none' }}>
                    <CardContent>
                      <Box 
                        sx={{ 
                          width: 64, 
                          height: 64, 
                          borderRadius: '50%',
                          bgcolor: `${categoryColor}20`,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          mb: 2,
                          mx: 'auto'
                        }}
                      >
                        <IconComponent 
                          fontSize="large" 
                          sx={{ color: categoryColor, fontSize: 32 }}
                        />
                      </Box>
                      <Typography 
                        variant="subtitle1" 
                        fontWeight="bold" 
                        color="text.primary"
                        sx={{ mb: 1 }}
                      >
                        {item.name}
                      </Typography>
                      <Chip
                        label={item.category || 'General'}
                        size="small"
                        sx={{
                          backgroundColor: `${categoryColor}20`,
                          color: categoryColor,
                          fontWeight: 600,
                          fontSize: '0.75rem',
                        }}
                      />
                    </CardContent>
                  </Card>
                </Button>
              </Grid>
            );
          })}
        </Grid>
      )}

      <Box sx={{ mt: 6, textAlign: 'center' }}>
        <Button
          variant="outlined"
          color="error"
          onClick={() => {
            logout();
            navigate('/login');
          }}
          sx={{
            borderRadius: 8,
            px: 4,
            py: 1.5
          }}
        >
          CERRAR SESIÓN
        </Button>
      </Box>
    </Container>
  );
};

export default MainMenu;