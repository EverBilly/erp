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
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  CircularProgress,
  Breadcrumbs,
  Link,
  Alert,
  FormControlLabel
} from '@mui/material';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import api from '../../services/api';
import { useNotification } from '../../context/NotificationContext';

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

  // Estados
  const [roles, setRoles] = useState([]);
  const [loadingRoles, setLoadingRoles] = useState(true);
  const [loadingFetch, setLoadingFetch] = useState(isEdit);
  const [error, setError] = useState('');

  // <--- ESTADO DINÁMICO PARA INITIALVALUES (FIX EDICIÓN) --->
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
      try {
        const response = await api.get('/roles');
        setRoles(response.data);
      } catch (err) {
        console.error("Error cargando roles", err);
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
          const user = response.data;
          
          const roleIds = user.roles ? user.roles.map(r => r.id) : [];
          
          setInitialValuesForm({
            username: user.username,
            email: user.email,
            nombreCompleto: user.nombreCompleto,
            telefono: user.telefono,
            activo: user.activo,
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
    // Clonamos los valores para no mutar el estado de Formik
    const payload = { ...values };

    // En edición: si password está vacío, lo eliminamos del payload
    if (isEdit) {
      if (!payload.password || payload.password.trim() === '') {
        delete payload.password;
      }
      await api.put(`/usuarios/${id}`, payload);
      showNotification('Usuario actualizado correctamente', 'success');
    } else {
      // En creación: aseguramos que se envíe la contraseña
      if (!payload.password || payload.password.trim() === '') {
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

  if (loadingFetch) return (
    <Box sx={{ display: 'flex', justifyContent: 'center', my: 10 }}>
      <CircularProgress />
    </Box>
  );

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
            // <--- CONECTAMOS ESTADO INITIALVALUES AQUÍ --->
            initialValues={initialValuesForm}
            validationSchema={usuarioSchema}
            onSubmit={handleSubmit}
            enableReinitialize={true}
          >
            {({ values, errors, touched, setFieldValue, isSubmitting }) => (
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

                  {/* <--- SELECTOR DE ROLES (COMPONENTES SEGURAS) --->*/}
                  <Grid item xs={12}>
                    <FormControl fullWidth error={touched.roleIds && !!errors.roleIds}>
                      <InputLabel id="roles-label">Asignar Roles</InputLabel>
                      {loadingRoles ? (
                        <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
                          <CircularProgress size={20} />
                        </Box>
                      ) : (
                        <Select
                          labelId="roles-label"
                          multiple
                          value={values.roleIds || []} // Asegura que siempre sea array
                          onChange={(e) => {
                            const {
                              target: { value },
                            } = e;
                            // value será un array cuando multiple={true}
                            setFieldValue('roleIds', value);
                          }}
                          renderValue={(selected) => (
                            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                              {selected.map((id) => {
                                const role = roles.find(r => r.id === id);
                                return <Chip key={id} label={role?.nombre || id} size="small" />;
                              })}
                            </Box>
                          )}
                          label="Asignar Roles"
                        >
                          {roles.map((role) => (
                            <MenuItem key={role.id} value={role.id}>
                              {role.nombre}
                            </MenuItem>
                          ))}
                        </Select>
                      )}
                      <ErrorMessage name="roleIds" component={Typography} variant="caption" color="error" sx={{ mt: 0.5 }} />
                    </FormControl>
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
            )}
          </Formik>
        </Paper>
      </Box>
    </Container>
  );
};

export default UsuarioForm;