import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Paper,
  Typography,
  Grid,
  Box,
  LinearProgress,
  Chip,
  Button
} from '@mui/material';
import {
  ShoppingBag as SalesIcon,
  TrendingUp as GrowthIcon,
  Warning as AlertIcon,
  People as UsersIcon
} from '@mui/icons-material';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  // Datos de ejemplo (estos deberían venir del backend en el futuro)
  const kpis = [
    { title: 'Ventas Hoy', value: '$12,450', progress: 75, color: 'success', icon: <SalesIcon /> },
    { title: 'Pedidos Pendientes', value: '24', progress: 30, color: 'warning', icon: <AlertIcon /> },
    { title: 'Usuarios Activos', value: '12', progress: 45, color: 'info', icon: <UsersIcon /> },
    { title: 'Crecimiento Mensual', value: '+15%', progress: 90, color: 'primary', icon: <GrowthIcon /> },
  ];

  return (
    <Box sx={{ flexGrow: 1 }}>
      
      {/* Cabecera de Bienvenida */}
      <Paper elevation={2} sx={{ p: 3, mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold', color: 'text.primary' }}>
            Panel de Control
          </Typography>
          <Typography variant="h6" color="text.secondary">
            Bienvenido, {user?.fullName || 'Usuario'}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            Rol: <strong>{user?.roles?.[0]?.authority || 'Sistema'}</strong>
          </Typography>
        </Box>
        <Button variant="outlined" color="error" onClick={() => { logout(); navigate('/login'); }}>
          Cerrar Sesión
        </Button>
      </Paper>

      {/* Tarjetas KPI */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {kpis.map((kpi, index) => (
          <Grid item xs={12} sm={6} md={3} key={index}>
            <Paper
              sx={{
                p: 2,
                display: 'flex',
                flexDirection: 'column',
                height: 140,
                justifyContent: 'center',
                bgcolor: 'background.paper',
                boxShadow: 3,
                '&:hover': { boxShadow: 6, transform: 'translateY(-4px)' },
                transition: 'all 0.3s',
                borderRadius: 2
              }}
            >
              <Box sx={{ display: 'flex', justifyContent: 'space-between', width: '100%', mb: 1 }}>
                <Typography variant="h6" color="textSecondary" sx={{ fontWeight: 500 }}>
                  {kpi.title}
                </Typography>
                <Box sx={{ color: `${kpi.color}.main`, fontSize: 32 }}>
                  {kpi.icon}
                </Box>
              </Box>
              <Typography variant="h4" component="div" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                {kpi.value}
              </Typography>
              <Box sx={{ mt: 1, width: '100%' }}>
                <LinearProgress variant="determinate" value={kpi.progress} color={kpi.color} />
              </Box>
            </Paper>
          </Grid>
        ))}
      </Grid>

      {/* Contenido Inferior */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 2, minHeight: 300 }}>
            <Typography variant="h6" gutterBottom sx={{ mb: 2 }}>
              Actividad Reciente
            </Typography>
            <Box sx={{ height: 220, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'text.secondary', border: '1px dashed #ccc', borderRadius: 1 }}>
              <Typography>Gráfico de ventas (Próximamente)</Typography>
            </Box>
          </Paper>
        </Grid>

        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 2, minHeight: 300 }}>
            <Typography variant="h6" gutterBottom sx={{ mb: 2 }}>
              Alertas del Sistema
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              <Chip label="Stock bajo: Arroz" color="error" variant="outlined" size="small" sx={{ justifyContent: 'flex-start' }} />
              <Chip label="CPU Servidor: 95%" color="warning" variant="outlined" size="small" sx={{ justifyContent: 'flex-start' }} />
              <Chip label="Backup Completado" color="success" variant="outlined" size="small" sx={{ justifyContent: 'flex-start' }} />
              <Chip label="Nuevo Usuario: Juan" color="info" variant="outlined" size="small" sx={{ justifyContent: 'flex-start' }} />
            </Box>
          </Paper>
        </Grid>
      </Grid>

    </Box>
  );
};

export default Dashboard;