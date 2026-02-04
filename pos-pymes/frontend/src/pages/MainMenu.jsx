import React, { useState, useEffect } from 'react';
import {
  Container,
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
  Button,
  useTheme,
  useMediaQuery,
  CircularProgress,
  Alert,
  IconButton,
  Tooltip
} from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { getIconComponent } from '../utils/iconMapper';

const MainMenu = () => {
  const [menus, setMenus] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { user } = useAuth();
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  useEffect(() => {
    const fetchMenus = async () => {
      try {
        // ✅ Ahora llama al endpoint correcto: /api/menus
        const response = await api.get('/menus');
        setMenus(response.data);
      } catch (err) {
        console.error('Error al cargar menús:', err);
        setError('No se pudieron cargar los módulos. Intente nuevamente.');
      } finally {
        setLoading(false);
      }
    };

    if (user) {
      fetchMenus();
    }
  }, [user]);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Container maxWidth="md" sx={{ py: 8 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  // Función para obtener color por categoría
  const getCategoryColor = (nombre) => {
    if (nombre.toLowerCase().includes('administración') || nombre.toLowerCase().includes('admin')) return '#1976d2';
    if (nombre.toLowerCase().includes('usuarios') || nombre.toLowerCase().includes('usuario')) return '#e91e63';
    if (nombre.toLowerCase().includes('configuración') || nombre.toLowerCase().includes('config')) return '#607d8b';
    if (nombre.toLowerCase().includes('reportes') || nombre.toLowerCase().includes('reporte')) return '#9c27b0';
    if (nombre.toLowerCase().includes('finanzas') || nombre.toLowerCase().includes('caja') || nombre.toLowerCase().includes('pago')) return '#4caf50';
    if (nombre.toLowerCase().includes('inventario') || nombre.toLowerCase().includes('stock')) return '#ff9800';
    if (nombre.toLowerCase().includes('ventas') || nombre.toLowerCase().includes('pedido')) return '#2196f3';
    return '#757575';
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4, minHeight: '100vh' }}>
      <Box sx={{ textAlign: 'center', mb: 6 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Panel de Control POS
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Selecciona un módulo para comenzar
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
            const IconComponent = getIconComponent(item.icono);
            return (
              <Grid item xs={12} sm={6} md={3} key={item.id}>
                <Button
                  component={Link}
                  to={item.ruta}
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
                      padding: 2,
                      borderRadius: '12px',
                      border: `1px solid ${theme.palette.divider}`,
                      bgcolor: theme.palette.background.paper,
                      transition: 'all 0.3s cubic-bezier(0.17, 0.67, 0.88, 1.0)',
                      boxShadow: '0 6px 20px rgba(0,0,0,0.08)',
                      '&:hover': {
                        transform: 'translateY(-6px) scale(1.02)',
                        boxShadow: '0 12px 30px rgba(0,0,0,0.12)',
                        borderColor: getCategoryColor(item.nombre),
                        bgcolor: theme.palette.action.hover,
                      },
                    },
                    '& .MuiCardContent-root': {
                      p: 2,
                    },
                  }}
                >
                  <Card sx={{ width: '100%', height: '100%' }}>
                    <CardContent>
                      <Box sx={{ color: theme.palette.primary.main, mb: 1 }}>
                        <IconComponent fontSize="large" />
                      </Box>
                      <Typography variant="subtitle1" fontWeight="bold" color="text.primary">
                        {item.nombre}
                      </Typography>
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
            localStorage.removeItem('token');
            navigate('/login');
          }}
        >
          CERRAR SESIÓN
        </Button>
      </Box>
    </Container>
  );
};

export default MainMenu;