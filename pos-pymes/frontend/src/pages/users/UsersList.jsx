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

const UsersList = () => {
  const [users, setUsers] = useState([]);
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

  const itemsPerPage = 10;

  const fetchStats = async () => {
    try {
      const response = await api.get('/users/stats');
      setStats(response.data);
    } catch (error) {
      console.error('Error al cargar estadísticas:', error);
    }
  };

  const fetchUsersList = async () => {
    try {
      const isSuperAdmin = user?.roles?.some(r => r.authority === 'SUPER_ADMIN');
      const endpoint = isSuperAdmin ? '/users' : '/users/active';

      const response = await api.get(endpoint);
      const allUsers = response.data;

      const filtered = allUsers.filter(u =>
        u.fullName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        u.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        u.email?.toLowerCase().includes(searchTerm.toLowerCase())
      );

      const startIndex = (currentPage - 1) * itemsPerPage;
      const paginated = filtered.slice(startIndex, startIndex + itemsPerPage);

      setUsers(paginated);
      setTotalPages(Math.ceil(filtered.length / itemsPerPage));
    } catch (error) {
      console.error('Error al cargar usuarios:', error);
      showNotification('Error al cargar usuarios', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user) {
      fetchStats();
      fetchUsersList();
    }
  }, [user, searchTerm, currentPage]);

  const refreshData = async () => {
    await fetchStats();
    await fetchUsersList();
  };

  const handleDelete = async (id, name) => {
    if (window.confirm(`¿Estás seguro de DESACTIVAR al usuario ${name}?`)) {
      try {
        await api.delete(`/users/${id}`);
        showNotification('Usuario desactivado correctamente', 'warning');
        refreshData();
      } catch (error) {
        showNotification('Error al desactivar usuario', 'error');
      }
    }
  };

  const handleRestore = async (id, name) => {
    if (window.confirm(`¿Estás seguro de REACTIVAR al usuario ${name}?`)) {
      try {
        await api.patch(`/users/${id}/activate`);
        showNotification('Usuario reactivado correctamente', 'success');
        refreshData();
      } catch (error) {
        showNotification('Error al reactivar usuario', 'error');
      }
    }
  };

  const handleView = (u) => {
    setViewingUser(u);
    setShowViewModal(true);
  };

  const handleSearchChange = (event) => {
    setSearchTerm(event.target.value);
    setCurrentPage(1);
  };

  const isSuperAdmin = user?.roles?.some(r => r.authority === 'SUPER_ADMIN');

  if (loading) {
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
        <Paper elevation={2} sx={{ p: 3, mb: 4, borderRadius: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Box>
              <Typography variant="h4" sx={{ mb: 0.5, fontWeight: 600 }}>Gestión de Usuarios</Typography>
              <Typography variant="body2" color="text.secondary">Administración de cuentas de usuario</Typography>
            </Box>
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
              onClick={() => navigate('/users/new')}
              sx={{ backgroundColor: '#1976d2', '&:hover': { backgroundColor: '#1565c0' } }}
            >
              Nuevo Usuario
            </Button>
          </Box>
        </Paper>

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
                {users.map((u, index) => (
                  <TableRow
                    key={u.id}
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
                        <strong>{u.username}</strong>
                      </Box>
                    </TableCell>
                    <TableCell>{u.fullName}</TableCell>
                    <TableCell>{u.email}</TableCell>
                    {isSuperAdmin && (
                      <TableCell>
                        <Tooltip title={u.roles?.[0]?.description || 'Sin descripción'}>
                          <Chip
                            label={u.roles?.[0]?.displayName || 'Sin rol'}
                            color={u.roles?.[0]?.color || 'default'}
                            size="small"
                            variant="outlined"
                          />
                        </Tooltip>
                      </TableCell>
                    )}
                    <TableCell>
                      <Chip
                        label={u.active ? 'Activo' : 'Inactivo'}
                        color={u.active ? 'success' : 'default'}
                        size="small"
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                        <Tooltip title="Ver detalles">
                          <IconButton onClick={() => handleView(u)} size="small" sx={{ color: 'info.main' }}>
                            <Visibility />
                          </IconButton>
                        </Tooltip>

                        {u.username !== 'superadmin' && u.id !== user?.id ? (
                          <>
                            <Tooltip title="Editar">
                              <IconButton
                                onClick={() => navigate(`/users/edit/${u.id}`)}
                                size="small"
                                sx={{ color: 'primary.main' }}
                              >
                                <Edit />
                              </IconButton>
                            </Tooltip>

                            {u.active ? (
                              <Tooltip title="Desactivar">
                                <IconButton onClick={() => handleDelete(u.id, u.username)} size="small" sx={{ color: 'error.main' }}>
                                  <Delete />
                                </IconButton>
                              </Tooltip>
                            ) : (
                              <Tooltip title="Reactivar Usuario">
                                <IconButton onClick={() => handleRestore(u.id, u.username)} size="small" sx={{ color: 'success.main' }}>
                                  <RestoreIcon />
                                </IconButton>
                              </Tooltip>
                            )}
                          </>
                        ) : (
                          <Typography variant="caption" color="textSecondary">
                            {u.id === user?.id ? '(Tu cuenta)' : '(Protegido)'}
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

      <Dialog open={showViewModal} onClose={() => setShowViewModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Detalles del Usuario</DialogTitle>
        <DialogContent>
          {viewingUser && (
            <Box sx={{ py: 2 }}>
              <Typography variant="subtitle2">Nombre de Usuario:</Typography>
              <Typography variant="body1" sx={{ mb: 2, fontWeight: 600 }}>{viewingUser.username}</Typography>

              <Typography variant="subtitle2">Nombre Completo:</Typography>
              <Typography variant="body1" sx={{ mb: 2 }}>{viewingUser.fullName}</Typography>

              <Typography variant="subtitle2">Email:</Typography>
              <Typography variant="body1" sx={{ mb: 2 }}>{viewingUser.email}</Typography>

              {isSuperAdmin && (
                <>
                  <Typography variant="subtitle2">Rol:</Typography>
                  <Tooltip title={viewingUser.roles?.[0]?.description || 'Sin descripción'}>
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
                label={viewingUser.active ? 'Activo' : 'Inactivo'}
                color={viewingUser.active ? 'success' : 'default'}
                variant="outlined"
                sx={{ mb: 2 }}
              />

              <Typography variant="subtitle2">Fecha de Creación:</Typography>
              <Typography variant="body1">
                {viewingUser.createdAt ? new Date(viewingUser.createdAt).toLocaleString() : 'No disponible'}
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

export default UsersList;
