import React from 'react';
import { Routes, Route } from 'react-router-dom';
import UsuariosList from './UsuariosList';
import UsuarioForm from './UsuarioForm';

const UsuariosView = () => (
  <Routes>
    <Route path="/" element={<UsuariosList />} />
    <Route path="/nuevo" element={<UsuarioForm />} />
    <Route path="/editar/:id" element={<UsuarioForm />} />
  </Routes>
);

export default UsuariosView;