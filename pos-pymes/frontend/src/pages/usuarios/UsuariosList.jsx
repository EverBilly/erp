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
  TextField,
  InputAdornment,
  Pagination,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions
} from '@mui/material';
import { Edit, Delete, Add, Person as PersonIcon, RestoreFromTrash as RestoreIcon, Search, Visibility } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { useNotification } from '../../context/NotificationContext';
import { useAuth } from '../../context/AuthContext';

const UsuariosList = () => {
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [showViewModal, setShowViewModal] = useState(false);
  const [viewingUser, setViewingUser] = useState(null);
  const [stats, setStats] = useState({ total: 0, activos: 0, inactivos: 0 });
  const navigate = useNavigate();
  const { showNotification } = useNotification();
  const { user } = useAuth();

  const itemsPerPage = 10; // Puedes ajustar esto según tus necesidades

  // Cargar estadísticas
  const fetchStats = async () => {
    try {
      const response = await api.get('/usuarios/contar');
      setStats(response.data);
    } catch (error) {
      console.error('Error al cargar estadísticas:', error);
    }
  };

  // <--- CARGA INICIAL USUARIOS --->
  const fetchUsuariosList = async () => {
    try {
      const isSuperAdmin = user?.roles?.some(r => r.authority === 'SUPER_ADMIN');
      // Elegir endpoint segun el rol
      const endpoint = isSuperAdmin ? '/usuarios' : '/usuarios/activos';

      const response = await api.get(endpoint); 
      const allUsuarios = response.data;

      // Filtrar por búsqueda
      const filtered = allUsuarios.filter(usuario =>
        usuario.nombreCompleto?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        usuario.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        usuario.email?.toLowerCase().includes(searchTerm.toLowerCase())
      );

      // Paginar resultados
      const startIndex = (currentPage - 1) * itemsPerPage;
      const paginated = filtered.slice(startIndex, startIndex + itemsPerPage);

      setUsuarios(paginated);
      setTotalPages(Math.ceil(filtered.length / itemsPerPage));
    } catch (error) {
      console.error('Error al cargar usuarios:', error);
      showNotification('Error al cargar usuarios', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if(user) {
      fetchStats();
      fetchUsuariosList();
    }
  }, [showNotification, user, searchTerm, currentPage]);

  const actualizarDespues = async () => {
    await fetchStats();
    await fetchUsuariosList();
  };

  // <--- FUNCIÓN DE DESACTIVACIÓN (Para usuarios activos) --->
  const handleDelete = async (id, nombre) => {
    if (window.confirm(`¿Estás seguro de DESACTIVAR al usuario ${nombre}?`)) {
      try {
        await api.delete(`/usuarios/${id}`);
        showNotification('Usuario desactivado correctamente', 'warning');
        actualizarDespues();
        fetchUsuariosList();
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
        actualizarDespues();
        fetchUsuariosList();
      } catch (error) {
        showNotification('Error al reactivar usuario', 'error');
      }
    }
  };

  const handleView = (usuario) => {
    setViewingUser(usuario);
    setShowViewModal(true);
  };

  const handleSearchChange = (event) => {
    setSearchTerm(event.target.value);
    setCurrentPage(1); // Resetear a primera página al buscar
  };

  // Determinar si el usuario es SUPER_ADMIN
  const isSuperAdmin = user?.roles?.some(r => r.authority === 'SUPER_ADMIN');

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
        {/* Cabecera con estadísticas */}
        <Paper elevation={2} sx={{ p: 3, mb: 4, borderRadius: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Box>
              <Typography variant="h4" sx={{ mb: 0.5, fontWeight: 600 }}>Gestión de Usuarios</Typography>
              <Typography variant="body2" color="text.secondary">Administración de cuentas de usuario</Typography>
            </Box>
            
            {/* Estadísticas */}
            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
              <Typography variant="body2" color="text.secondary">
                Total: {stats.total} | Activos: {stats.activos} | Inactivos: {stats.inactivos}
              </Typography>
            </Box>
          </Box>
          
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
            <TextField
              placeholder="Buscar usuarios..."
              variant="outlined"
              size="small"
              value={searchTerm}
              onChange={handleSearchChange}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Search />
                  </InputAdornment>
                ),
              }}
              sx={{ minWidth: 250 }}
            />
            
            <Button
              variant="contained"
              startIcon={<Add />}
              onClick={() => navigate('/usuarios/nuevo')}
              sx={{ backgroundColor: '#1976d2', '&:hover': { backgroundColor: '#1565c0' } }}
            >
              Nuevo Usuario
            </Button>
          </Box>
        </Paper>

        {/* Tabla de usuarios */}
        <Paper elevation={2} sx={{ borderRadius: 2 }}>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell sx={{ fontWeight: 600, backgroundColor: '#f8f9fa', width: '60px' }}>#</TableCell>
                  <TableCell sx={{ fontWeight: 600, backgroundColor: '#f8f9fa' }}>Usuario</TableCell>
                  <TableCell sx={{ fontWeight: 600, backgroundColor: '#f8f9fa' }}>Nombre</TableCell>
                  <TableCell sx={{ fontWeight: 600, backgroundColor: '#f8f9fa' }}>Email</TableCell>
                  {isSuperAdmin && (
                  <TableCell sx={{ fontWeight: 600, backgroundColor: '#f8f9fa' }}>Rol</TableCell>
                  )}
                  <TableCell sx={{ fontWeight: 600, backgroundColor: '#f8f9fa' }}>Estado</TableCell>
                  <TableCell sx={{ fontWeight: 600, backgroundColor: '#f8f9fa', textAlign: 'right' }}>Acciones</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {usuarios.map((usuario, index) => (
                  <TableRow 
                    key={usuario.id}
                    sx={{ 
                      '&:nth-of-type(even)': { backgroundColor: '#fafafa' },
                      '&:hover': { backgroundColor: '#f0f8ff' }
                    }}
                  >
                    <TableCell>
                      <strong>{index + 1 + ((currentPage - 1) * itemsPerPage)}</strong>
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <PersonIcon color="primary" />
                        <strong>{usuario.username}</strong>
                      </Box>
                    </TableCell>
                    <TableCell>{usuario.nombreCompleto}</TableCell>
                    <TableCell>{usuario.email}</TableCell>
                    {isSuperAdmin && (
                      <TableCell>
                        <Tooltip title={usuario.roles?.[0]?.descripcion || 'Sin descripción'}>
                          <Chip
                            label={usuario.roles?.[0]?.displayName || 'Sin rol'}
                            color={usuario.roles?.[0]?.color || 'default'}
                            size="small"
                            variant="outlined"
                          />
                        </Tooltip>
                      </TableCell>
                    )}
                    <TableCell>
                      <Chip
                        label={usuario.activo ? 'Activo' : 'Inactivo'}
                        color={usuario.activo ? 'success' : 'default'}
                        size="small"
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                        <Tooltip title="Ver detalles">
                          <IconButton
                            onClick={() => handleView(usuario)}
                            size="small"
                            sx={{ color: 'info.main' }}
                          >
                            <Visibility />
                          </IconButton>
                        </Tooltip>

                        {usuario.username !== 'superadmin' && usuario.id !== user?.id ? (
                          <>
                            <Tooltip title="Editar">
                              <IconButton
                                onClick={() => navigate(`/usuarios/editar/${usuario.id}`)}
                                size="small"
                                sx={{ color: 'primary.main' }}
                              >
                                <Edit />
                              </IconButton>
                            </Tooltip>

                            {usuario.activo ? (
                              <Tooltip title="Desactivar">
                                <IconButton
                                  onClick={() => handleDelete(usuario.id, usuario.username)}
                                  size="small"
                                  sx={{ color: 'error.main' }}
                                >
                                  <Delete />
                                </IconButton>
                              </Tooltip>
                            ) : (
                              <Tooltip title="Reactivar Usuario">
                                <IconButton
                                  onClick={() => handleRestore(usuario.id, usuario.username)}
                                  size="small"
                                  sx={{ color: 'success.main' }}
                                >
                                  <RestoreIcon />
                                </IconButton>
                              </Tooltip>
                            )}
                          </>
                        ) : (
                          <Typography variant="caption" color="textSecondary">
                            {usuario.id === user?.id ? '(Tu cuenta)' : '(Protegido)'}
                          </Typography>
                        )}
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          
          {totalPages > 1 && (
            <Box sx={{ p: 2, display: 'flex', justifyContent: 'center' }}>
              <Pagination
                count={totalPages}
                page={currentPage}
                onChange={(event, value) => setCurrentPage(value)}
                color="primary"
              />
            </Box>
          )}
        </Paper>
      </Box>

      {/* Modal de vista detallada */}
      <Dialog open={showViewModal} onClose={() => setShowViewModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Detalles del Usuario</DialogTitle>
        <DialogContent>
          {viewingUser && (
            <Box sx={{ py: 2 }}>
              <Typography variant="subtitle2">Nombre de Usuario:</Typography>
              <Typography variant="body1" sx={{ mb: 2, fontWeight: 600 }}>{viewingUser.username}</Typography>
              
              <Typography variant="subtitle2">Nombre Completo:</Typography>
              <Typography variant="body1" sx={{ mb: 2 }}>{viewingUser.nombreCompleto}</Typography>
              
              <Typography variant="subtitle2">Email:</Typography>
              <Typography variant="body1" sx={{ mb: 2 }}>{viewingUser.email}</Typography>
              
              {isSuperAdmin && (
                <>
                  <Typography variant="subtitle2">Rol:</Typography>
                  <Tooltip title={viewingUser.roles?.[0]?.descripcion || 'Sin descripción'}>
                    <Chip
                      label={viewingUser.roles?.[0]?.displayName || 'Sin rol'}
                      color={viewingUser.roles?.[0]?.color || 'default'}
                      variant="outlined"
                      sx={{ mb: 2 }}
                    />
                  </Tooltip>
                </>
              )}
              
              <Typography variant="subtitle2">Estado:</Typography>
              <Chip
                label={viewingUser.activo ? 'Activo' : 'Inactivo'}
                color={viewingUser.activo ? 'success' : 'default'}
                variant="outlined"
                sx={{ mb: 2 }}
              />
              
              <Typography variant="subtitle2">Fecha de Creación:</Typography>
              <Typography variant="body1">
                {viewingUser.fechaCreacion ? new Date(viewingUser.fechaCreacion).toLocaleString() : 'No disponible'}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowViewModal(false)}>Cerrar</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default UsuariosList;