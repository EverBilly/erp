import React, { useState, useEffect } from 'react';
import {
  Container,
  Paper,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  IconButton,
  Chip,
  Box,
  Skeleton,
  Breadcrumbs,
  Link,
  Tooltip,
  Typography as MuiTypography
} from '@mui/material';
import { Edit, Delete, Add, Person as PersonIcon, RestoreFromTrash as RestoreIcon } from '@mui/icons-material'; // <--- Importamos Restore
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { useNotification } from '../../context/NotificationContext';

const UsuariosList = () => {
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const { showNotification } = useNotification();

  // <--- CARGA INICIAL (SÓLO ACTIVOS) --->
  useEffect(() => {
    const fetchUsuarios = async () => {
      try {
        // <--- CAMBIO AQUÍ: Usamos el endpoint de ACTIVOS --->
        const response = await api.get('/usuarios/activos'); 
        setUsuarios(response.data);
      } catch (error) {
        console.error('Error al cargar usuarios:', error);
        showNotification('Error al cargar usuarios', 'error');
      } finally {
        setLoading(false);
      }
    };
    fetchUsuarios();
  }, [showNotification]);

  const handleDelete = async (id, nombre) => {
    if (window.confirm(`¿Estás seguro de DESACTIVAR al usuario ${nombre}?`)) {
      try {
        await api.delete(`/usuarios/${id}`);
        showNotification('Usuario desactivado correctamente', 'warning');
        // Recargar lista (ahora debería desaparecer)
        setUsuarios(prev => prev.filter(u => u.id !== id));
      } catch (error) {
        showNotification('Error al desactivar usuario', 'error');
      }
    }
  };
  
  // <--- FUNCIÓN DE ACTIVACIÓN (Para usuarios inactivos) --->
  const handleRestore = async (id, nombre) => {
    if (window.confirm(`¿Estás seguro de REACTIVAR al usuario ${nombre}?`)) {
      try {
        // Usamos el endpoint de activar (Patch)
        await api.patch(`/usuarios/${id}/activar`);
        showNotification('Usuario reactivado correctamente', 'success');
        // Recargar lista (ahora debería aparecer si tenemos lógica de ver todos, o se mantiene en lista actual)
        window.location.reload(); // Opcional para refrescar simple si lógica compleja
      } catch (error) {
        showNotification('Error al reactivar usuario', 'error');
      }
    }
  };

  if (loading) {
    // Skeletons para que se vea bonito mientras carga
    return (
      <Container maxWidth="lg">
        <Box sx={{ my: 4 }}>
          <Skeleton variant="rectangular" height={400} />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ my: 4 }}>
        {/* <--- CABECERA Y NAVEGACIÓN --->*/}
        <Paper elevation={2} sx={{ p: 2, mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box>
            <Typography variant="h4" sx={{ mb: 0.5 }}>Gestión de Usuarios</Typography>
            <Typography variant="body2" color="text.secondary">Solo usuarios activos</Typography>
          </Box>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => navigate('/usuarios/nuevo')}
          >
            Nuevo Usuario
          </Button>
        </Paper>

        {/* TABLA DE USUARIOS */}
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Usuario</TableCell>
                <TableCell>Nombre</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell>Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {usuarios.map((usuario) => (
                <TableRow key={usuario.id}>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <PersonIcon color="primary" />
                      <strong>{usuario.username}</strong>
                    </Box>
                  </TableCell>
                  <TableCell>{usuario.nombreCompleto}</TableCell>
                  <TableCell>{usuario.email}</TableCell>
                  <TableCell>
                    <Chip
                        label={usuario.activo ? 'Activo' : 'Inactivo'}
                        color={usuario.activo ? 'success' : 'default'}
                        size="small"
                    />
                  </TableCell>
                  <TableCell>
                    {/* <--- LÓGICA INTELIGENTE DE BOTONES --->*/}
                    {/* Si usuario es SUPERADMIN (ID 1), no mostrar botones */}
                    {usuario.username !== 'superadmin' ? (
                      <>
                        <Tooltip title="Editar">
                          <IconButton
                            onClick={() => navigate(`/usuarios/editar/${usuario.id}`)}
                            color="primary"
                          >
                            <Edit />
                          </IconButton>
                        </Tooltip>

                        {/* Si está ACTIVO, mostrar Borrar (Desactivar) */}
                        {usuario.activo ? (
                          <Tooltip title="Desactivar">
                            <IconButton
                              onClick={() => handleDelete(usuario.id, usuario.username)}
                              color="error"
                            >
                              <Delete />
                            </IconButton>
                          </Tooltip>
                        ) : (
                          /* Si está INACTIVO, mostrar Activar (Restaurar) */
                          <Tooltip title="Reactivar Usuario">
                            <IconButton
                              onClick={() => handleRestore(usuario.id, usuario.username)}
                              color="success"
                            >
                              <RestoreIcon />
                            </IconButton>
                          </Tooltip>
                        )}
                      </>
                    ) : (
                      <Typography variant="caption" color="textSecondary">
                        (Protegido)
                      </Typography>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Box>
    </Container>
  );
};

export default UsuariosList;