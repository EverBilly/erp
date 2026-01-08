import React, {useState} from 'react';
import {
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  Box,
  FormControlLabel,
  Checkbox,
  Grid,
  Alert
} from '@mui/material';
import { Formik, Form, Field } from 'formik';
import * as Yup from 'yup';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../../services/api';

const usuarioSchema = Yup.object().shape({
  username: Yup.string()
    .required('Requerido')
    .min(3, 'Mínimo 3 caracteres'),
  email: Yup.string()
    .email('Email inválido')
    .required('Requerido'),
  nombreCompleto: Yup.string().required('Requerido'),
  telefono: Yup.string().nullable(),
  ...(window.location.pathname.includes('/nuevo') && {
    password: Yup.string().required('Requerido').min(6, 'Mínimo 6 caracteres')
  })
});

const UsuarioForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;

  const [error, setError] = useState('');

  const fetchUsuario = async () => {
    const response = await api.get(`/api/usuarios/${id}`);
    return response.data;
  };

  const handleSubmit = async (values, { setSubmitting }) => {
    setError('');
    try {
      if (isEdit) {
        await api.put(`/api/usuarios/${id}`, values);
      } else {
        // Para crear, el backend espera `passwordHash` como campo de la contraseña en texto plano
        await api.post('/api/usuarios', {
          ...values,
          passwordHash: values.password
        });
      }
      navigate('/usuarios');
    } catch (err) {
      setError(err.response?.data?.error || 'Error al guardar usuario');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Container maxWidth="md">
      <Box sx={{ my: 4 }}>
        <Paper elevation={3} sx={{ p: 3 }}>
          <Typography variant="h4" gutterBottom>
            {isEdit ? 'Editar Usuario' : 'Crear Usuario'}
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Formik
            initialValues={{
              username: '',
              email: '',
              nombreCompleto: '',
              telefono: '',
              activo: true,
              password: '',
              ...(isEdit && { id })
            }}
            validationSchema={usuarioSchema}
            onSubmit={handleSubmit}
            enableReinitialize={isEdit}
          >
            {({ values, errors, touched, isSubmitting, setFieldValue }) => (
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
                    <FormControlLabel
                      control={
                        <Field name="activo">
                          {({ field }) => (
                            <Checkbox
                              {...field}
                              checked={field.value}
                              onChange={(e) => setFieldValue('activo', e.target.checked)}
                            />
                          )}
                        </Field>
                      }
                      label="Usuario activo"
                    />
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

                  <Grid item xs={12}>
                    <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                      <Button onClick={() => navigate('/usuarios')}>
                        Cancelar
                      </Button>
                      <Button
                        type="submit"
                        variant="contained"
                        disabled={isSubmitting}
                      >
                        {isEdit ? 'Actualizar' : 'Crear'}
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