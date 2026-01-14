import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  Box,
  Grid,
  Chip,
  CircularProgress,
  Breadcrumbs,
  Link,
  Alert,
  FormControlLabel,
  Autocomplete
} from '@mui/material';
import { Formik, Form, Field } from 'formik';
import * as Yup from 'yup';
import api from '../../services/api';
import { useNotification } from '../../context/NotificationContext';
import { useAuth } from '../../context/AuthContext';

const usuarioSchema = Yup.object().shape({
  username: Yup.string().required('Requerido').min(3, 'Mínimo 3 caracteres'),
  email: Yup.string().email('Email inválido').required('Requerido'),
  nombreCompleto: Yup.string().required('Requerido'),
  roleIds: Yup.array().min(1, 'Selecciona al menos un rol'),
  password: Yup.string().min(6, 'Mínimo 6 caracteres')
});

const UsuarioForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;
  const { showNotification } = useNotification();
  const { user } = useAuth();

  // Estados
  const [roles, setRoles] = useState([]);
  const [loadingRoles, setLoadingRoles] = useState(true);
  const [loadingFetch, setLoadingFetch] = useState(isEdit);
  const [error, setError] = useState('');

  const [initialValuesForm, setInitialValuesForm] = useState({
    username: '',
    email: '',
    nombreCompleto: '',
    telefono: '',
    activo: true,
    password: '',
    roleIds: []
  });

  // Cargar Roles al montar
  useEffect(() => {
    const fetchRoles = async () => {
      setLoadingRoles(true);
      try {
        const response = await api.get('/roles');
        setRoles(response.data);
      } catch (err) {
        console.error("Error cargando roles", err);
        showNotification('Error al cargar roles', 'error');
      } finally {
        setLoadingRoles(false);
      }
    };
    fetchRoles();
  }, []);

  // Cargar Usuario si es edición
  useEffect(() => {
    if (isEdit) {
      const fetchUsuario = async () => {
        setLoadingFetch(true);
        try {
          const response = await api.get(`/usuarios/${id}`);
          const userData = response.data;
          
          const roleIds = userData.roles ? userData.roles.map(r => r.id) : [];
          
          setInitialValuesForm({
            username: userData.username,
            email: userData.email,
            nombreCompleto: userData.nombreCompleto,
            telefono: userData.telefono,
            activo: userData.activo,
            roleIds: roleIds,
            password: ''
          });
        } catch (err) {
          console.error("Error al cargar usuario", err);
          showNotification('Error al cargar usuario', 'error');
        } finally {
          setLoadingFetch(false);
        }
      };
      fetchUsuario();
    }
  }, [id, isEdit, showNotification]);

  const handleSubmit = async (values, { setSubmitting }) => {
    setError('');
    try {
      const payload = { ...values };
      
      // Agregar asignadoPor con el ID del usuario logueado
      payload.asignadoPor = user.id;

      if (isEdit) {
        if (!payload.password?.trim()) {
          delete payload.password;
        }
        await api.put(`/usuarios/${id}`, payload);
        showNotification('Usuario actualizado correctamente', 'success');
      } else {
        if (!payload.password?.trim()) {
          throw new Error('La contraseña es requerida');
        }
        await api.post('/usuarios', payload);
        showNotification('Usuario creado exitosamente', 'success');
      }

      navigate('/usuarios');
    } catch (err) {
      console.error("Error al guardar", err);
      const msg = err.response?.data?.message || 
                  err.response?.data?.error || 
                  'Error al guardar usuario';
      setError(msg);
      setSubmitting(false);
    }
  };

  // Mostrar loading mientras se cargan datos
  if (loadingFetch || loadingRoles) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', my: 10 }}>
        <CircularProgress />
        <Typography sx={{ ml: 2 }}>
          {loadingFetch ? 'Cargando usuario...' : 'Cargando permisos...'}
        </Typography>
      </Box>
    );
  }

  return (
    <Container maxWidth="md">
      <Box sx={{ my: 4 }}>
        <Paper elevation={3} sx={{ p: 3 }}>
          <Typography variant="h5" gutterBottom>
            {isEdit ? 'Editar Usuario' : 'Nuevo Usuario'}
          </Typography>

          {/* Navegación */}
          <Box sx={{ mb: 2 }}>
            <Breadcrumbs aria-label="breadcrumb">
              <Link underline="hover" color="inherit" href="/">
                <Typography sx={{ fontWeight: 'bold' }}>Inicio</Typography>
              </Link>
              <Link underline="hover" color="inherit" href="/usuarios">
                <Typography color="text.primary">Usuarios</Typography>
              </Link>
              <Typography color="text.primary">{isEdit ? 'Editar' : 'Nuevo'}</Typography>
            </Breadcrumbs>
          </Box>

          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

          <Formik
            initialValues={initialValuesForm}
            validationSchema={usuarioSchema}
            onSubmit={handleSubmit}
            enableReinitialize={true}
          >
            {({ values, errors, touched, setFieldValue, isSubmitting }) => {
              // Calcular availableRoles en cada render
              const isSuperAdmin = user?.roles?.some(r => r.authority === 'SUPER_ADMIN');
              const availableRoles = roles.filter(role => {
                if (role.nombre === 'SUPER_ADMIN' && !isSuperAdmin) {
                  return false;
                }
                return true;
              });

              const validRoleIds = values.roleIds.filter(id => 
                availableRoles.some(role => role.id === id)
              );

              return (
                <Form>
                  <Grid container spacing={2}>
                    <Grid item xs={12} sm={6}>
                      <Field name="username">
                        {({ field }) => (
                          <TextField
                            {...field}
                            label="Nombre de usuario"
                            fullWidth
                            error={touched.username && !!errors.username}
                            helperText={touched.username && errors.username}
                            disabled={isEdit}
                          />
                        )}
                      </Field>
                    </Grid>

                    <Grid item xs={12} sm={6}>
                      <Field name="email">
                        {({ field }) => (
                          <TextField
                            {...field}
                            label="Email"
                            type="email"
                            fullWidth
                            error={touched.email && !!errors.email}
                            helperText={touched.email && errors.email}
                          />
                        )}
                      </Field>
                    </Grid>

                    <Grid item xs={12}>
                      <Field name="nombreCompleto">
                        {({ field }) => (
                          <TextField
                            {...field}
                            label="Nombre completo"
                            fullWidth
                            error={touched.nombreCompleto && !!errors.nombreCompleto}
                            helperText={touched.nombreCompleto && errors.nombreCompleto}
                          />
                        )}
                      </Field>
                    </Grid>

                    <Grid item xs={12} sm={6}>
                      <Field name="telefono">
                        {({ field }) => (
                          <TextField
                            {...field}
                            label="Teléfono"
                            fullWidth
                          />
                        )}
                      </Field>
                    </Grid>

                    <Grid item xs={12} sm={6}>
                      <Box sx={{ display: 'flex', alignItems: 'center', height: '100%' }}>
                        <Field name="activo">
                          {({ field }) => (
                            <FormControlLabel
                              control={
                                <input
                                  type="checkbox"
                                  checked={field.value}
                                  onChange={(e) => setFieldValue('activo', e.target.checked)}
                                  style={{ width: 20, height: 20 }}
                                />
                              }
                              label="Usuario Activo"
                            />
                          )}
                        </Field>
                      </Box>
                    </Grid>

                    {!isEdit && (
                      <Grid item xs={12}>
                        <Field name="password">
                          {({ field }) => (
                            <TextField
                              {...field}
                              label="Contraseña"
                              type="password"
                              fullWidth
                              error={touched.password && !!errors.password}
                              helperText={touched.password && errors.password}
                            />
                          )}
                        </Field>
                      </Grid>
                    )}

                    {/* Selector de Roles con Autocomplete */}
                    <Grid item xs={12}>
                      <Autocomplete
                        multiple
                        options={availableRoles}
                        getOptionLabel={(option) => option.nombre}
                        value={availableRoles.filter(role => 
                          validRoleIds.includes(role.id)
                        )}
                        onChange={(event, newValue) => {
                          setFieldValue('roleIds', newValue.map(role => role.id));
                        }}
                        renderInput={(params) => (
                          <TextField
                            {...params}
                            label="Asignar Roles"
                            error={touched.roleIds && !!errors.roleIds}
                            helperText={touched.roleIds && errors.roleIds}
                          />
                        )}
                        renderTags={(value, getTagProps) =>
                          value.map((option, index) => (
                            <Chip
                              key={option.id}
                              label={option.nombre}
                              {...getTagProps({ index })}
                            />
                          ))
                        }
                      />
                    </Grid>

                    <Grid item xs={12}>
                      <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 2 }}>
                        <Button onClick={() => navigate('/usuarios')}>
                          Cancelar
                        </Button>
                        <Button
                          type="submit"
                          variant="contained"
                          disabled={isSubmitting}
                        >
                          {isSubmitting ? 'Guardando...' : (isEdit ? 'Actualizar' : 'Crear')}
                        </Button>
                      </Box>
                    </Grid>
                  </Grid>
                </Form>
              );
            }}
          </Formik>
        </Paper>
      </Box>
    </Container>
  );
};

export default UsuarioForm;