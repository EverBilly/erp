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
  Switch,
  Autocomplete,
  Card,
  CardContent,
  CardHeader
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
  password: Yup.string().when('isEditing', {
    is: false,
    then: (schema) => schema.required('Requerido').min(6, 'Mínimo 6 caracteres'),
    otherwise: (schema) => schema.min(6, 'Mínimo 6 caracteres'),
  }),
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
  const [showPassword, setShowPassword] = useState(false);

  const [initialValuesForm, setInitialValuesForm] = useState({
    username: '',
    email: '',
    nombreCompleto: '',
    telefono: '',
    activo: true,
    password: '',
    roleIds: [],
    isEditing: isEdit
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
            password: '',
            isEditing: true
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

      // Eliminar el campo isEditing antes del payload
      delete payload.isEditing;

      if (isEdit) {
        // Si no se proporciona contraseña, no la incluimos en el payload
        if (!payload.password?.trim()) {
          delete payload.password;
        }
        await api.put(`/usuarios/${id}`, payload);
        showNotification('Usuario actualizado correctamente', 'success');
      } else {
        // Para nuevo usuario, la contraseña es obligatoria
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
    <Container maxWidth="lg">
      <Box sx={{ my: 4 }}>
        <Card sx={{ borderRadius: 2, boxShadow: 2 }}>
          <CardHeader
            title={isEdit ? 'Editar Usuario' : 'Nuevo Usuario'}
            subheader={isEdit ? 'Modifica la información del usuario' : 'Crea un nuevo usuario para el sistema'}
            sx={{ 
              backgroundColor: 'primary.light',
              color: 'white',
              borderRadius: '8px 8px 0 0'
            }}
          />
          <CardContent>
            {/* Navegación */}
            <Box sx={{ mb: 3 }}>
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
                    <Grid container spacing={3}>
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
                              variant="outlined"
                              size="small"
                              sx={{ mb: 2 }}
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
                              variant="outlined"
                              size="small"
                              sx={{ mb: 2 }}
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
                              variant="outlined"
                              size="small"
                              sx={{ mb: 2 }}
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
                              variant="outlined"
                              size="small"
                              sx={{ mb: 2 }}
                            />
                          )}
                        </Field>
                      </Grid>

                      <Grid item xs={12} sm={6}>
                        <Field name="activo">
                          {({ field }) => (
                            <FormControlLabel
                              control={
                                <Switch
                                  checked={field.value}
                                  onChange={(e) => setFieldValue('activo', e.target.checked)}
                                  color="primary"
                                />
                              }
                              label="Usuario Activo"
                            />
                          )}
                        </Field>
                      </Grid>

                      {!isEdit ? (
                        <Grid item xs={12}>
                          <Field name="password">
                            {({ field }) => (
                              <TextField
                                {...field}
                                label="Contraseña"
                                type={showPassword ? "text" : "password"}
                                fullWidth
                                error={touched.password && !!errors.password}
                                helperText={touched.password && errors.password}
                                variant="outlined"
                                size="small"
                                sx={{ mb: 2 }}
                              />
                            )}
                          </Field>
                        </Grid>
                      ) : (
                        <Grid item xs={12}>
                          <Field name="password">
                            {({ field }) => (
                              <TextField
                                {...field}
                                label="Nueva Contraseña (opcional)"
                                type={showPassword ? "text" : "password"}
                                fullWidth
                                error={touched.password && !!errors.password}
                                helperText={touched.password && errors.password || "Dejar vacío para mantener la contraseña actual"}
                                variant="outlined"
                                size="small"
                                sx={{ mb: 2 }}
                              />
                            )}
                          </Field>
                        </Grid>
                      )}

                      <Grid item xs={12}>
                        <FormControlLabel
                          control={
                            <Switch
                              checked={showPassword}
                              onChange={(e) => setShowPassword(e.target.checked)}
                              color="primary"
                            />
                          }
                          label="Mostrar contraseña"
                        />
                      </Grid>

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
                              variant="outlined"
                              size="small"
                            />
                          )}
                          renderTags={(value, getTagProps) =>
                            value.map((option, index) => (
                              <Chip
                                key={option.id}
                                label={option.nombre}
                                {...getTagProps({ index })}
                                size="small"
                                sx={{ mr: 0.5, mb: 0.5 }}
                              />
                            ))
                          }
                          sx={{ mb: 2 }}
                        />
                      </Grid>

                      <Grid item xs={12}>
                        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 2 }}>
                          <Button 
                            onClick={() => navigate('/usuarios')}
                            variant="outlined"
                            size="large"
                          >
                            Cancelar
                          </Button>
                          <Button
                            type="submit"
                            variant="contained"
                            disabled={isSubmitting}
                            size="large"
                            sx={{
                              backgroundColor: '#1976d2',
                              '&:hover': {
                                backgroundColor: '#1565c0',
                              }
                            }}
                          >
                            {isSubmitting ? (
                              <>
                                <CircularProgress size={20} sx={{ mr: 1 }} />
                                Guardando...
                              </>
                            ) : (isEdit ? 'Actualizar Usuario' : 'Crear Usuario')}
                          </Button>
                        </Box>
                      </Grid>
                    </Grid>
                  </Form>
                );
              }}
            </Formik>
          </CardContent>
        </Card>
      </Box>
    </Container>
  );
};

export default UsuarioForm;