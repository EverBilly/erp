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
  Alert,
  FormControlLabel
} from '@mui/material';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import api from '../../services/api';

const usuarioSchema = Yup.object().shape({
  username: Yup.string().required('Requerido').min(3, 'Mínimo 3 caracteres'),
  email: Yup.string().email('Email inválido').required('Requerido'),
  nombreCompleto: Yup.string().required('Requerido'),
  roleIds: Yup.array().min(1, 'Selecciona al menos un rol'), // Validación de roles
  // Password solo requerido si es nuevo
});

const UsuarioForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [roles, setRoles] = useState([]); // Lista de todos los roles disponibles
  const [loadingRoles, setLoadingRoles] = useState(true);
  const [error, setError] = useState('');

  // Cargar roles al montar el componente
  useEffect(() => {
    const fetchRoles = async () => {
      try {
        const response = await api.get('/api/roles');
        setRoles(response.data);
      } catch (err) {
        console.error("Error cargando roles", err);
      } finally {
        setLoadingRoles(false);
      }
    };

    fetchRoles();

    // Si es edición, cargar datos del usuario
    if (isEdit) {
      // Aquí podrías hacer un fetch para llenar el form si tus valores iniciales no están ya ahí
    }
  }, [isEdit, id]);

  const handleSubmit = async (values, { setSubmitting }) => {
    setError('');
    try {
      if (isEdit) {
        await api.put(`/api/usuarios/${id}`, values);
      } else {
        await api.post('/api/usuarios', {
          ...values,
          passwordHash: values.password // Enviar el password como llega
        });
      }
      navigate('/usuarios');
    } catch (err) {
      setError(err.response?.data?.error || 'Error al guardar usuario');
      setSubmitting(false);
    }
  };

  return (
    <Container maxWidth="md">
      <Box sx={{ my: 4 }}>
        <Paper elevation={3} sx={{ p: 4 }}>
          <Typography variant="h5" gutterBottom>
            {isEdit ? 'Editar Usuario' : 'Nuevo Usuario'}
          </Typography>

          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

          <Formik
            initialValues={{
              username: '',
              email: '',
              nombreCompleto: '',
              telefono: '',
              activo: true,
              password: '',
              roleIds: [] // Inicialmente vacío
            }}
            validationSchema={usuarioSchema}
            onSubmit={handleSubmit}
            enableReinitialize={isEdit}
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
                    {/* Checkbox Activo */}
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

                  {/* <--- SELECTOR MÚLTIPLE DE ROLES ---> */}
                  <Grid item xs={12}>
                    <FormControl fullWidth error={touched.roleIds && !!errors.roleIds}>
                      <InputLabel>Asignar Roles</InputLabel>
                      {loadingRoles ? (
                        <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
                          <CircularProgress size={20} />
                        </Box>
                      ) : (
                        <Field name="roleIds">
                          {({ field }) => (
                            <Select
                              {...field}
                              multiple
                              value={field.value || []}
                              onChange={(e) => setFieldValue('roleIds', e.target.value)}
                              renderValue={(selected) => (
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                                  {selected.map((value) => {
                                    const role = roles.find(r => r.id === value);
                                    return (
                                      <Chip key={value} label={role ? role.nombre : value} />
                                    );
                                  })}
                                </Box>
                              )}
                            >
                              {roles.map((role) => (
                                <MenuItem key={role.id} value={role.id}>
                                  {role.nombre}
                                </MenuItem>
                              ))}
                            </Select>
                          )}
                        </Field>
                      )}
                      {touched.roleIds && errors.roleIds && (
                        <Typography variant="caption" color="error" sx={{ mt: 0.5 }}>
                          {errors.roleIds}
                        </Typography>
                      )}
                    </FormControl>
                  </Grid>

                  <Grid item xs={12}>
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 2 }}>
                      <Button onClick={() => navigate('/usuarios')}>
                        Cancelar
                      </Button>
                      <Button type="submit" variant="contained" disabled={isSubmitting}>
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