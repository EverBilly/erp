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
  Alert
} from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

// Iconos de Material UI
import {
  Home,
  People,
  Settings,
  Shield,
  Key,
  Menu as MenuIcon,
  Assessment,
  Person,
  Lock,
  Monitor,
  List,
  Dashboard,
  AdminPanelSettings,
  Assignment,
  BarChart,
  Inventory,
  ShoppingCart,
  Receipt,
  Build,
  History,
  Report,
  VerifiedUser,
  Security
} from '@mui/icons-material';

// Mapeo de iconos (puedes extenderlo según tu backend)
const ICON_MAP = {
  'home': Home,
  'users': People,
  'settings': Settings,
  'shield': Shield,
  'key': Key,
  'menu': MenuIcon,
  'activity': Assessment,
  'bar-chart': BarChart,
  'user': Person,
  'lock': Lock,
  'monitor': Monitor,
  'list': List,
  'dashboard': Dashboard,
  'admin_panel_settings': AdminPanelSettings,
  'assignment': Assignment,
  'inventory': Inventory,
  'shopping_cart': ShoppingCart,
  'receipt': Receipt,
  'build': Build,
  'history': History,
  'report': Report,
  'verified_user': VerifiedUser,
  'security': Security,
  'default': Person
};

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
            const IconComponent = ICON_MAP[item.icono] || ICON_MAP['default'];
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
                      borderRadius: 2,
                      border: `1px solid ${theme.palette.divider}`,
                      bgcolor: theme.palette.background.paper,
                      transition: 'all 0.3s ease',
                      boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
                      '&:hover': {
                        transform: 'translateY(-4px)',
                        boxShadow: '0 6px 16px rgba(0,0,0,0.1)',
                        borderColor: theme.palette.primary.main,
                        bgcolor: theme.palette.action.hover,
                      },
                      // Colores por categoría
                      ...(item.nombre.includes('Administración') && { borderColor: '#1976d2' }),
                      ...(item.nombre.includes('Usuarios') && { borderColor: '#e91e63' }),
                      ...(item.nombre.includes('Configuración') && { borderColor: '#607d8b' }),
                      ...(item.nombre.includes('Reportes') && { borderColor: '#9c27b0' }),
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