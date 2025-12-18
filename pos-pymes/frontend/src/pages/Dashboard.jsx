import React from 'react';
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
import {
  PointOfSale as PosIcon,
  Inventory as InventoryIcon,
  People as PeopleIcon,
  Assessment as ReportIcon
} from '@mui/icons-material';

const Dashboard = () => {
  const { user, logout } = useAuth();

  const modules = [
    { title: 'Punto de Venta', icon: <PosIcon />, path: '/ventas', color: '#4caf50' },
    { title: 'Productos', icon: <InventoryIcon />, path: '/productos', color: '#2196f3' },
    { title: 'Clientes', icon: <PeopleIcon />, path: '/clientes', color: '#ff9800' },
    { title: 'Reportes', icon: <ReportIcon />, path: '/reportes', color: '#9c27b0' },
  ];

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
                Bienvenido, {user?.nombre} {user?.apellido}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Rol: {user?.roles?.join(', ')}
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
          {modules.map((module, index) => (
            <Grid item xs={12} sm={6} md={3} key={index}>
              <Card 
                sx={{ 
                  height: '100%',
                  cursor: 'pointer',
                  transition: 'transform 0.2s',
                  '&:hover': {
                    transform: 'translateY(-4px)'
                  }
                }}
                onClick={() => window.location.href = module.path}
              >
                <CardContent sx={{ textAlign: 'center', p: 3 }}>
                  <Box sx={{ 
                    display: 'inline-flex',
                    p: 2,
                    borderRadius: '50%',
                    bgcolor: module.color + '20',
                    color: module.color,
                    mb: 2
                  }}>
                    {React.cloneElement(module.icon, { sx: { fontSize: 40 } })}
                  </Box>
                  <Typography variant="h6" gutterBottom>
                    {module.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Acceder al módulo
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>

        <Paper elevation={2} sx={{ p: 3, mt: 4 }}>
          <Typography variant="h6" gutterBottom>
            Permisos del usuario
          </Typography>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            {user?.permisos?.map((permiso, index) => (
              <Paper 
                key={index}
                variant="outlined" 
                sx={{ 
                  p: 1, 
                  px: 2, 
                  borderRadius: 2,
                  bgcolor: '#f5f5f5'
                }}
              >
                <Typography variant="body2">
                  {permiso}
                </Typography>
              </Paper>
            ))}
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Dashboard;